package de.wpvs.sudo_ku.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.activity.game.GameActivity;
import de.wpvs.sudo_ku.activity.game.GameFinishedActivity;
import de.wpvs.sudo_ku.activity.menu.NewGameActivity;
import de.wpvs.sudo_ku.activity.menu.StartMenuActivity;

/**
 * Static utility methods to allow for type-checked navigation between activities.
 */
public class NavigationUtils {
    /**
     * Go to the start menu activity.
     *
     * @param activity Calling activity
     */
    public static void gotoStartMenu(Activity activity) {
        Intent intent = new Intent(activity, StartMenuActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Go to the activity to start a new game.
     *
     * @param activity Calling activity
     */
    public static void gotoNewGame(Activity activity) {
        Intent intent = new Intent(activity, NewGameActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Go to the activity running a game.
     *
     * @param activity Calling activity
     * @param gameUid Database ID of the game
     */
    public static void gotoSavedGame(Activity activity, long gameUid) {
        Intent intent = new Intent(activity, GameActivity.class);
        intent.putExtra("gameUid", gameUid);
        activity.startActivity(intent);
    }

    /**
     * Go to the activity shown after a games has been successfully solved.
     *
     * @param activity Calling activity
     * @param gameUid Database ID of the game
     */
    public static void gotoGameFinished(Activity activity, long gameUid) {
        Intent intent = new Intent(activity, GameFinishedActivity.class);
        intent.putExtra("gameUid", gameUid);
        activity.startActivity(intent);
    }

    /**
     * Open a web browser (via Android's custom tabs API) to display a given website.
     *
     * @param context Calling context
     * @param url URL of the website to show
     */
    public static void gotoWebsite(Context context, String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.primaryColor));

        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, Uri.parse(url));

    }

    /**
     * Open a web browser (via Android's custom tabs API) to display the man page of a command.
     *
     * @param context Calling context
     * @param command Searched command
     */
    public static void gotoManPage(Context context, String command) {
        gotoWebsite(context, "http://man.he.net/" + command + "&section=all");
    }

    /**
     * Open a web browser (via Android's custom tabs API) to display the TLDR page of a command.
     *
     * @param context Calling context
     * @param command Searched command
     */
    public static void gotoTldrPage(Context context, String command) {
        gotoWebsite(context, "https://tldr.ostera.io/" + command);
    }
}
