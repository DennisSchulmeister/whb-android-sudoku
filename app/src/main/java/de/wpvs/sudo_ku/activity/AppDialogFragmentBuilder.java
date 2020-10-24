package de.wpvs.sudo_ku.activity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

/**
 * Utility class that provides a simplified, fluid interface to display a confirmation dialog.
 * This abstracts away the need to implement a new class inheriting from AppCompatDialogFragment
 * just to show a simple confirmation dialog.
 *
 * Since this class still builds an AppCompatDialogFragment the dialog will be managed and thus not
 * disappear e.g. when a configuration change occurs, unlike with AlertDialog.Builder::show() or
 * Dialog::show().
 */
public class AppDialogFragmentBuilder {
    private Activity activity;
    private Bundle savedInstanceState = null;
    AlertDialog.Builder builder;

    /**
     * Constructor. Needs to be passed the owning activity.
     *
     * @param activity Owning activity
     */
    public AppDialogFragmentBuilder(Activity activity) {
        this.activity = activity;
    }

    /**
     * Constructor. Needs to be passed the owning activity and the saved instance state.
     *
     * @param activity Owning activity
     * @param savedInstanceState Saved instance state
     */
    public AppDialogFragmentBuilder(Activity activity, Bundle savedInstanceState) {
        this.activity = activity;
        this.savedInstanceState = savedInstanceState;
    }

    /**
     * Get the alert dialog builder used to configure the dialog.
     * @return AlertDialog.Builder instance
     */
    public AlertDialog.Builder getAlertDialogBuilder() {
        if (this.builder == null) {
            this.builder = new AlertDialog.Builder(this.activity);
        }

        return this.builder;
    }

    /**
     * Build the dialog fragment, that can be used to show the dialog. Simply call the show()
     * method of the returned object.
     *
     * @returns a newAppCompatDialogFragment instance.
     */
    public AppCompatDialogFragment create() {
        return new DialogFragment(this.builder);
    }

    /**
     * That's the thing we want to hide from our clients. It seems crazy that one has to implement
     * a whole new class just to display a confirmation message.
     */
    public static class DialogFragment extends AppCompatDialogFragment {
        AlertDialog.Builder builder;

        public DialogFragment(AlertDialog.Builder builder) {
            this.builder = builder;
        }
        /**
         * Here the magic happens and the dialog is being built.
         *
         * @param savedInstanceState
         * @return A new dialog
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return this.builder.create();
        }
    }
}
