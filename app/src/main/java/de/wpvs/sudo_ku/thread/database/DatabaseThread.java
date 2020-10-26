package de.wpvs.sudo_ku.thread.database;

import de.wpvs.sudo_ku.thread.BackgroundThread;
import de.wpvs.sudo_ku.thread.BackgroundThreadHolder;

/**
 * Background thread to be used for all database operations, that are not automatically put in
 * a thread by the Room persistence framework. The implication of this is, that it is okay to
 * directly use the DAO classes to query the database from the UI thread (if one of the methods
 * returning a LiveSet is used), but any changing operation must be encapsulated in a Runnable
 * object posted to this thread.
 *
 * To make things easier, this package contains predefined Runnables for all changing database
 * operations of the app.
 */
public class DatabaseThread extends BackgroundThread {
    private static final String NAME = "database";

    /**
     * Don't allow direct instantiation of this class from clients. We rather want to use the
     * global BackgroundTheadManager obejct to maintain a singleton instance.
     */
    private DatabaseThread() {
        super(NAME);
    }

    /**
     * Get the singleton instance of this thread. Creates and starts the thread if necessary.
     *
     * @return background thread for database operations
     */
    public static BackgroundThread getInstance() {
        BackgroundThreadHolder backgroundThreadHolder = BackgroundThreadHolder.getInstance();
        BackgroundThread instance = backgroundThreadHolder.getThread(NAME);

        if (instance == null) {
            instance = new DatabaseThread();
            backgroundThreadHolder.addThread(instance);
        }

        return instance;
    }
}
