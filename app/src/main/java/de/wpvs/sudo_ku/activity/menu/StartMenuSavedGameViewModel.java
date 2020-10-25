package de.wpvs.sudo_ku.activity.menu;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import de.wpvs.sudo_ku.storage.DatabaseHolder;
import de.wpvs.sudo_ku.storage.GameEntity;

/**
 *  ViewModel for persistent saved games, that will remain in memory even if the parent
 *  activity is restarted (e.g. due to an orientation change) or paused. Uses a LiveData object
 *  as an observable data container, that updates all currently active observers. The LiveData-
 *  object is automagically provided by a Room database, which also takes care to run the query
 *  in a background task.
 */
public class StartMenuSavedGameViewModel extends AndroidViewModel {
    private DatabaseHolder databaseHolder;
    private LiveData<List<GameEntity>> gameEntities = null;
    private LiveData<Integer> count = null;

    /**
     * Constructor
     */
    public StartMenuSavedGameViewModel(@NonNull Application application) {
        super(application);

        this.databaseHolder = DatabaseHolder.getInstance();
    }

    /**
     * Get the encapsulated DatabaseHolder-instance so that other database queries than those
     * directly provided here can be issued. This is especially needed to load a single saved
     * game that the user wants to resume or the modify the database contents.
     *
     * @returns a DatabaseHolder instance
     */
    public DatabaseHolder getDatabaseHolder() {
        return this.databaseHolder;
    }

    /**
     * Load all saved games on first access and return the LiveData object wrapping the list.
     * @returns a LiveData wrapped List of GameEntity instances
     */
    public LiveData<List<GameEntity>> getGameEntities() {
        if (this.gameEntities == null) {
            this.gameEntities = this.databaseHolder.gameDao().selectAll();
        }

        return this.gameEntities;
    }

    /**
     * Queries the amount of saved games and returns a LiveData object wrapping the value.
     * @returns a LiveData wrapped Integer value
     */
    public LiveData<Integer> getCount() {
        if (this.count == null) {
            this.count = this.databaseHolder.gameDao().getRowCount();
        }

        return this.count;
    }
}
