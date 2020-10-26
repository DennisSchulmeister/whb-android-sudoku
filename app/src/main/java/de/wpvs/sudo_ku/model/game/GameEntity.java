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
    public int difficulty = 75;
    public int progress = 0;
    public long seconds = 0;

    /**
     * Game types, analogous R.array.game_type_keys
     */
    public enum GameType {
        NUMBER_GAME,
        LETTER_GAME,
    }
}