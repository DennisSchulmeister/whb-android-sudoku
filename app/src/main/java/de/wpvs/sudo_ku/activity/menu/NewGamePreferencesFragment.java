package de.wpvs.sudo_ku.activity.menu;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.activity.NavigationUtils;
import de.wpvs.sudo_ku.model.GameType;
import de.wpvs.sudo_ku.model.SavedGame;
import de.wpvs.sudo_ku.model.ModelUtils;
import de.wpvs.sudo_ku.thread.database.DatabaseThread;
import de.wpvs.sudo_ku.thread.database.SaveOrDeleteGame;

/**
 * Fragment which holds the actual preferences view with the settings for a new game.
 */
public class NewGamePreferencesFragment extends PreferenceFragmentCompat {
    private static final String PARAMETER_GAME_TYPE = "PARAM_GAME_TYPE";
    private static final String PARAMETER_BOARD_SIZE = "PARAM_BOARD_SIZE";
    private static final String PARAMETER_CHARACTER_SET = "PARAM_CHARACTER_SET";
    private static final String PARAMETER_DIFFICULTY = "PARAM_DIFFICULTY";
    private static final String ACTION_START_GAME = "ACTION_START_GAME";

    private static final String GAME_TYPE_NUMBER = "NUMBER_GAME";
    private static final String GAME_TYPE_LETTER = "LETTER_GAME";

    private ListPreference preferenceGameType;
    private ListPreference preferenceBoardSize;
    private MultiSelectListPreference preferenceCharacterSet;
    private SeekBarPreference preferenceDifficulty;

    /**
     * Load preferences definition from the corresponding XML resource.
     * @param savedInstanceState The saved instance state of the activity
     * @param rootKey Not used
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Inflate UI
        this.setPreferencesFromResource(R.xml.new_game_preferences, null);

        // Get references to each preference, so that we can latter access their values
        this.preferenceGameType = this.findPreference(PARAMETER_GAME_TYPE);
        this.preferenceBoardSize = this.findPreference(PARAMETER_BOARD_SIZE);
        this.preferenceCharacterSet = this.findPreference(PARAMETER_CHARACTER_SET);
        this.preferenceDifficulty = this.findPreference(PARAMETER_DIFFICULTY);

        // Toggle character sets when game type or board size are changed
        this.preferenceGameType.setOnPreferenceChangeListener((preference, newValue) -> {
            this.toggleVisibleCharacterSet(newValue.toString());
            this.shuffleCharacterSet(newValue.toString(), this.preferenceBoardSize.getValue());
            return true;
        });

        this.preferenceBoardSize.setOnPreferenceChangeListener((preference, newValue) -> {
            this.shuffleCharacterSet(this.preferenceGameType.getValue(), newValue.toString());
            return true;
        });

        this.toggleVisibleCharacterSet(this.preferenceGameType.getValue());
        this.shuffleCharacterSet(this.preferenceGameType.getValue(), this.preferenceBoardSize.getValue());

        // Show selected characters in their summary
        this.preferenceCharacterSet.setSummaryProvider(preference -> {
            MultiSelectListPreference listPreference = (MultiSelectListPreference) preference;
            List<String> characterSet = new ArrayList<>(listPreference.getValues());
            String summary = "";

            ModelUtils.sortCharacterSet(characterSet, GameType.valueOf(this.preferenceGameType.getValue()));

            for (String value : characterSet) {
                if (summary.isEmpty()) {
                    summary = value;
                } else {
                    summary += ", " + value;
                }
            }

            return !summary.isEmpty() ? summary : this.getString(R.string.new_game_param_character_set_none_selected);
        });

        // Show difficulty value in its summary
        // NOTE: Using a SummaryProvider doesn't reliably update the summary, when the
        // value is changed. :-(
        this.preferenceDifficulty.setMin(33);
        this.preferenceDifficulty.setMax(100);
        this.preferenceDifficulty.setSummary(this.preferenceDifficulty.getValue() + "%");

        this.preferenceDifficulty.setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary("" + newValue + "%");
            return true;
        });
    }

    /**
     * Start game, when the corresponding preference is clicked.
     *
     * @param preference Clicked preference
     * @returns always true
     */
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        super.onPreferenceTreeClick(preference);

