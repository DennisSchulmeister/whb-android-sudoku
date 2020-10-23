package de.wpvs.sudo_ku.model;

import java.util.List;

import androidx.lifecycle.LifecycleOwner;

/**
 * Utility class with static methods to help with debugging the app.
 */
public class DebugUtils {
    /**
     * Start a new background thread and create a new dummy game record in the database.
     *
     * @param gameDatabase The GameDatabase instance to use
     * @returns the newly persisted SavedGame instance
     */
    public static SavedGame createAndSaveDummyGame(GameDatabase gameDatabase) {
        SavedGameDAO dao = gameDatabase.savedGameDAO();

        SavedGame savedGame = new SavedGame();
        savedGame.setGameType(GameType.NUMBER_GAME);
        savedGame.setSize(9);
        savedGame.setDifficulty(0.5f);
        savedGame.setProgress(0.33f);
        savedGame.setSeconds(345);

        new Thread(() -> {
            dao.insert(savedGame);
        }).start();

        return savedGame;
    }

    /**
     * Start a new background thread and delete all existing saved games.
     *
     * @param owner Calling activity of lifecycle owner
     * @param gameDatabase The GameDatabase instance to use.
     */
    public static void deleteAllGames(LifecycleOwner owner, GameDatabase gameDatabase) {
        SavedGameDAO dao = gameDatabase.savedGameDAO();

        new Thread(() -> {
            List<SavedGame> savedGames = dao.selectAllSynchronously();

            for (SavedGame savedGame : savedGames) {
                dao.delete(savedGame);
            }
        }).start();
    }
}
