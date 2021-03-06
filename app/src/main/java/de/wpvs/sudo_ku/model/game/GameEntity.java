package de.wpvs.sudo_ku.model.game;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Data transfer object for head data of a saved game.
 */
@Entity(tableName = "Game")
public class GameEntity {
    @PrimaryKey(autoGenerate = true)
    public long uid;

    public Date startDate = new Date();
    public Date saveDate = new Date();
    public GameType gameType = GameType.NUMBER_GAME;
    public int size = 9;
    public List<String> characterSet = new ArrayList<>();
    public int prefill = 5;
    public boolean lockPrefilled = true;
    public int progress = 0;
    public long seconds = 0;

    /**
     * Game types, analogous R.array.game_type_keys
     */
    public enum GameType {
        NUMBER_GAME,
        LETTER_GAME,
    }

    /**
     * Default constructor.
     */
    public GameEntity () {
    }

    /**
     * Copy constructor. Creates a deep clone of the given entity.
     *
     * @param that Entity to copy
     */
    public GameEntity(GameEntity that) {
        this.uid           = that.uid;
        this.startDate     = new Date(that.startDate.getTime());
        this.saveDate      = new Date(that.saveDate.getTime());
        this.gameType      = that.gameType;
        this.size          = that.size;
        this.prefill       = that.prefill;
        this.lockPrefilled = that.lockPrefilled;
        this.progress      = that.progress;
        this.seconds       = that.seconds;

        this.characterSet.addAll(that.characterSet);
    }
}
