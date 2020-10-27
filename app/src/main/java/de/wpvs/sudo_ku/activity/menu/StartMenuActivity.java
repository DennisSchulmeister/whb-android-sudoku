package de.wpvs.sudo_ku.activity.menu;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.activity.NavigationUtils;
import de.wpvs.sudo_ku.model.DatabaseHolder;
import de.wpvs.sudo_ku.model.game.GameEntity;
import de.wpvs.sudo_ku.model.game.GameState;
import de.wpvs.sudo_ku.thread.database.DatabaseThread;
import de.wpvs.sudo_ku.thread.database.DeleteAllGames;
import de.wpvs.sudo_ku.thread.BackgroundThreadHolder;
import de.wpvs.sudo_ku.thread.database.SaveOrDeleteGame;

/**
 * First visible activity after starting the app. It shows the logo and the game menu. The menu
 * consists of ongoing games (in the spirit of "ls") plus a floating button to start a new game.
 */
public class StartMenuActivity extends AppCompatActivity {
    private Bundle savedInstanceState;

    private TextView noSavedGamesMessage;
    private RecyclerView gameEntityList;
    private FloatingActionButton floatingActionButton;

    private StartMenuSavedGameViewModel startMenuSavedGameViewModel;
    private StartMenuSavedGameRecyclerViewAdapter startMenuSavedGameRecyclerViewAdapter;
    private List<GameEntity> gameEntities = null;

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

        this.savedInstanceState = savedInstanceState;

        // Retrieve often needed view instances
        this.noSavedGamesMessage = this.findViewById(R.id.start_menu_no_saved_games_message);
        this.gameEntityList = this.findViewById(R.id.start_menu_game_entity_list);
        this.floatingActionButton = this.findViewById(R.id.start_menu_new_game_fab);

        // Retrieve the ViewModel for saved games and create RecyclerView.Adapter
        this.startMenuSavedGameRecyclerViewAdapter = new StartMenuSavedGameRecyclerViewAdapter();
        this.gameEntityList.setAdapter(this.startMenuSavedGameRecyclerViewAdapter);
        this.gameEntityList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Toggle between list or message depending on the amount of saved games
        this.startMenuSavedGameViewModel = new ViewModelProvider(this).get(StartMenuSavedGameViewModel.class);

        this.startMenuSavedGameViewModel.getCount().observe(this, count -> {
            this.noSavedGamesMessage.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
            this.gameEntityList.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        });

        this.startMenuSavedGameViewModel.getGameEntities().observe(this, savedGames -> {
            this.gameEntities = savedGames;
            this.startMenuSavedGameRecyclerViewAdapter.setGameEntities(savedGames);
        });

        // Start existing game on click on the list
        this.startMenuSavedGameRecyclerViewAdapter.setClickListener(savedGame -> {
            if (savedGame.progress < 100) {
                NavigationUtils.gotoSavedGame(this, savedGame.uid);
            } else {
                NavigationUtils.gotoFinished(this, savedGame.uid);
            }
        });

        // Start new game on click on the floating action button
        this.floatingActionButton.setOnClickListener(v -> {
            NavigationUtils.gotoNewGame(this);
        });
    }

    /**
     * Quit all background thread when this activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        BackgroundThreadHolder.getInstance().quitSafely();
    }

    /**
     * Create options menu for this activity.
     *
     * @param menu The menu to extend
     * @returns always true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.start_menu_options_menu, menu);
        return true;
    }

    /**
     * Perform action selected in the options menu.
     *
     * @param item Selected menu item
     * @returns true, if the item has been handled
     *
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start_menu_create_random:
                this.startRandomGame();
                break;
            case R.id.action_start_menu_delete_all:
                this.deleteAllGames();
                break;
            case R.id.action_start_menu_tldr_sudo:
                NavigationUtils.gotoTldrPage(this, "sudo");
                break;
            case R.id.action_start_menu_website:
                NavigationUtils.gotoWebsite(this, "https://www.wpvs.de");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    /**
     * Save a new random game in the database and launch it.
     */
    private void startRandomGame() {
        GameState gameState = GameState.createRandomGame();
        SaveOrDeleteGame task = new SaveOrDeleteGame(gameState, SaveOrDeleteGame.Operation.INSERT);

        task.setCallback(new SaveOrDeleteGame.Callback() {
            @Override
            public void onUpdatePerformed() {
                NavigationUtils.gotoSavedGame(StartMenuActivity.this, gameState.game.uid);
            }

            @Override
            public void onErrorsFound(Map<GameState.Error, String> errors) {
                // Shouldn't happen, but anyway …
                String messages = "";

                for (String message : errors.values()) {
                    if (messages.isEmpty()) {
                        messages = message;
                    } else {
                        messages += " " + message;
                    }
                }

                Toast.makeText(StartMenuActivity.this, messages, Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseThread.getInstance().post(task);
    }

    /**
     * Delete all saved games from the database.
     */
    private void deleteAllGames() {
        DeleteAllGames task = new DeleteAllGames(this, this.gameEntityList, this.savedInstanceState);

        task.setCallback(new DeleteAllGames.Callback() {
            @Override
            public void beforeDeletion() {
                // Temporarily clear the list, to make it look like the games have already been deleted
                startMenuSavedGameRecyclerViewAdapter.setGameEntities(new ArrayList<>());
            }

            @Override
            public void afterDeletion() {
                // Restore the real list, once the task has been cancelled or completed
                startMenuSavedGameRecyclerViewAdapter.setGameEntities(gameEntities);
            }
        });

        DatabaseThread.getInstance().post(task);
    }
}