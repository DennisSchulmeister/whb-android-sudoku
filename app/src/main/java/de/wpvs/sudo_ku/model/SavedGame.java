package de.wpvs.sudo_ku.model;

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
@Entity
public class SavedGame {
    @NonNull
    @PrimaryKey
    private String id = UUID.randomUUID().toString();

    private Date startDate = new Date();
    private Date saveDate = new Date();
    private GameType gameType = GameType.NUMBER_GAME;
    private int size = 9;
    private List<String> characterSet = new ArrayList<>();
    private float difficulty = 0.5f;
    private float progress = 0.0f;
    private long seconds = 0;
    private String serializedGameBoard = "";

    /**
     * Error codes, why a game is inconsistent and should not be saved.
     */
    public enum Error {
        ERROR_CHARSET_SIZE,
    };

    /**
     * Default constructor for a new game. Use the setter methods to set the game configuration,
     * before saving it.
     */
    public SavedGame() {
    }

     /**
     * Check that all parameters are consistent and the game is safe to be saved. Note, that this
     * currently only checks that the amount of available characters matches the game board size.
     * Especially it is not checked, that every parameters has a value, since each parameter has
     * an assigned default value, that is set when the new SavedGame object is created.
     *
     * @return A list of all found errors
     */
    public Map<Error, String> checkConsistency() {
        Map<Error, String> errors = new HashMap<>();
        Context context = MyApplication.getInstance();

        if (this.characterSet == null || this.characterSet.size() != this.size) {
            String message = "";

            switch (this.gameType) {
                case NUMBER_GAME:
                    message = context.getString(R.string.new_game_error_wrong_amount_of_numbers, this.size);
                    break;
                case LETTER_GAME:
                    message = context.getString(R.string.new_game_error_wrong_amount_of_letters, this.size);
                    break;
            }

            errors.put(Error.ERROR_CHARSET_SIZE, message);
        }

        return errors;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getSaveDate() {
        return saveDate;
    }

    public void setSaveDate(Date saveDate) {
        this.saveDate = saveDate;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<String> getCharacterSet() {
        return characterSet;
    }

    public void setCharacterSet(List<String> characterSet) {
        this.characterSet = characterSet;
    }

    public float getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public String getSerializedGameBoard() {
        return serializedGameBoard;
    }

    public void setSerializedGameBoard(String serializedGameBoard) {
        this.serializedGameBoard = serializedGameBoard;
    }
}
