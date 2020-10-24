package de.wpvs.sudo_ku.thread;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton object to manage all background threads used by the application. This is used by the
 * application's main activity terminate the threads on exit and by all other classes to get
 * access to (and thus starting) the threads. Depending on the thread implementation, usually a
 * Runnable or a Message can then be posted to be processed by the thread.
 */
public class BackgroundThreadManager {
    private static BackgroundThreadManager instance;
    private Map<String, BackgroundThread> threads = new HashMap<>();

    /**
     * Don't allow direct instantiation.
     */
    private BackgroundThreadManager() {
    }

    /**
     * Get the single one background thread manager.
     *
     * @return Singleton instance of this class
     */
    public static BackgroundThreadManager getInstance() {
        if (instance == null) {
            instance = new BackgroundThreadManager();
        }

        return instance;
    }

    /**
     * Add a new background thread to be managed by this class. For this to work the thread
     * must have a unique name, not used by any other thread managed by this class!
     *
     * This method needs to be called either by the main activity of the app or by a static
     * accessor method of the thread itself to add a new singleton instance. Afterwards getThread()
     * can be used to retrieve the thread instance.
     *
     * @param thread Background thread to be managed
     */
    public void addThread(BackgroundThread thread) {
        thread.start();
        this.threads.put(thread.getName(), thread);
    }

    /**
     * Retrieve the instance of a previously added thread. This method is meant to be used
     * everywhere in the application, where a certain thread, that has previously been added,
     * is needed.
     *
     * NOTE: This method can be used directly everywhere in the app. However, for added type-safety
     * and robustness each background thread should have its own class, providing a static
     * getInstance() method. This method should call here to retrieve the singleton instance
     * or start a new thread and call addThread() if no instance could be found. This way the
     * thread will not started unless really needed and it will always restart as needed.
     *
     * @param name Name of the search thread
     * @return Requested background thread or null, if not found
     */
    public BackgroundThread getThread(String name) {
        return this.threads.get(name);
    }

    /**
     * Get the currently running BackgroundThread, provided the current thread is managed by
     * this class. This is meant to be called by Runnables, which are executed in one of the
     * background threads to call the waitForDecision() and signalDecision() methods.
     *
     * @return The currently running BackgroundThread or null
     */
    public BackgroundThread getCurrentThread() {
        Thread currentThread = Thread.currentThread();
        BackgroundThread backgroundThread = null;

        if (currentThread instanceof BackgroundThread) {
            backgroundThread = (BackgroundThread) currentThread;
        }

        if (this.threads.containsValue(backgroundThread)) {
            return backgroundThread;
        }

        return null;
    }

    /**
     * Called by the main activity to safely quit all threads. Each thread will be given the
     * opportunity to finish processing all queued messages before it terminates.
     */
    public void quitSafely() {
        for (BackgroundThread thread : this.threads.values()) {
            thread.quitSafely();
            this.threads.remove(thread.getName());
        }
    }
}
