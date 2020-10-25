package de.wpvs.sudo_ku.model.dictionary;

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
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(KnownWordEntity knownWordEntity);

    @Query("DELETE FROM KnownWord WHERE word = :word")
    void delete(String word);

    @Query("SELECT COUNT(*) FROM KnownWord WHERE word = :word")
    int searchSynchronously(String word);

    @Query("SELECT * FROM KnownWord ORDER BY word")
    LiveData<List<KnownWordEntity>> selectAll();

    @Query("SELECT * FROM KnownWord ORDER BY word")
    List<KnownWordEntity> selectAllSynchronously();

    @Query("SELECT COUNT(*) FROM KnownWord")
    LiveData<Integer> getRowCount();

    @Query("SELECT COUNT(*) FROM KnownWord")
    Integer getRowCountSynchronously();
}
