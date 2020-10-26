package de.wpvs.sudo_ku.thread.database;

import java.util.HashMap;
import java.util.Map;

import de.wpvs.sudo_ku.model.DatabaseHolder;
import de.wpvs.sudo_ku.model.dictionary.KnownWordDao;

/**
 * Background operation to query the known word database for matches during the game play.
 * This is running on the database background thread instead of using Room's built-in threading
 * because it is triggered deep inside the game logic, decoupled from the UI. But Room's automatic
 * threading only applies when receiving a LiveData object. This in turn cannot be used in the game
 * logic as there is no simple way to receive the result without memory leaks.
 *
 * Note that this class uses a generic type parameter, so that the caller can pass additional data
 * for each searched word. The data will be completely be opaque to the background task. But it
 * will be passed back to the caller for each successful match.
 *
 * @param <T> Custom data by the client for each word
 */
public class MatchKnownWords<T> implements Runnable {
    private Map<String, T> searchedWords;
    private Callback callback;
    private KnownWordDao dao;

    /**
     * Callback interface used to return the found matches.
     */
    public interface Callback<T> {
        /**
         * Receive result with the matched words. This method will be called after all database
         * operations have finished, even when no match was found.
         *
         * @param matchedWords Map with all positive matches, only
         */
        void receiveResult(Map<String, T> matchedWords);
    }

    /**
     * Constructor.
     * @param searchedWords Map with all searched words.
     */
    public MatchKnownWords(Map<String, T> searchedWords) {
        this.searchedWords = searchedWords;
        this.dao = DatabaseHolder.getInstance().knownWordDao();
    }

    /**
     * Set callback object.
     *
     * @param callback Callback object
     */
    public void setCallback(MatchKnownWords.Callback callback) {
        this.callback = callback;
    }

    /**
     * Execute task.
     */
    @Override
    public void run() {
        if (this.callback == null) {
            return;
        }

        Map<String, T> matchedWords = new HashMap<String, T>();

        for (String word : this.searchedWords.keySet()) {
            if (this.dao.searchSynchronously(word) != 0) {
                matchedWords.put(word, this.searchedWords.get(word));
            }
        }


        this.callback.receiveResult(matchedWords);
    }
}
