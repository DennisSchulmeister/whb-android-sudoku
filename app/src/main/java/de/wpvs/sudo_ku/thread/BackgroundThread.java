package de.wpvs.sudo_ku.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

/**
 * Better version and Android's stock HandlerThread class. Can either directly be used to create
 * a new Thread with a default Handler instance, or be subclassed to provide a customer Handler
 * instance. Also provides some convenience methods to post Runnables and Messages to the thread.
 */
public class BackgroundThread extends HandlerThread {
    private Handler handler = null;
    private int decision = -1;

    /**
     * Constructor.
     *
     * @param name Name of the thread
     */
    public BackgroundThread(String name) {
        super(name);
    }

    /**
     * Returns the Handler instance that is actually controlling the thread's execution. Use this
     * object to post Runnables to run on the thread or to post Messages to be handled by the
     * thread in case the provided convenience methods of this class are not enough for you.
     *
     * @returns the Handler instance of the thread
     */
    public Handler getHandler() {
        if (this.handler == null) {
            this.handler = this.doCreateHandler();
        }

        return handler;
    }

    /**
     * Template method to be overwritten by subclasses if they wish to use a customer Handler
     * instance, e.g. one that processes specific messages.
     *
     * @return new Handler instance
     */
    protected Handler doCreateHandler() {
        return new Handler(this.getLooper());
    }

    /**
     * Schedule the given Runnable to run on the Thread. Use the object returned by getHandler()
     * instead, if you need finer control on when the code should run.
     *
     * @param runnable Code to run
     * @return true, if the event could be scheduled
     */
    public final boolean post(Runnable runnable) {
        return this.getHandler().post(runnable);
    }

    /**
     * Send an empty message to be processed by the thread. Note, that this only has an effect on
     * subclasses, that provide their own Handler instance. Use the object returned by getHandler()
     * instead, if you need finer control on when the message should be handled.
     *
     * @param what Message number
     * @return true, if the event could be scheduled
     */
    public final boolean sendEmptyMessage(int what) {
        return this.getHandler().sendEmptyMessage(what);
    }

    /**
     * Send an full message to be processed by the thread. Note, that this only has an effect on
     * subclasses, that provide their own Handler instance. Use the object returned by getHandler()
     * instead, if you need finer control on when the message should be handled.
     *
     * @param message the Message to send
     * @return true, if the event could be scheduled
     */
    public final boolean sendMessage(Message message) {
        return this.getHandler().sendMessage(message);
    }

    /**
     * Block the calling thread (usually the background thread) until another thread (usually the
     * UI thread) has called signalDecision(). This is meant to be used when a confirmation popup
     * is shown in the UI thread, so that the background thread is halted until the user has made
     * a decision. The decision number given by the UI thread will then be returned here.
     *
     * @return Decision that has been sent by the other thread
     */
    public int waitForDecision() {
        synchronized (this) {
            this.decision = -1;

            while (this.decision == -1) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    // Nothing to be done
                }
            }
        }

        return this.decision;
    }

    /**
     * Wake up the background thread waiting for a decision. This is meant to be called from
     * another thread (usually the UI thread) after the background thread has been halted to
     * wait for something (usually an action by the user).
     *
     * The decision can be any number except -1, which is used to detect that no decision has
     * been made, yet, if the thread is waked up by an InterruptedException.
     *
     * @param decision The decision to hand over to the background thread
     */
    public void signalDecision(int decision) {
        synchronized (this) {
            this.decision = decision;
            this.notifyAll();
        }
    }
}
