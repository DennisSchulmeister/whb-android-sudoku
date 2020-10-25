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
public class GameUtils {
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
     * Create a new list for character fields for a game, making sure the indices are set correctly
     * and the list is sorted.
     *
     * @param size Size of the game board
     * @return New sorted list of character fields
     */
    public static List<CharacterFieldEntity> createCharacterFields(int size) {
        List<CharacterFieldEntity> characterFieldEntities = new ArrayList<>(size * size);

        for (int xPos = 0; xPos < size; xPos++) {
            for (int yPos = 0; yPos < size; yPos++) {
                CharacterFieldEntity characterFieldEntity = new CharacterFieldEntity();
                characterFieldEntity.xPos = xPos;
                characterFieldEntity.yPos = yPos;

                characterFieldEntities.add(characterFieldEntity);
            }
        }

        return characterFieldEntities;
    }

    /**
     * When a new game is started, prepopulate hte character fields with characters, according
     * to the chosen difficulty. The difficulty here simply is the percentage of fields that are
     * not tried to be filled.
     */
    public static void prepopulateCharacterFields(GameState gameState) {
        // TODO
    }
}
