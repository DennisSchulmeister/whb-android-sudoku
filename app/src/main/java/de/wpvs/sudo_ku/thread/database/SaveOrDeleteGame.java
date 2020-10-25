package de.wpvs.sudo_ku.thread.database;

import java.util.Map;

import de.wpvs.sudo_ku.storage.DatabaseHolder;
import de.wpvs.sudo_ku.storage.GameDao;
import de.wpvs.sudo_ku.storage.GameState;
import de.wpvs.sudo_ku.storage.GameUtils;

/**
 * Background operation to perform a database operation on a GameEntity instance. This can either
 * be used to insert a new game into the database, update an existing game or delete a game.
 * The operation is meant to be executed in the database background thread as returned by the
 * DatabaseThread class. It does not run any code outside that thread (e.g. in the UI thread
 * for user notification) but relies completely on callback methods for that.
 *
 * When the database operation is INSERT or UPDATE, the same instance can be rerun multiple times,
 * to continuously keep the game in the database up to date.
 */
public class SaveOrDeleteGame implements Runnable {
    private GameState gameState;
    private Operation operation;
    private GameDao dao;

    private Callback callback;

    /**
     * The database operation to perform.
     */
    public enum Operation {
        INSERT, UPDATE, DELETE;
    };

    /**
     * Callback interface used to inform the caller, whether the database update could be
     * performed or errors in the game configuration have prevented this. Note, that the
     * callback methods are running on the database thread.
     */
    public interface Callback {
        /**
         * Database update has been performed.
         */
        void onUpdatePerformed();

        /**
         * Database update could not be performed due to errors in the game object.
         *
         * @param errors Found errors
         */
        void onErrorsFound(Map<GameState.Error, String> errors);
    };

    /**
     * Constructor.
     *
     * @param gameState The game to save or delete
     */
    public SaveOrDeleteGame(GameState gameState, Operation operation) {
        this.gameState = gameState;
        this.operation = operation;

        this.dao = DatabaseHolder.getInstance().gameDao();
    }

    /**
     * Set callback object.
     *
     * @param callback Callback object
     */
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    /**
     * Perform task.
     */
    @Override
    public void run() {
        // Check consistency, in case the game shall be saved
        if (this.operation == Operation.INSERT || this.operation == Operation.UPDATE) {
            Map<GameState.Error, String> errors = this.gameState.checkGameConsistency();

            if (!errors.isEmpty()) {
                if (this.callback != null) {
                    this.callback.onErrorsFound(errors);
                }

                return;
            }
        }

        // Perform requested database operation
        switch (this.operation) {
            case INSERT:
                this.dao.insert(this.gameState);
                this.operation = Operation.UPDATE;
                break;
            case UPDATE:
                this.dao.update(this.gameState);
                break;
            case DELETE:
                this.dao.delete(this.gameState.game.uid);
                break;
        }

        if (this.callback != null) {
            this.callback.onUpdatePerformed();
        }
    }
}
