package de.wpvs.sudo_ku.storage;

import androidx.room.Entity;
import androidx.room.ForeignKey;

/**
 * Data transfer object for a word on the game board. This is part of the data model used to
 * persist and represent the game state. In this case, when a letter game is played, this entity
 * contains the words that have been found on the game board based on the current value of each
 * field.
 *
 * If a valid word has been found, all fields that form that word, are assigned a word number
 * essentially pointing at this entity. This entity then acts as a separate list of all found
 * words in the game.
 *
 * Note, that if the same word has been found more than once, each occurrence will get its own
 * unique word number and will the stored separately in the list. This way a word can simply be
 * removed from the list without further checks, when the user erases a letter.
 */
@Entity(
        tableName   = "Word",
        primaryKeys = {"gameUid", "wordNumber"},
        foreignKeys = {
                @ForeignKey(
                        entity       = GameEntity.class,
                        parentColumns = "uid",
                        childColumns  = "gameUid",
                        onDelete      = ForeignKey.CASCADE,
                        onUpdate      = ForeignKey.CASCADE
                )
        })
public class WordEntity {
    public long gameUid = -1;
    public int wordNumber = -1;

    public String word = "";
}
