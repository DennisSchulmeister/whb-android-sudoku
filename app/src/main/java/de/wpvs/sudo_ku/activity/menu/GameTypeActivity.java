package de.wpvs.sudo_ku.activity.menu;

import androidx.appcompat.app.AppCompatActivity;
import de.wpvs.sudo_ku.R;

import android.os.Bundle;

/**
 * Intermediate activity that will be shown before a new game is started. Here the player is
 * asked what type of game shall be started.
 */
public class GameTypeActivity extends AppCompatActivity {

    /**
     * System callback that will be used to inflate the UI, after the activity has been created.
     *
     * @param savedInstanceState The saved instance state, if the activity is restarted (e.g.
     *                           after a configuration change) or null, otherwise.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_type_activity);
    }

}