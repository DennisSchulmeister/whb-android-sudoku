package de.wpvs.sudo_ku.activity.menu;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import de.wpvs.sudo_ku.model.GameDatabase;
import de.wpvs.sudo_ku.model.SavedGame;

/**
 *  ViewModel for persistent saved games, that will remain in memory even if the parent
 *  activity is restarted (e.g. due to an orientation change) or paused. Uses a LiveData object
 *  as an observable data container, that updates all currently active observers. The LiveData-
 *  object is automagically provided by a Room database, which also takes care to run the query
 *  in a background task.
 */
public class StartMenuSavedGameViewModel extends AndroidViewModel {
    private GameDatabase gameDatabase;
    private LiveData<List<SavedGame>> savedGames = null;
    private LiveData<Integer> count = null;

    /**
     * Constructor
     */
    public StartMenuSavedGameViewModel(@NonNull Application application) {
        super(application);

        this.gameDatabase = GameDatabase.getInstance(application);
    }

    /**
     * Get the encapsulated GameDatabase-instance so that other database queries than those
     * directly provided here can be issued. This is especially needed to load a single saved
     * game that the user wants to resume or the modify the database contents.
     *
     * @returns a GameDatabase instance
     */
    public GameDatabase getGameDatabase() {
        return this.gameDatabase;
    }

    /**
     * Load all saved games on first access and return the LiveData object wrapping the list.
     * @returns a LiveData wrapped List of SavedGame instances
     */
    public LiveData<List<SavedGame>> getSavedGames() {
        if (this.savedGames == null) {
            this.savedGames = this.gameDatabase.savedGameDAO().selectAll();
        }

        return this.savedGames;
    }

    /**
     * Queries the amount of saved games and returns a LiveData object wrapping the value.
     * @returns a LiveData wrapped Integer value
     */
    public LiveData<Integer> getCount() {
        if (this.count == null) {
            this.count = this.gameDatabase.savedGameDAO().getRowCount();
        }

        return this.count;
    }
}
