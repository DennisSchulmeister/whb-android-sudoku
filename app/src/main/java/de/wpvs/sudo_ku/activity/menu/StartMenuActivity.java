package de.wpvs.sudo_ku.activity.menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.activity.game.GameActivity;
import de.wpvs.sudo_ku.model.DebugUtils;
import de.wpvs.sudo_ku.model.GameDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private StartMenuSavedGameViewModel startMenuSavedGameViewModel;
    private StartMenuSavedGameRecyclerViewAdapter startMenuSavedGameRecyclerViewAdapter;

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

        // Retrieve the ViewModel for saved games and create RecyclerView.Adapter
        this.startMenuSavedGameRecyclerViewAdapter = new StartMenuSavedGameRecyclerViewAdapter();
        this.savedGamesList.setAdapter(this.startMenuSavedGameRecyclerViewAdapter);
        this.savedGamesList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Toggle between list or message depending on the amount of saved games
        this.startMenuSavedGameViewModel = new ViewModelProvider(this).get(StartMenuSavedGameViewModel.class);

        this.startMenuSavedGameViewModel.getCount().observe(this, count -> {
            this.noSavedGamesMessage.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
            this.savedGamesList.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        });

        this.startMenuSavedGameViewModel.getSavedGames().observe(this, savedGames -> {
            this.startMenuSavedGameRecyclerViewAdapter.setSavedGames(savedGames);
        });

        // Start existing game on click on the list
        this.startMenuSavedGameRecyclerViewAdapter.setClickListener(savedGame -> {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("SavedGameId", savedGame.getId());
            this.startActivity(intent);
        });

        // Start new game on click on the floating action button
        this.floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GameTypeActivity.class);
            this.startActivity(intent);
        });
    }

    /**
     * Create options menu for this activity.
     *
     * NOTE: This is currently only used to create some debugging menu entries to test the
     * RecyclerView and database storage.
     *
     * @param menu The menu to extend
     * @returns always true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        boolean debug = true;       // Set to true, to enable debugging menu

        if (debug) {
            MenuInflater menuInflater = this.getMenuInflater();
            menuInflater.inflate(R.menu.debug, menu);
        }

        return true;
    }

    /**
     * Perform action selected in the options menu.
     *
     * NOTE: This currently only used for some debugging menu entries to test the RecyclerView
     * and database storage.
     *
     * @param item Selected menu item
     * @returns true, if the item has been handled
     *
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        GameDatabase gameDatabase = this.startMenuSavedGameViewModel.getGameDatabase();

        switch (item.getItemId()) {
            case R.id.action_debug_create_game:
                DebugUtils.createAndSaveDummyGame(gameDatabase);
                break;
            case R.id.action_debug_delete_all:
                DebugUtils.deleteAllGames(this, gameDatabase);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}