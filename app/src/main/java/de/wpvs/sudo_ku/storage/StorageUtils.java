package de.wpvs.sudo_ku.storage;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.wpvs.sudo_ku.MyApplication;
import de.wpvs.sudo_ku.R;

/**
 * Static utility method to simplify or outsource some often needed functions.
 */
public class StorageUtils {
    /**
     * Error codes, why a game is inconsistent and should not be saved.
     */
    public enum Error {
        ERROR_CHARSET_SIZE,
    }

    /**
     * Factory method for a new game with random configuration. The returned object can directly
     * be saved.
     *
     * @return new game with random parameters
     */
    public static GameEntity createRandomGame() {
        Context context     = MyApplication.getInstance();
        String[] gameTypes  = context.getResources().getStringArray(R.array.game_type_keys);
        String[] boardSizes = context.getResources().getStringArray(R.array.board_size_keys);

        int gameTypeIndex  = (int) Math.floor(Math.random() * gameTypes.length);
        int boardSizeIndex = (int) Math.floor(Math.random() * boardSizes.length);

        GameEntity gameEntity   = new GameEntity();
        gameEntity.gameType     = GameEntity.GameType.valueOf(gameTypes[gameTypeIndex]);
        gameEntity.size         = Integer.parseInt(boardSizes[boardSizeIndex]);
        gameEntity.characterSet = createCharacterSet(gameEntity.gameType, gameEntity.size);
        gameEntity.difficulty   = Math.max(0.33f, (float) Math.random());
        return gameEntity;
    }

    /**
     * Create a new character set for a game. In case of a number game, this simply contains
     * all numbers [1, size]. In case of a letter game random letters will be chosen.
     *
     * @param gameType The game type (number or letter quiz)
     * @param size Size of the game board
     * @return New shuffled list of numbers or letters
     */
    public static List<String> createCharacterSet(GameEntity.GameType gameType, int size) {
        Context context = MyApplication.getInstance();

        List<String> availableCharacters;
        List<String> chosenCharacters = new ArrayList<>();

        switch (gameType) {
            case NUMBER_GAME:
                availableCharacters = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(R.array.character_set_numbers)));
                break;
            case LETTER_GAME:
                availableCharacters = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(R.array.character_set_letters)));
                Collections.shuffle(availableCharacters);
                break;
            default:
                availableCharacters = new ArrayList<>();
        }

        for (int i = 0; i < size; i++) {
            String character = availableCharacters.get(i);
            chosenCharacters.add(character);
        }

        sortCharacterSet(chosenCharacters, gameType);
        return chosenCharacters;
    }

    /**
     * Utility method to sort the given character set, either numerically or alphabetically
      * depending on the game type-
      *
      * @param characterSet Character set to be sorted
      * @param gameType Game type
      */
    public static void sortCharacterSet(List<String> characterSet, GameEntity.GameType gameType) {
        switch (gameType) {
            case NUMBER_GAME:
                Collections.sort(characterSet, (c1, c2) -> Integer.compare(Integer.parseInt(c1), Integer.parseInt(c2)));
                break;
            case LETTER_GAME:
                Collections.sort(characterSet);
                break;
        }
    }

    /**
     * Check that all parameters are consistent and the game is safe to be saved. Note, that this
     * currently only checks that the amount of available characters matches the game board size.
     * Especially it is not checked, that every parameters has a value, since each parameter has
     * an assigned default value, that is set when the new GameEntity object is created.
     *
     * @return A list of all found errors
     * @param gameEntity
     */
    public static Map<Error, String> checkGameConsistency(GameEntity gameEntity) {
        Map<Error, String> errors = new HashMap<>();
        Context context = MyApplication.getInstance();

        if (gameEntity.characterSet == null || gameEntity.characterSet.size() != gameEntity.size) {
            String message = "";

            switch (gameEntity.gameType) {
                case NUMBER_GAME:
                    message = context.getString(R.string.new_game_error_wrong_amount_of_numbers, gameEntity.size);
                    break;
                case LETTER_GAME:
                    message = context.getString(R.string.new_game_error_wrong_amount_of_letters, gameEntity.size);
                    break;
            }

            errors.put(Error.ERROR_CHARSET_SIZE, message);
        }

        return errors;
    }
}