        if (preference.getKey().equals(ACTION_START_GAME)) {
            this.startGame();
        }

        return true;
    }

    /**
     * Show the currently selected values in the summary string of each preference. Also make sure
     * that only PARAMETER_CHARACTER_SET_NUMBERS or PARAMETER_CHARACTER_SET_LETTERS is shown
     * according to the currently selected game type.
     */
    private void toggleVisibleCharacterSet(String selectedGameType) {
        // Show the correct character set preference, depending on the game type
        switch (selectedGameType) {
            case GAME_TYPE_NUMBER:
                this.preferenceCharacterSet.setTitle(R.string.new_game_param_character_set_numbers);
                this.preferenceCharacterSet.setDialogTitle(R.string.new_game_param_character_set_numbers);
                this.preferenceCharacterSet.setEntries(R.array.character_set_numbers);
                this.preferenceCharacterSet.setEntryValues(R.array.character_set_numbers);
                break;
            case GAME_TYPE_LETTER:
                this.preferenceCharacterSet.setTitle(R.string.new_game_param_character_set_letters);
                this.preferenceCharacterSet.setDialogTitle(R.string.new_game_param_character_set_letters);
                this.preferenceCharacterSet.setEntries(R.array.character_set_letters);
                this.preferenceCharacterSet.setEntryValues(R.array.character_set_letters);
                break;
        }
    }

    /**
     * Randomly pick the available numbers or letters for the game.
     *
     * @param gameType Selected game type
     * @param boardSize Selected board size
     */
    private void shuffleCharacterSet(String gameType, String boardSize) {
        int iBoardSize = Integer.parseInt(boardSize);
        GameType eGameType = GameType.valueOf(gameType);

        Collection<String> characterSet = ModelUtils.createCharacterSet(eGameType, iBoardSize);
        this.preferenceCharacterSet.setValues(new HashSet<>(characterSet));
    }

    /**
     * Check all values for plausibility and start the game if all is good. Otherwise present a
     * snack bar with a hint on the erroneous value.
     */
    private void startGame() {
        // Get all parameters
        GameType gameType = GameType.valueOf(this.preferenceGameType.getValue());
        int boardSize = Integer.parseInt(this.preferenceBoardSize.getValue());
        float difficulty = this.preferenceDifficulty.getValue() / 100.0f;
        Set<String> characterSet = this.preferenceCharacterSet.getValues();

        // Try to save and start game
        SavedGame savedGame = new SavedGame();
        savedGame.setGameType(gameType);
        savedGame.setSize(boardSize);
        savedGame.setDifficulty(difficulty);
        savedGame.setCharacterSet(new ArrayList<String>(characterSet));

        SaveOrDeleteGame task = new SaveOrDeleteGame(savedGame, SaveOrDeleteGame.Operation.INSERT);

        task.setCallback(new SaveOrDeleteGame.Callback() {
            @Override
            public void onUpdatePerformed() {
                NavigationUtils.gotoSavedGame(NewGamePreferencesFragment.this.getActivity(), savedGame.getId());
            }

            @Override
            public void onErrorsFound(Map<SavedGame.Error, String> errors) {
                if (errors.containsKey(SavedGame.Error.ERROR_CHARSET_SIZE)) {
                    // Snackbar to shuffle character set, in case not enough characters were selected
                    String message = errors.get(SavedGame.Error.ERROR_CHARSET_SIZE);
                    Snackbar snackbar = Snackbar.make(NewGamePreferencesFragment.this.getView(), message, Snackbar.LENGTH_LONG);

                    snackbar.setAction(R.string.new_game_error_wrong_amount_fix, v -> {
                        // Automatically pick new characters if the user wishes
                        NewGamePreferencesFragment.this.shuffleCharacterSet(gameType.name(), Integer.toString(boardSize));
                    });

                    snackbar.show();
                } else {
                    // Toast otherwise
                    for (String message : errors.values()) {
                        Toast.makeText(NewGamePreferencesFragment.this.getContext(), message, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        });

        DatabaseThread.getInstance().post(task);
    }

}
