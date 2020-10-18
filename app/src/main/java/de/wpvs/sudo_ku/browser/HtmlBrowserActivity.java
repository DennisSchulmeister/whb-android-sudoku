package de.wpvs.sudo_ku.browser;

import androidx.appcompat.app.AppCompatActivity;
import de.wpvs.sudo_ku.R;

import android.os.Bundle;

/**
 * Simple web browser based on the stock Android web view, which is used to display the man
 * page of any given Unix command.
 */
public class HtmlBrowserActivity extends AppCompatActivity {

    /**
     * System callback that will be used to inflate the UI, after the activity has been created.
     *
     * @param savedInstanceState The saved instance state, if the activity is restarted (e.g.
     *                           after a configuration change) or null, otherwise.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.html_browser_activity);
    }

}