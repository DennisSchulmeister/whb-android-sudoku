package de.wpvs.sudo_ku.activity.menu;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.activity.NavigationUtils;
import de.wpvs.sudo_ku.model.GameDatabase;
import de.wpvs.sudo_ku.model.GameType;
import de.wpvs.sudo_ku.model.SavedGame;

/**
 * Fragment which holds the actual preferences view with the settings for a new game.
 */
public class NewGamePreferencesFragment extends PreferenceFragmentCompat {
    private static final String PARAMETER_GAME_TYPE = "PARAM_GAME_TYPE";
    private static final String PARAMETER_BOARD_SIZE = "PARAM_BOARD_SIZE";
    private static final String PARAMETER_CHARACTER_SET_NUMBERS = "PARAM_CHARACTER_SET_NUMBERS";
    private static final String PARAMETER_CHARACTER_SET_LETTERS = "PARAM_CHARACTER_SET_LETTERS";
    private static final String PARAMETER_DIFFICULTY = "PARAM_DIFFICULTY";
    private static final String ACTION_START_GAME = "ACTION_START_GAME";

    private static final String GAME_TYPE_NUMBER = "NUMBER_GAME";
    private static final String GAME_TYPE_LETTER = "LETTER_GAME";

    private ListPreference preferenceGameType;
    private ListPreference preferenceBoardSize;
    private MultiSelectListPreference preferenceCharacterSetNumbers;
    private MultiSelectListPreference preferenceCharacterSetLetters;
    private SeekBarPreference preferenceDifficulty;

    /**
     * Load prefernces definition from the corresponding XML resource.
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
        this.preferenceCharacterSetNumbers = this.findPreference(PARAMETER_CHARACTER_SET_NUMBERS);
        this.preferenceCharacterSetLetters = this.findPreference(PARAMETER_CHARACTER_SET_LETTERS);
        this.preferenceDifficulty = this.findPreference(PARAMETER_DIFFICULTY);

        // Toggle character sets when game type or board size are changed
        this.preferenceGameType.setOnPreferenceChangeListener((preference, newValue) -> {
            this.toggleVisibleCharacterSet(newValue.toString());
            this.shuffleAvailableCharacterSet(newValue.toString(), this.preferenceBoardSize.getValue());
            return true;
        });

        this.preferenceBoardSize.setOnPreferenceChangeListener((preference, newValue) -> {
            this.shuffleAvailableCharacterSet(this.preferenceGameType.getValue(), newValue.toString());
            return true;
        });

        this.toggleVisibleCharacterSet(this.preferenceGameType.getValue());
        this.shuffleAvailableCharacterSet(this.preferenceGameType.getValue(), this.preferenceBoardSize.getValue());

        // Show selected characters in their summary
        Preference.SummaryProvider characterSetSummaryProvider = preference -> {
            String gameType = this.preferenceGameType.getValue();
            List<String> valueList = this.getSelectedCharacters((MultiSelectListPreference) preference, gameType);
            String summary = "";

            for (String value : valueList) {
                if (summary.isEmpty()) {
                    summary = value;
                } else {
                    summary += ", " + value;
                }
            }

            return !summary.isEmpty() ? summary : this.getString(R.string.new_game_param_character_set_none_selected);
        };

        this.preferenceCharacterSetNumbers.setSummaryProvider(characterSetSummaryProvider);
        this.preferenceCharacterSetLetters.setSummaryProvider(characterSetSummaryProvider);

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
            this.checkSettingsAndStartGame();
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
                this.preferenceCharacterSetLetters.setVisible(false);
                this.preferenceCharacterSetNumbers.setVisible(true);
                break;
            case GAME_TYPE_LETTER:
                this.preferenceCharacterSetLetters.setVisible(true);
                this.preferenceCharacterSetNumbers.setVisible(false);
                break;
        }
    }

    /**
     * Randomly pick the available numbers or letters for the game.
     *
     * @param gameType Selected game type
     * @param boardSize Selected board size
     */
    private void shuffleAvailableCharacterSet(String gameType, String boardSize) {
        MultiSelectListPreference preference;
        int iBoardSize = Integer.parseInt(boardSize);
        List<String> availableCharacters;
        List<String> chosenCharacters = new ArrayList<>();

        switch (gameType) {
            case GAME_TYPE_NUMBER:
                preference = (MultiSelectListPreference) this.preferenceCharacterSetNumbers;
                availableCharacters = new ArrayList<String>(Arrays.asList(this.getResources().getStringArray(R.array.character_set_numbers)));
                break;
            case GAME_TYPE_LETTER:
                preference = (MultiSelectListPreference) this.preferenceCharacterSetLetters;
                availableCharacters = new ArrayList<String>(Arrays.asList(this.getResources().getStringArray(R.array.character_set_letters)));
                Collections.shuffle(availableCharacters);
                break;
            default:
                return;
        }

        for (int i = 0; i < iBoardSize; i++) {
            String character = availableCharacters.get(i);
            chosenCharacters.add(character);
        }

        preference.setValues(new HashSet<>(chosenCharacters));
    }

