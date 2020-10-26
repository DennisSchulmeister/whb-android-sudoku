package de.wpvs.sudo_ku.activity.game;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.model.DatabaseHolder;
import de.wpvs.sudo_ku.model.game.GameDao;
import de.wpvs.sudo_ku.model.game.GameState;
import de.wpvs.sudo_ku.model.game.GameUtils;
import de.wpvs.sudo_ku.thread.clock.ClockThread;
import de.wpvs.sudo_ku.thread.database.DatabaseThread;
import de.wpvs.sudo_ku.thread.database.PreloadKnownWords;
import de.wpvs.sudo_ku.thread.database.SaveOrDeleteGame;

/**
 * Main activity of an ongoing game. This is the top-level user interface for a running game.
 * But rather than handling all UI interactions itself, most tasks are delegated to a set of
 * loosely coupled fragments interconnected through the shared game logic object and the
 * possibility to send exchange simple messages amongst each other.
 *
 * The most important things driven by this class are:
 *
 *   » Displaying the game name in the application header
 *   » Displaying the elapsed time and progress in the application header
 *   » Updating the game state to advance the elapsed time every second
 *   » Regularly persisting the current game state in the database
 *   » Providing an opaque implementation for message exchange with the fragments
 */
public class GameActivity extends AppCompatActivity implements Handler.Callback {
    private static final int SAVE_GAME_STATE_INTERVAL = 30;
    private int savedGameStateAge = 0;
    private long lastFullSecond = 0;

    private ActionBar actionBar;
    private MenuItem elapsedTimeMenuItem;
    private MenuItem progressMenuItem;

    private final List<GameStateClient> gameStateClients = new LinkedList<>();

    private final GameStateClient.GameMessageExchange gameMessageExchange = new GameStateClient.GameMessageExchange() {
        /**
         * Send empty message to all fragments.
         *
         * @param what Message code (see constants)
         */
        @Override
        public void sendEmptyMessage(int what) {
            for (GameStateClient gameStateClient : GameActivity.this.gameStateClients) {
                gameStateClient.onGameStateMessage(what, -1, -1);
            }
        }

        /**
         * Send a message with field coordinates to all fragments.
         *
         * @param what Message code (see constants)
         * @param xPos Horizontal coordinate
         * @param yPos Vertical coordinate
         */
        @Override
        public void sendFieldMessage(int what, int xPos, int yPos) {
            for (GameStateClient gameStateClient : GameActivity.this.gameStateClients) {
                gameStateClient.onGameStateMessage(what, xPos, yPos);
            }
        }
    };

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

        // Set up game state
        Intent intent = this.getIntent();
        long gameUid = intent.getLongExtra("gameUid", -1);

        GameDao dao = DatabaseHolder.getInstance().gameDao();
        LiveData<GameState> gameStateLiveData = dao.selectSingleGameState(gameUid);

        gameStateLiveData.observe(this, gameState -> {
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
        this.gameState.setThreadMutexCallback(blockThread -> {
            this.runOnUiThread(() -> {
                blockThread.run();
                this.gameMessageExchange.sendEmptyMessage(GameStateClient.MESSAGE_REFRESH_VIEWS);
            });
        });

        // Hand over the game state to the fragments
        FragmentManager fragmentManager = this.getSupportFragmentManager();

        for (Fragment fragment : fragmentManager.getFragments()) {
            if (!(fragment instanceof GameStateClient)) {
                continue;
            }

            GameStateClient gameStateClient = (GameStateClient) fragment;
            this.gameStateClients.add(gameStateClient);

            gameStateClient.setGameState(this.gameState, this.gameMessageExchange);
        }

        this.gameMessageExchange.sendEmptyMessage(GameStateClient.MESSAGE_REFRESH_VIEWS);
    }

    /**
     * Create options menu for this activity.
     *
     * @param menu The menu to extend
     * @return always true
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
     * @return always true
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
        if (msg.what == ClockThread.MESSAGE_TICK) {
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
        long deltaSeconds = (long) Math.floor((System.currentTimeMillis() - this.lastFullSecond) / 1000.0);
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