package de.wpvs.sudo_ku.thread.database;

import java.util.Map;

import de.wpvs.sudo_ku.storage.DatabaseHolder;
import de.wpvs.sudo_ku.storage.GameDao;
import de.wpvs.sudo_ku.storage.GameEntity;
import de.wpvs.sudo_ku.storage.StorageUtils;

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
    private GameEntity gameEntity;
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
        void onErrorsFound(Map<GameEntity.Error, String> errors);
    };

    /**
     * Constructor.
     *
     * @param gameEntity The game to save or delete
     */
    public SaveOrDeleteGame(GameEntity gameEntity, Operation operation) {
        this.gameEntity = gameEntity;
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
            Map<GameEntity.Error, String> errors = StorageUtils.checkGameConsistency(this.gameEntity);

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
                this.dao.insert(this.gameEntity);
                this.operation = Operation.UPDATE;
                break;
            case UPDATE:
                this.dao.update(this.gameEntity);
                break;
            case DELETE:
                this.dao.delete(this.gameEntity);
                break;
        }

        if (this.callback != null) {
            this.callback.onUpdatePerformed();
        }
    }
}
