package de.wpvs.sudo_ku.game;

import androidx.appcompat.app.AppCompatActivity;
import de.wpvs.sudo_ku.R;

import android.os.Bundle;

/**
 * Main activity of an ongoing game. It shows the game board and handles the UI part of the
 * game logic.
 */
public class GameActivity extends AppCompatActivity {

    /**
     * System callback that will be used to inflate the UI, after the activity has been created.
     *
     * @param savedInstanceState The saved instance state, if the activity is restarted (e.g.
     *                           after a configuration change) or null, otherwise.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);
    }
}