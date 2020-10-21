package de.wpvs.sudo_ku.model;

import java.util.Date;
import java.util.UUID;

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
    private String id = UUID.randomUUID().toString();

    private Date startDate = new Date();
    private Date saveDate = new Date();
    private GameType gameType = GameType.NUMBER_QUIZ;
    private int size = 3;
    private String[] characters = new String[0];
    private float difficulty = 0.5f;
    private float progress = 0.0f;
    private long seconds = 0;
    private String serializedGameBoard = "";

    /**
     * Default constructor.
     */
    public SavedGame() {
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

    public String[] getCharacters() {
        return characters;
    }

    public void setCharacters(String[] characters) {
        this.characters = characters;
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
