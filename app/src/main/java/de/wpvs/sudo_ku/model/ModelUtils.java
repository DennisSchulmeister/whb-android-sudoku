package de.wpvs.sudo_ku.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.wpvs.sudo_ku.MyApplication;
import de.wpvs.sudo_ku.R;

/**
 * Static utility method to simplify or outsource some often needed functions.
 */
public class ModelUtils {

    /**
     * Factory method for a new game with random configuration. The returned object can directly
     * be saved.
     *
     * @return new game with random parameters
     */
    public static SavedGame createRandomGame() {
        Context context = MyApplication.getInstance();
        String[] gameTypes = context.getResources().getStringArray(R.array.game_type_keys);
        String[] boardSizes = context.getResources().getStringArray(R.array.board_size_keys);

        int gameTypeIndex = (int) Math.floor(Math.random() * gameTypes.length);
        int boardSizeIndex = (int) Math.floor(Math.random() * boardSizes.length);

        SavedGame savedGame = new SavedGame();
        savedGame.setGameType(GameType.valueOf(gameTypes[gameTypeIndex]));
        savedGame.setSize(Integer.parseInt(boardSizes[boardSizeIndex]));
        savedGame.setCharacterSet(createCharacterSet(savedGame.getGameType(), savedGame.getSize()));
        savedGame.setDifficulty(Math.max(0.33f, (float) Math.random()));
        return savedGame;
    }

    /**
     * Create a new character set for a game. In case of a number game, this simply contains
     * all numbers [1, size]. In case of a letter game random letters will be chosen.
     *
     * @param gameType The game type (number or letter quiz)
     * @param size Size of the game board
     * @return New shuffled list of numbers or letters
     */
    public static List<String> createCharacterSet(GameType gameType, int size) {
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
    public static void sortCharacterSet(List<String> characterSet, GameType gameType) {
        switch (gameType) {
            case NUMBER_GAME:
                Collections.sort(characterSet, (c1, c2) -> Integer.compare(Integer.parseInt(c1), Integer.parseInt(c2)));
                break;
            case LETTER_GAME:
                Collections.sort(characterSet);
                break;
        }
    }
}
