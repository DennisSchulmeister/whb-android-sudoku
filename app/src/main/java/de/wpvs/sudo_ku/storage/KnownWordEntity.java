package de.wpvs.sudo_ku.storage;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Data transfer object for a known word, that will be recognized during the letter game. For this
 * the word will be searched with a database query.
 */
@Entity(tableName = "KnownWord")
public class KnownWordEntity {
    @PrimaryKey
    @NonNull
    public String word = "";
}
