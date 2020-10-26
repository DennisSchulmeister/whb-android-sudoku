package de.wpvs.sudo_ku.model.game;

import android.transition.ChangeTransform;

import java.util.ArrayList;
import java.util.List;

import androidx.room.Entity;
import androidx.room.ForeignKey;

/**
 * Data transfer object for a field on the game board.
 */
@Entity(
        tableName   = "CharacterField",
        primaryKeys = {"gameUid", "xPos", "yPos"},
        foreignKeys = {
                @ForeignKey(
                        entity        = GameEntity.class,
                        parentColumns = "uid",
                        childColumns  = "gameUid",
                        onDelete      = ForeignKey.CASCADE,
                        onUpdate      = ForeignKey.CASCADE
                )
        })
public class CharacterFieldEntity {
    public long gameUid = -1;
    public int xPos = -1;
    public int yPos = -1;

    public String character = "";
    public List<String> pencil = new ArrayList<>();
    public List<Integer> words = new ArrayList<>();
    public boolean locked = false;

    /**
     * Default constructor.
     */
    public CharacterFieldEntity() {
    }

    /**
     * Copy constructor. Creates a deep clone of the given entity.
     *
     * @param that Entity to copy
     */
    public CharacterFieldEntity(CharacterFieldEntity that) {
        this.gameUid   = that.gameUid;
        this.xPos      = that.xPos;
        this.yPos      = that.yPos;
        this.character = that.character;
        this.locked    = that.locked;

        this.pencil.addAll(that.pencil);
        this.words.addAll(that.words);
    }
}
