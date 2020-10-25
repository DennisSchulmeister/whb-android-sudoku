package de.wpvs.sudo_ku.thread.clock;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import de.wpvs.sudo_ku.thread.BackgroundThread;
import de.wpvs.sudo_ku.thread.BackgroundThreadManager;

/**
 * Background thread that acts as the game clock by periodically calling its callback every
 * second. Using the methods of this class, the clock can be started and halted anytime.
 */
public class ClockThread extends BackgroundThread {
    private static final String NAME = "clock";

    private static final int MESSAGE_START = 1;
    private static final int MESSAGE_PAUSE = 2;
    private static final int MESSAGE_TICK = 3;

    private boolean running = false;
    private Callback callback = null;

    /**
     * Callback interface used to run some code every second. Note, that the callback will be
     * running in the clock thread.
     */
    public interface Callback {
        /**
         * Called every second as long as the clock is running.
         */
        void tickSecond();
    }

    /**
     * Don't allow direct instantiation of this class from clients. We rather want to use the
     * global BackgroundTheadManager obejct to maintain a singleton instance.
     */
    private ClockThread() {
        super(NAME);
    }

    /**
     * Get the singleton instance of this thread. Creates and starts the thread if necessary.
     *
     * @return background thread for periodic clock ticks
     */
    public static BackgroundThread getInstance() {
        BackgroundThreadManager backgroundThreadManager = BackgroundThreadManager.getInstance();
        BackgroundThread instance = backgroundThreadManager.getThread(NAME);

        if (instance == null) {
            instance = new ClockThread();
            backgroundThreadManager.addThread(instance);
        }

        return instance;
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
     * Start the clock, if it's not running, yet.
     */
    public void startClock() {
        this.sendEmptyMessage(MESSAGE_START);
    }

    /**
     * Halt the clock, if it is still running.
     */
    public void pauseClock() {
        this.sendEmptyMessage(MESSAGE_PAUSE);
    }

    /**
     * Check, whether the clock is running
     *
     * @returns true, if the clock is running
     */
    public boolean isRunning() {
        return this.running;
    }

    /**
     * Create custom Handler instance, which understands clock message.
     *
     * @return Custom Handler instance
     */
    @Override
    protected Handler doCreateHandler() {
        return new Handler(this.getLooper(), new HandlerCallback());
    }

    /**
     * The heart of our clock, actually running in the clock thread. The clock is simply a
     * Handler, that periodically sends TICK messages to itself, for as long as the clock is
     * running. START and PAUSE messages respectively start and halt the clock.
     */
    private class HandlerCallback implements Handler.Callback {
        /**
         * Arbitrate incoming messages by calling the appropriate methods.
         *
         * @param msg Incoming message
         * @returns always true (no further processing needed)
         */
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MESSAGE_START:
                    this.handleStartMessage();
                    break;
                case MESSAGE_PAUSE:
                    this.handleStopMessage();
                    break;
                case MESSAGE_TICK:
                    this.handleTickMessage();
                    break;
            }

            return true;
        }

        /**
         * Start the clock, if it s not yet running.
         */
        private void handleStartMessage() {
            if (!ClockThread.this.running) {
                ClockThread.this.running = true;
                ClockThread.this.sendEmptyMessage(MESSAGE_TICK);
            }
        }

        /**
         * Halt the clock, if it is still running.
         */
        private void handleStopMessage() {
            if (ClockThread.this.running) {
                ClockThread.this.running = false;
            }
        }

        /**
         * Call callback and schedule next tick.
         */
        private void handleTickMessage() {
            if (!ClockThread.this.running) {
                return;
            }

            if (ClockThread.this.callback != null) {
                ClockThread.this.callback.tickSecond();
            }

            ClockThread.this.getHandler().sendEmptyMessageDelayed(MESSAGE_TICK, 1000);
        }
    }
}
