package de.wpvs.sudo_ku.activity.game;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.activity.NavigationUtils;

/**
 * Activity to show after a game has been completely solved. It shows a nice congratuliations
 * message as well as a button to return to the start menu.
 */
public class GameFinishedActivity extends AppCompatActivity {
    /**
     * System callback that will be used to inflate the UI, after the activity has been created.
     *
     * @param savedInstanceState The saved instance state, if the activity is restarted (e.g.
     *                           after a configuration change) or null, otherwise.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_finished_activity);

        Intent intent = this.getIntent();
        long gameUid = intent.getLongExtra("gameUid", -1);

        Button newGameButton = this.findViewById(R.id.game_finished_new_game_button);
        Button backToGameButton = this.findViewById(R.id.game_finished_back_to_game_button);

        newGameButton.setOnClickListener(v -> {
            NavigationUtils.gotoStartMenu(this);
        });

        backToGameButton.setOnClickListener(v -> {
            NavigationUtils.gotoSavedGame(this, gameUid);
        });
    }
}
