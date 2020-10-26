package de.wpvs.sudo_ku.thread;

import java.util.concurrent.Semaphore;

/**
 * This class provides a callback mechanism that a background thread can provide to temporarily
 * suspend another thread without resorting to classical locking. This is meant for cases, when
 * an object is used almost always by the same thread (e.g. the UI thread) but very infrequently
 * a background thread needs to update the object for a very short time.
 *
 * Note the phrases „very infrequently“ and „for a very short time” here. Of course we especially
 * want to keep the UI thread going at all times. But there are cases where just doesn't make sense
 * to resort to more complicated protocols just to update a handful fields provided by another
 * thread.
 */
public class ThreadMutex {
    private Semaphore backgroundThreadMayStart = new Semaphore(0);
    private Semaphore foregroundThreadMyContinue = new Semaphore(0);
    private Callback callback;

    /**
     * Callback interface that needs to be implemented by the foreground activity. The implementing
     * class must schedule execution in the foreground thread and call be provided Runnable to
     * temporarily suspend the foreground thread and allow the background thread to run.
     */
    public interface Callback {
        /**
         * Method called by the background thread to temporarily block the foreground thread.
         */
        void requestSuspend(Runnable blockThread);
    }

    /**
     * Constructor. To be used by the background thread to create a new mutex.
     * @param callback Callback implementation from the foreground thread.
     */
    public ThreadMutex(Callback callback) {
        this.callback = callback;
    }

    /**
     * Called by the background thread to block until the foreground thread is waiting.
     */
    public void lock() {
        if (this.callback == null) {
            return;
        }

        this.callback.requestSuspend(() -> {
            this.backgroundThreadMayStart.release();
            this.foregroundThreadMyContinue.acquireUninterruptibly();
        });

        this.backgroundThreadMayStart.acquireUninterruptibly();
    }

    /**
     * Called by the background thread to release the foreground thread again.
     */
    public void release() {
        if (this.callback == null) {
            return;
        }

        this.foregroundThreadMyContinue.release();
    }
}
