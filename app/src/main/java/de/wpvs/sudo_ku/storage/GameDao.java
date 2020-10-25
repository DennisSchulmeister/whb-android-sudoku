package de.wpvs.sudo_ku.storage;

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
public interface GameDao {
    /**
     * Insert a new game, replace existing on conflict.
     *
     * @param gameEntity Record to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(GameEntity gameEntity);

    /**
     * Update an existing game.
     *
     * @param gameEntity Record to update
     */
    @Update
    public void update(GameEntity gameEntity);

    /**
     * Delete an existing game.
     *
     * @param gameEntity Record to delete
     */
    @Delete
    public void delete(GameEntity gameEntity);

    /**
     * Selects all saved games.
     *
     * @return All saved games ordered by save data descending
     */
    @Query("SELECT * FROM Game ORDER BY saveDate DESC")
    public LiveData<List<GameEntity>> selectAll();

    /**
     * The same as selectAll() but without wrapping the result in a LiveData object.
     *
     * @return All saved games ordered by save data descending
     */
    @Query("SELECT * FROM Game ORDER BY saveDate DESC")
    public List<GameEntity> selectAllSynchronously();

    /**
     * Selects a single saved game via its ID.
     *
     * @param id ID of the searched game.
     * @return Found record or null
     */
    @Query("SELECT * FROM Game WHERE id = :id")
    public LiveData<GameEntity> selectSingle(String id);

    /**
     * The same as selectSingle() but without wrapping the result in a LiveData object.
     *
     * @return Found record or null
     */
    @Query("SELECT * FROM Game WHERE id = :id")
    public GameEntity selectSingleSynchronously(String id);

    /**
     * Selects to total amount of saved games
     *
     * @returns Number of saved games
     */
    @Query("SELECT COUNT(*) FROM Game")
    LiveData<Integer> getRowCount();

    /**
     * The same as getRowCount() but without wrapping the result in a LiveData object.
     *
     * @returns Number of saved games
     */
    @Query("SELECT COUNT(*) FROM Game")
    Integer getRowCountSynchronously();
}
