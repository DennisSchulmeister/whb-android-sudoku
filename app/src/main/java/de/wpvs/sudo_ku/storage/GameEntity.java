package de.wpvs.sudo_ku.storage;

import android.content.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import de.wpvs.sudo_ku.MyApplication;
import de.wpvs.sudo_ku.R;

/**
 * Data transfer object for a saved game. This is used to persist a started game in the database,
 * so that it can be resumed even days or weeks after the game has started.
 */
@Entity(tableName = "Game")
public class GameEntity {
    @NonNull
    @PrimaryKey
    public String id = UUID.randomUUID().toString();

    public Date startDate = new Date();
    public Date saveDate = new Date();
    public GameType gameType = GameType.NUMBER_GAME;
    public int size = 9;
    public List<String> characterSet = new ArrayList<>();
    public float difficulty = 0.5f;
    public float progress = 0.0f;
    public long seconds = 0;

    /**
     * Game types, analogous R.array.game_type_keys
     */
    public enum GameType {
        NUMBER_GAME,
        LETTER_GAME,
    }

    /**
     * Error codes, why a game is inconsistent and should not be saved.
     */
    public enum Error {
        ERROR_CHARSET_SIZE,
    }
}
