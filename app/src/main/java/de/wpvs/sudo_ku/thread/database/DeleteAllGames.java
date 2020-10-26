package de.wpvs.sudo_ku.thread.database;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.activity.AppDialogFragmentBuilder;
import de.wpvs.sudo_ku.model.DatabaseHolder;
import de.wpvs.sudo_ku.model.game.GameDao;
import de.wpvs.sudo_ku.thread.BackgroundThread;
import de.wpvs.sudo_ku.thread.BackgroundThreadHolder;

/**
 * Background operation to delete all saved games. If there are no saved games, a toast message
 * will be posted. Otherwise a confirmation popup will be shown to make sure that the user really
 * wants to delete all games.
 *
 * NOTE: For an easier implementation the calling thread will be interrupted while the confirmation
 * popup is visible. Thus always run this task in the central database thread, which should be
 * accessed during that time, anyway.
 */
public class DeleteAllGames implements Runnable {
    private FragmentActivity activity;
    private View view;
    private Bundle savedInstanceState;
    private GameDao dao;

    private static int DECISION_YES = 1;
    private static int DECISION_NO = 2;

    private Callback callback;

    /**
     * Callback interface used to hide the saved games in the UI before they are really deleted.
     * This is used to give the user the impression, the games were already deleted when the
     * snackbar is shown, while in reality they are still there and were are just waiting if the
     * user changes mind.
     */
    public interface Callback {
        /**
         * Hide entries as if they were already deleted.
         */
        void beforeDeletion();

        /**
         * Restore the real active LiveData instances again.
         */
        void afterDeletion();
    }

    /**
     * Constructor.
     *
     * @param activity Calling activity
     * @param view View to search a suitable parent view for the snackbar
     * @param savedInstanceState Saved instance state
     */
    public DeleteAllGames(FragmentActivity activity, View view, Bundle savedInstanceState) {
        this.activity = activity;
        this.view = view;
        this.savedInstanceState = savedInstanceState;
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
        // Check, if there are any saved games at all
        int count = this.dao.selectGameCountSynchronously();

        if (count == 0) {
            Toast.makeText(this.activity, R.string.task_deleteAllGames_none_found, Toast.LENGTH_SHORT).show();
            return;
        }

        // Ask user if we really shall delete
        BackgroundThread thread = BackgroundThreadHolder.getInstance().getCurrentThread();
        AppDialogFragmentBuilder appDialogFragmentBuilder = new AppDialogFragmentBuilder(this.activity, this.savedInstanceState);

        AlertDialog.Builder builder = appDialogFragmentBuilder.getAlertDialogBuilder();
        builder.setMessage(R.string.task_deleteAllGames_confirm_message);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> thread.signalDecision(DECISION_YES));
        builder.setNegativeButton(R.string.no, (dialog, which) -> thread.signalDecision(DECISION_NO));

        appDialogFragmentBuilder.create().show(this.activity.getSupportFragmentManager(), "confirm_deletion");

        if (thread.waitForDecision() != DECISION_YES) {
            return;
        }

        // Show a snackbar, giving the user a last chance to stop deletion
        if (this.callback != null) {
            this.activity.runOnUiThread(() -> {
                this.callback.beforeDeletion();
            });
        }

        Snackbar snackbar = Snackbar.make(this.view, R.string.task_deleteAllGames_success, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo, view -> thread.signalDecision(DECISION_NO));

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                thread.signalDecision(DECISION_YES);
            }
        });

        snackbar.show();

        if (thread.waitForDecision() != DECISION_YES) {
            if (this.callback != null) {
                this.activity.runOnUiThread(() -> {
                    this.callback.afterDeletion();
                });
            }

            Toast.makeText(this.activity, R.string.task_deleteAllGames_undone, Toast.LENGTH_SHORT).show();
            return;
        }

        // Finally delete all games
        List<Long> gameIds = this.dao.selectAllGameIdsSynchronously();

        for (long uid : gameIds) {
            this.dao.delete(uid);
        }

        if (this.callback != null) {
            this.activity.runOnUiThread(() -> {
                this.callback.afterDeletion();
            });
        }
    }
}
