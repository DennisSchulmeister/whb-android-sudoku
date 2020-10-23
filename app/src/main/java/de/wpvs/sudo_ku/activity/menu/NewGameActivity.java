package de.wpvs.sudo_ku.activity.menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import de.wpvs.sudo_ku.R;

import android.os.Bundle;
import android.view.MenuItem;

/**
 * Intermediate activity that will be shown before a new game is started. Here the player is
 * asked what type of game shall be started.
 */
public class NewGameActivity extends AppCompatActivity {
    /**
     * System callback that will be used to inflate the UI, after the activity has been created.
     *
     * @param savedInstanceState The saved instance state, if the activity is restarted (e.g.
     *                           after a configuration change) or null, otherwise.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game_activity);

        // Set custom theme, to make the summary texts visible
        this.setTheme(R.style.Theme_Sudoku_Preferences);

        // Display back button in the action bar

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Handle back button in the action bar
     *
     * @param item Pressed menu item
     * @returns true, when handled
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}