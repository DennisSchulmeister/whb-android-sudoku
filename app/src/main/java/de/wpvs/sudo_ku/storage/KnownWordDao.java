package de.wpvs.sudo_ku.storage;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * Data access object that defines all database to search and edit the list of known words for
 * the letter game.
 */
@Dao
public interface KnownWordDao {
    /**
     * Insert a new word into the list
     *
     * @param knownWordEntity New word to insert
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(KnownWordEntity knownWordEntity);

    /**
     * Delete an existing word from the list
     *
     * @param word Word to delete
     */
    @Query("DELETE FROM KnownWord WHERE word = :word")
    void delete(String word);

    /**
     * Check, whether a specific word is contained in the list.
     *
     * @param word Searched word
     * @return Amount of occurrences (zero ore one)
     */
    @Query("SELECT COUNT(*) FROM KnownWord WHERE word = :word")
    int searchSynchronously(String word);

    /**
     * Selects all known words.
     *
     * @return All known words alphabetically sorted
     */
    @Query("SELECT * FROM KnownWord ORDER BY word")
    LiveData<List<KnownWordEntity>> selectAll();

    /**
     * The same as selectAll() but without wrapping the result in a LiveData object.
     *
     * @return All known words alphabetically sorted
     */
    @Query("SELECT * FROM KnownWord ORDER BY word")
    List<KnownWordEntity> selectAllSynchronously();

    /**
     * Selects to total amount of known words
     *
     * @returns Number of known words
     */
    @Query("SELECT COUNT(*) FROM KnownWord")
    LiveData<Integer> getRowCount();

    /**
     * The same as getRowCount() but without wrapping the result in a LiveData object.
     *
     * @returns Number of known words
     */
    @Query("SELECT COUNT(*) FROM KnownWord")
    Integer getRowCountSynchronously();
}
