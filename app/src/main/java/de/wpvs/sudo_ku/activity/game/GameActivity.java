package de.wpvs.sudo_ku.activity.game;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.model.DatabaseHolder;
import de.wpvs.sudo_ku.model.game.GameDao;
import de.wpvs.sudo_ku.model.game.GameState;
import de.wpvs.sudo_ku.thread.BackgroundThreadHolder;
import de.wpvs.sudo_ku.thread.clock.ClockThread;
import de.wpvs.sudo_ku.thread.database.DatabaseThread;
import de.wpvs.sudo_ku.thread.database.PreloadKnownWords;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

/**
 * Main activity of an ongoing game. It shows the game board and handles the UI part of the
 * game logic.
 */
public class GameActivity extends AppCompatActivity implements Handler.Callback {
    private GameDao dao;
    private GameState gameState;
    private Handler handler;

    private TextView idTextView;

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
        this.idTextView = this.findViewById(R.id.activity_game_id);

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
        this.idTextView.setText("UID: " + this.gameState.game.uid);
    }

    /**
     * Start game clock, when the activity becomes visible.
     */
    @Override
    protected void onResume() {
        super.onResume();
        ClockThread.getInstance().addClientHandler(this.handler);
        ClockThread.getInstance().startClock();
    }

    /**
     * Pause game clock, when the activity becomes invisible.
     */
    @Override
    protected void onPause() {
        super.onPause();
        ClockThread.getInstance().removeClientHandler(this.handler);
        ClockThread.getInstance().pauseClock();
    }

    /**
     * Handle messages from othre threads sent to the UI thread. Note, that the background threads
     * use unique message numbes, so that one handler callback can be used for them all.
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
     *
     */
    private void onClockTick() {
        // TODO
    }
}