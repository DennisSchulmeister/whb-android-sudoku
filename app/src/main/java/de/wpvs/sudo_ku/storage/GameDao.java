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
    void insert(GameEntity gameEntity);

    /**
     * Update an existing game.
     *
     * @param gameEntity Record to update
     */
    @Update
    void update(GameEntity gameEntity);

    /**
     * Delete an existing game.
     *
     @param uid ID of the game to delete.
     */
    //@Delete
    @Query("DELETE FROM Game WHERE uid = :uid")
    void delete(int uid);

    /**
     * Selects all saved games.
     *
     * @return All saved games ordered by save data descending
     */
    @Query("SELECT * FROM Game ORDER BY saveDate DESC")
    LiveData<List<GameEntity>> selectAll();

    /**
     * The same as selectAll() but without wrapping the result in a LiveData object.
     *
     * @return All saved games ordered by save data descending
     */
    @Query("SELECT * FROM Game ORDER BY saveDate DESC")
    List<GameEntity> selectAllSynchronously();

    /**
     * Selects a single saved game via its ID.
     *
     * @param uid ID of the searched game.
     * @return Found record or null
     */
    @Query("SELECT * FROM Game WHERE uid = :uid")
    LiveData<GameEntity> selectSingle(int uid);

    /**
     * The same as selectSingle() but without wrapping the result in a LiveData object.
     *
     * @param uid ID of the searched game.
     * @return Found record or null
     */
    @Query("SELECT * FROM Game WHERE uid = :uid")
    GameEntity selectSingleSynchronously(int uid);

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
