package de.wpvs.sudo_ku.activity.game;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavGraph;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.activity.NavigationUtils;

/**
 * Activity to show after a game has been completely solved. It shows a nice congratuliations
 * message as well as a button to return to the start menu.
 */
public class GameFinishedActivity extends AppCompatActivity {
    private Button finishButton;

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

        this.finishButton = this.findViewById(R.id.game_finished_finish_button);

        this.finishButton.setOnClickListener(v -> {
            NavigationUtils.gotoStartMenu(this);
        });
    }
}
