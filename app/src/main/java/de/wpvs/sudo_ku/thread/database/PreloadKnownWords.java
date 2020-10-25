package de.wpvs.sudo_ku.thread.database;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.wpvs.sudo_ku.MyApplication;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.model.DatabaseHolder;
import de.wpvs.sudo_ku.model.dictionary.KnownWordDao;
import de.wpvs.sudo_ku.model.dictionary.KnownWordEntity;

/**
 * Background operation to load the list of known words from the asset file raw/knownwords.txt
 * into the database, in case the database table is empty. This is done manually to prevent having
 * to ship a full-fledged preloaded sqlite database file just for the list of known words.
 *
 * If the database contains at least one word, this task does nothing. Otherwise all words from
 * the asset file are saved into the table.
 *
 * There is no callback here, as it is assumed that this only needs to be done before the very
 * first game is played and will long be finished, when the first word is searched.
 */
public class PreloadKnownWords implements Runnable {
    private KnownWordDao dao;

    /**
     * Constructor.
     */
    public PreloadKnownWords() {
        this.dao = DatabaseHolder.getInstance().knownWordDao();
    }

    /**
     * Execute task.
     */
    @Override
    public void run() {
        if (dao.getRowCountSynchronously() > 0) {
            return;
        }

        Context context = MyApplication.getInstance();
        KnownWordEntity entity = new KnownWordEntity();

        try(BufferedReader fromFile = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.knownwords)))) {
            while ((entity.word = fromFile.readLine()) != null) {
                dao.insert(entity);
            }
        } catch (IOException ex) {
            String message = context.getString(R.string.task_preloadKnownWords_error, ex.getLocalizedMessage());
            Toast.makeText(context, message, Toast.LENGTH_LONG);

            Log.e("sudo-ku", "IOException", ex);
        }
    }
}
