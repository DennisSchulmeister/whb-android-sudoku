package de.wpvs.sudo_ku.model;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Data transfer object for a saved game. This is used to persist a started game in the database,
 * so that it can be resumed even days or weeks after the game has started.
 */
@Entity
public class SavedGame {
    @NonNull
    @PrimaryKey
    private String id;

    private Date startDate;
    private Date saveDate;
    private GameType gameType;
    private float difficulty;
    private float progress;
    private String serializedGameBoard;

    /**
     * Default constructor.
     */
    public SavedGame() {
    }

    /**
     * Constructor for quick creation of new records.
     *
     * @param startDate Time, when the game was started
     * @param saveDate Time, when the game was last saved
     * @param gameType Variant/type of game
     * @param difficulty Difficulty in percent from 0 to 1
     * @param progress Game progress in percent from 0 to 1
     * @param serializedGameBoard Serialized game board content
     */
    public SavedGame(Date startDate, Date saveDate, GameType gameType, float difficulty, float progress, String serializedGameBoard) {
        this.startDate = startDate;
        this.saveDate = saveDate;
        this.gameType = gameType;
        this.difficulty = difficulty;
        this.progress = progress;
        this.serializedGameBoard = serializedGameBoard;
    }

    /**
     * Constructor for recreating saved records.
     *
     * @param id Record ID
     * @param startDate Time, when the game was started
     * @param saveDate Time, when the game was last saved
     * @param gameType Variant/type of game
     * @param difficulty Difficulty in percent from 0 to 1
     * @param progress Game progress in percent from 0 to 1
     * @param serializedGameBoard Serialized game board content
     */
    public SavedGame(@NonNull String id, Date startDate, Date saveDate, GameType gameType, float difficulty, float progress, String serializedGameBoard) {
        this.id = id;
        this.startDate = startDate;
        this.saveDate = saveDate;
        this.gameType = gameType;
        this.difficulty = difficulty;
        this.progress = progress;
        this.serializedGameBoard = serializedGameBoard;
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

    public String getSerializedGameBoard() {
        return serializedGameBoard;
    }

    public void setSerializedGameBoard(String serializedGameBoard) {
        this.serializedGameBoard = serializedGameBoard;
    }
}
