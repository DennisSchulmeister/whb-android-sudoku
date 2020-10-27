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
import androidx.preference.SwitchPreference;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.activity.NavigationUtils;
import de.wpvs.sudo_ku.model.game.GameEntity;
import de.wpvs.sudo_ku.model.game.GameState;
import de.wpvs.sudo_ku.model.game.GameUtils;
import de.wpvs.sudo_ku.thread.database.DatabaseThread;
import de.wpvs.sudo_ku.thread.database.SaveOrDeleteGame;

/**
 * Fragment which holds the actual preferences view with the settings for a new game.
 */
public class NewGamePreferencesFragment extends PreferenceFragmentCompat {
    private static final String PARAMETER_GAME_TYPE = "PARAM_GAME_TYPE";
    private static final String PARAMETER_BOARD_SIZE = "PARAM_BOARD_SIZE";
    private static final String PARAMETER_CHARACTER_SET = "PARAM_CHARACTER_SET";
    private static final String PARAMETER_PREFILL = "PARAM_PREFILL";
    private static final String PARAMETER_LOCK_PREFILLED = "PARAM_LOCK_PREFILLED";
    private static final String ACTION_START_GAME = "ACTION_START_GAME";

    private static final String GAME_TYPE_NUMBER = "NUMBER_GAME";
    private static final String GAME_TYPE_LETTER = "LETTER_GAME";

    private ListPreference preferenceGameType;
    private ListPreference preferenceBoardSize;
    private MultiSelectListPreference preferenceCharacterSet;
    private SeekBarPreference preferencePrefill;
    private SwitchPreference preferenceLockPrefilled;

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
        this.preferencePrefill = this.findPreference(PARAMETER_PREFILL);
        this.preferenceLockPrefilled = this.findPreference(PARAMETER_LOCK_PREFILLED);

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

        // Show selected values in their respective summary
        this.preferenceGameType.setSummaryProvider(preference -> {
            ListPreference listPreference = (ListPreference) preference;
            return listPreference.getEntry();
        });

        this.preferenceBoardSize.setSummaryProvider(preference -> {
            ListPreference listPreference = (ListPreference) preference;
            return listPreference.getEntry();
        });

        // Show selected characters in their summary
        this.preferenceCharacterSet.setSummaryProvider(preference -> {
            MultiSelectListPreference listPreference = (MultiSelectListPreference) preference;
            List<String> characterSet = new ArrayList<>(listPreference.getValues());
            String summary = "";

            GameUtils.sortCharacterSet(characterSet, GameEntity.GameType.valueOf(this.preferenceGameType.getValue()));

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
        this.preferencePrefill.setMin(0);
        this.preferencePrefill.setMax(25);
        this.preferencePrefill.setSummary(this.preferencePrefill.getValue() + "%");

        this.preferencePrefill.setOnPreferenceChangeListener((preference, newValue) -> {
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
        GameEntity.GameType eGameType = GameEntity.GameType.valueOf(gameType);

        Collection<String> characterSet = GameUtils.createCharacterSet(eGameType, iBoardSize);
        this.preferenceCharacterSet.setValues(new HashSet<>(characterSet));
    }

    /**
     * Check all values for plausibility and start the game if all is good. Otherwise present a
     * snack bar with a hint on the erroneous value.
     */
    private void startGame() {
        // Get all parameters
        GameEntity.GameType gameType = GameEntity.GameType.valueOf(this.preferenceGameType.getValue());
        int boardSize = Integer.parseInt(this.preferenceBoardSize.getValue());
        int prefill = this.preferencePrefill.getValue();
        Set<String> characterSet = this.preferenceCharacterSet.getValues();
        boolean lockPrefilled = this.preferenceLockPrefilled.isChecked();

        // Try to save and start game
        GameEntity gameEntity = new GameEntity();
        gameEntity.gameType = gameType;
        gameEntity.size = boardSize;
        gameEntity.prefill = prefill;
        gameEntity.characterSet = new ArrayList<String>(characterSet);
        gameEntity.lockPrefilled = lockPrefilled;

        GameState gameState = GameState.createParametrizedGame(gameEntity);
        SaveOrDeleteGame task = new SaveOrDeleteGame(gameState, SaveOrDeleteGame.Operation.INSERT);

        task.setCallback(new SaveOrDeleteGame.Callback() {
            @Override
            public void onUpdatePerformed() {
                NavigationUtils.gotoSavedGame(NewGamePreferencesFragment.this.getActivity(), gameEntity.uid);
            }

            @Override
            public void onErrorsFound(Map<GameState.Error, String> errors) {
                if (errors.containsKey(GameState.Error.ERROR_CHARSET_SIZE)) {
                    // Snackbar to shuffle character set, in case not enough characters were selected
                    String message = errors.get(GameState.Error.ERROR_CHARSET_SIZE);
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
