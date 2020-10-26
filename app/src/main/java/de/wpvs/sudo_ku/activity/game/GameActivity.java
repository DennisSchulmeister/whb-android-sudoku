package de.wpvs.sudo_ku.activity.game;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.model.DatabaseHolder;
import de.wpvs.sudo_ku.model.game.GameDao;
import de.wpvs.sudo_ku.model.game.GameState;
import de.wpvs.sudo_ku.model.game.GameUtils;
import de.wpvs.sudo_ku.thread.BackgroundThreadHolder;
import de.wpvs.sudo_ku.thread.clock.ClockThread;
import de.wpvs.sudo_ku.thread.database.DatabaseThread;
import de.wpvs.sudo_ku.thread.database.PreloadKnownWords;
import de.wpvs.sudo_ku.thread.database.SaveOrDeleteGame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.concurrent.Semaphore;

/**
 * Main activity of an ongoing game. It shows the game board and handles the UI part of the
 * game logic.
 */
public class GameActivity extends AppCompatActivity implements Handler.Callback {
    private static final int SAVE_GAME_STATE_INTERVAL = 30;
    private int savedGameStateAge = 0;
    private long lastFullSecond = 0;

    private ActionBar actionBar;
    private MenuItem elapsedTimeMenuItem;
    private MenuItem progressMenuItem;

    private GameBoardFragment gameBoardFragment;
    private GameMatchedWordsFragment gameMatchedWordsFragment;
    private GameControlsFragment gameControlsFragment;

    private GameDao dao;
    private GameState gameState;
    private Handler handler;

    /**
     * System callback that will be used to inflate the UI, after the activity has been created.
     *
     * @param savedInstanceState The saved instance state, if the activity is restarted (e.g.
     *                           after a configuration change) or null, otherwise.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Inflate UI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);

        // Retrieve often needed view instances
        this.actionBar = this.getSupportActionBar();

        FragmentManager fragmentManager = getSupportFragmentManager();
        this.gameBoardFragment = (GameBoardFragment) fragmentManager.findFragmentById(R.id.game_activity_game_board_fragment);
        this.gameMatchedWordsFragment = (GameMatchedWordsFragment) fragmentManager.findFragmentById(R.id.game_activity_game_matched_words_fragment);
        this.gameControlsFragment = (GameControlsFragment) fragmentManager.findFragmentById(R.id.game_activity_game_controls_fragment);

        // Set up game state
        Intent intent = this.getIntent();
        long gameUid = intent.getLongExtra("gameUid", -1);

        this.dao = DatabaseHolder.getInstance().gameDao();
        LiveData<GameState> gameStateLiveData = this.dao.selectSingleGameState(gameUid);

        gameStateLiveData.observe(this, gameState -> {
            Log.d("GameActivity", "gameStateLiveData.observe: " + gameState);

            if (gameState != null) {
                this.gameState = gameState;
                this.onGameStateLoaded();
                gameStateLiveData.removeObservers(this);
            }
        });

        // Set up handler for background thread communication
        this.handler = new Handler(this.getMainLooper(), this);

        // Preload database with all known words
        DatabaseThread.getInstance().post(new PreloadKnownWords());
    }

    /**
     * Finish initialization of the UI once the game state has been loaded and is available in
     * this.gameState.
     */
    private void onGameStateLoaded() {
        // Display game type
        this.actionBar.setTitle(GameUtils.formatGameType(this.gameState.game));
        this.updateTimeAndProgressUi();

        // Set up callback for the database thread to get exclusive access on the game state,
        // when matched words have been found. See comments on setThreadMutexCallback() about
        // the reasoning and why that sounds worse than it is.
        this.gameState.setThreadMutexCallback(this::runOnUiThread);
    }

    /**
     * Create options menu for this activity.
     *
     * @param menu The menu to extend
     * @returns always true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.game_options_menu, menu);
        return true;
    }

    /**
     * Get menu items that are used to display the current progress and elapsed time.
     * @param menu Options menu
     * @returns always true
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.elapsedTimeMenuItem = menu.findItem(R.id.action_game_elapsed_time);
        this.progressMenuItem = menu.findItem(R.id.action_game_progress);
        return true;
    }

    /**
     * Display the elapsed time and game progress in the action bar.
     */
    private void updateTimeAndProgressUi() {
        if (this.elapsedTimeMenuItem != null) {
            this.elapsedTimeMenuItem.setTitle(GameUtils.formatElapsedTime(this.gameState.game));
        }

        if (this.progressMenuItem != null) {
            this.progressMenuItem.setTitle(GameUtils.formatProgress(this.gameState.game));
        }
    }

    /**
     * Start game clock, when the activity becomes visible.
     */
    @Override
    protected void onResume() {
        super.onResume();

        this.lastFullSecond = System.currentTimeMillis();
        ClockThread.getInstance().addClientHandler(this.handler);
        ClockThread.getInstance().startClock();
    }

    /**
     * Pause game clock and save the game, when the activity becomes invisible.
     */
    @Override
    protected void onPause() {
        super.onPause();
        ClockThread.getInstance().removeClientHandler(this.handler);
        ClockThread.getInstance().pauseClock();

        this.saveGameState();
    }

    /**
     * Handle messages from other threads sent to the UI thread. Note, that the background threads
     * use unique message numbers, so that one handler callback can be used for them all.
     *
     * @param msg Received message
     * @return true, if the message could be handled
     */
    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case ClockThread.MESSAGE_TICK:
                this.onClockTick();
                return true;
        }

        return false;
    }

    /**
     * Update time and progress every second. Save game every SAVE_GAME_STATE_INTERVAL seconds.
     */
    private void onClockTick() {
        // Wait until the game state becomes ready
        if (this.gameState == null) {
            return;
        }

        // Update time and progress
        long deltaSeconds = (long) Math.floor((System.currentTimeMillis() - this.lastFullSecond) / 1000);
        this.gameState.game.seconds += deltaSeconds;
        this.lastFullSecond += deltaSeconds * 1000;

        this.updateTimeAndProgressUi();

        // Save game every SAVE_GAME_STATE_INTERVAL seconds
        this.savedGameStateAge += 1;

        if (this.savedGameStateAge >= SAVE_GAME_STATE_INTERVAL) {
            this.saveGameState();
            this.savedGameStateAge = 0;
        }
    }

    /**
     * Clone the current game state and save it on the database.
     */
    private void saveGameState() {
        this.gameState.game.saveDate = new Date();
        GameState persistedGameState = new GameState(this.gameState);
        DatabaseThread.getInstance().post(new SaveOrDeleteGame(persistedGameState, SaveOrDeleteGame.Operation.UPDATE));
    }
}