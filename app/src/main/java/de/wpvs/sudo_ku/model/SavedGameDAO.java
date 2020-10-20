package de.wpvs.sudo_ku.model;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * Data access object that defines all database queries for saved games.
 */
@Dao
public interface SavedGameDAO {
    /**
     * Insert a new game, replace existing on conflict.
     * @param savedGame Record to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(SavedGame savedGame);

    /**
     * Update an existing game.
     * @param savedGame Record to update
     */
    @Update
    public void update(SavedGame savedGame);

    /**
     * Delete an existing game.
     * @param savedGame Record to delete
     */
    @Delete
    public void delete(SavedGame savedGame);

    /**
     * Selects all saved games.
     * @return All saved games ordered by save data descending
     */
    @Query("SELECT * FROM SavedGame ORDER BY saveDate DESC")
    public LiveData<List<SavedGame>> selectAll();

    /**
     * Selects a single saved game via its ID.
     * @param id ID of the searched game.
     * @return Found record or null
     */
    @Query("SELECT * FROM SavedGame WHERE id = :id")
    public LiveData<SavedGame> selectSingle(String id);

    /**
     * Selects to total amount of saved games
     * @returns Number of saved games
     */
    @Query("SELECT COUNT(*) FROM SavedGame")
    LiveData<Integer> getRowCount();
}
