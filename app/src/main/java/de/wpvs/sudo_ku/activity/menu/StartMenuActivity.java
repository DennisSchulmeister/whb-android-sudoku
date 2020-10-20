package de.wpvs.sudo_ku.activity.menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.model.SavedGameViewModel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * First visible activity after starting the app. It shows the logo and the game menu. The menu
 * consists of ongoing games (in the spirit of "ls") plus a floating button to start a new game.
 */
public class StartMenuActivity extends AppCompatActivity {
    private TextView noSavedGamesMessage;
    private RecyclerView savedGamesList;
    private FloatingActionButton floatingActionButton;

    private SavedGameViewModel savedGameViewModel;

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

        // Retrieve often needed view instances
        this.noSavedGamesMessage = this.findViewById(R.id.start_menu_no_saved_games_message);
        this.savedGamesList = this.findViewById(R.id.start_menu_saved_games_list);
        this.floatingActionButton = this.findViewById(R.id.start_menu_new_game_fab);

        // Retrieve the ViewModel for saved games
        this.savedGameViewModel = new ViewModelProvider(this).get(SavedGameViewModel.class);

        // Toggle between list or message depending on the amount of saved games
        this.savedGameViewModel.getCount().observe(this, count -> {
            this.noSavedGamesMessage.setVisibility(count > 0 ? View.INVISIBLE : View.VISIBLE);
            this.savedGamesList.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
        });

        // Start new game on click on the floating action button
        this.floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GameTypeActivity.class);
            this.startActivity(intent);
        });
    }

}