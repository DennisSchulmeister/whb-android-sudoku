package de.wpvs.sudo_ku.menu;

import androidx.appcompat.app.AppCompatActivity;
import de.wpvs.sudo_ku.R;

import android.os.Bundle;

/**
 * First visible activity after starting the app. It shows the logo and the game menu. The menu
 * consists of ongoing games (in the spirit of "ls") plus a floating button to start a new game.
 */
public class StartMenuActivity extends AppCompatActivity {

    /**
     * System callback that will be used to inflate the UI, after the activity has been created.
     *
     * @param savedInstanceState The saved instance state, if the activity is restarted (e.g.
     *                           after a configuration change) or null, otherwise.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_menu_activity);
    }

}