    /**
     * Utility method to get a sorted list of the chosen characters.
     *
     * @param preference The preference object for available numbers or letters
     * @param gameType The currently selected game type
     * @returns a sorted list of Strings
     */
    private List<String> getSelectedCharacters(MultiSelectListPreference preference, String gameType) {
        Set<String> valueSet = preference.getValues();
        List<String> valueList = new ArrayList<>(valueSet);

        switch (gameType) {
            case GAME_TYPE_NUMBER:
                Collections.sort(valueList, (o1, o2) -> Integer.compare(Integer.parseInt(o1), Integer.parseInt(o2)));
                break;
            case GAME_TYPE_LETTER:
                Collections.sort(valueList);
                break;
        }

        return valueList;
    }

    /**
     * Check all values for plausibility and start the game if all is good. Otherwise present a
     * snack bar with a hint on the erroneous value.
     */
    private void checkSettingsAndStartGame() {
        // Get all parameters
        String gameType = this.preferenceGameType.getValue();
        String boardSize = this.preferenceBoardSize.getValue();
        int iBoardSize = Integer.parseInt(boardSize);
        float difficulty = this.preferenceDifficulty.getValue() / 100.0f;

        List<String> characterSet = new ArrayList<>();
        String wrongAmountOfCharacters = "";

        switch (gameType) {
            case GAME_TYPE_NUMBER:
                characterSet = this.getSelectedCharacters(this.preferenceCharacterSetNumbers, gameType);
                wrongAmountOfCharacters = this.getString(R.string.new_game_error_wrong_amount_of_numbers, iBoardSize);
                break;
            case GAME_TYPE_LETTER:
                characterSet = this.getSelectedCharacters(this.preferenceCharacterSetLetters, gameType);
                wrongAmountOfCharacters = this.getString(R.string.new_game_error_wrong_amount_of_letters, iBoardSize);
                break;
        }

        // Check amount of selected characters
        if (characterSet.size() != iBoardSize) {
            Snackbar snackbar = Snackbar.make(this.getView(), wrongAmountOfCharacters, Snackbar.LENGTH_LONG);

            snackbar.setAction(R.string.new_game_error_wrong_amount_fix, v -> {
                // Automatically pick new characters if the user wishes
                this.shuffleAvailableCharacterSet(gameType, boardSize);
            });

            snackbar.show();
            return;
        }

        // Start new game
        SavedGame savedGame = new SavedGame();
        savedGame.setGameType(GameType.valueOf(gameType));
        savedGame.setSize(iBoardSize);
        savedGame.setCharacterSet(characterSet);
        savedGame.setDifficulty(difficulty);
        // TODO: Game board

        new Thread(() -> {
            GameDatabase.getInstance(this.getContext()).savedGameDAO().insert(savedGame);
            NavigationUtils.gotoSavedGame(this.getActivity(), savedGame.getId());
        }).start();
    }
}
