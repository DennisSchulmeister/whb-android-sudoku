package de.wpvs.sudo_ku.model.game;

import android.content.Context;

import java.lang.reflect.GenericArrayType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.wpvs.sudo_ku.MyApplication;
import de.wpvs.sudo_ku.R;

/**
 * Static utility method to simplify or outsource some often needed functions.
 */
public class GameUtils {
    /**
     * Returns a localized string to properly format the game type, e.g. "4×4 Letter Game".
     *
     * @param gameEntity The game whose type shall be formated
     * @returns Localized string containing size and type of the game
     */
    public static String formatGameType(GameEntity gameEntity) {
        Context context = MyApplication.getInstance();

        StringBuilder text = new StringBuilder();
        text.append(gameEntity.size);
        text.append("×");
        text.append(gameEntity.size);
        text.append(" ");

        switch (gameEntity.gameType) {
            case NUMBER_GAME:
                text.append(context.getString(R.string.game_type_number));
                break;
            case LETTER_GAME:
                text.append(context.getString(R.string.game_type_letter));
                break;
        }

        return text.toString();
    }

    /**
     * Returns a formatted string with the hours (optional), minutes and seconds the game
     * already has been running.
     *
     * @param gameEntity The game whose duration shall be formatted
     * @return String containing hours, minutes and seconds
     */
    public static String formatElapsedTime(GameEntity gameEntity) {
        StringBuilder text = new StringBuilder();
        long time = gameEntity.seconds;
        int hours, minutes, seconds;

        hours   = (int) Math.floor(time / 3600f);
        minutes = (int) Math.floor((time - (3600 * hours)) / 60f);
        seconds = (int) (time - (3600 * hours)) % 60;

        if (hours != 0) {
            if (hours < 10) {
                text.append("0");
            }

            text.append(hours);
            text.append(":");
        }

        if (minutes < 10) {
            text.append("0");
        }

        text.append(minutes);
        text.append(":");

        if (seconds < 10) {
            text.append("0");
        }

        text.append(seconds);

        return text.toString();
    }

    /**
     * Returns a formatted string with the game progress in percent.
     *
     * @param gameEntity The game whose progress shall be formatted
     * @return String the progress in percent
     */
    public static String formatProgress(GameEntity gameEntity) {
        return (int) Math.floor(gameEntity.progress) + "%";
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
     *
     * NOTE: the algorithm here is not very clever. It is more of a brute-force attack, simply
     * picking random fields and then assigning random characters that are valid for those fields.
     * This should hopefully work in most cases. But to be very sure, the algorithm is time boxed.
     * It might thus be, that some fields remain unfilled or a game cannot be won.
     */
    public static void prepopulateCharacterFields(GameState gameState) {
        // Prepare list of so far unset fields, to simplify picking a new random field
        List<CharacterFieldEntity> remainingCharacterFields = new ArrayList<>(gameState.characterFields.size());

        for (CharacterFieldEntity characterField : gameState.characterFields) {
            remainingCharacterFields.add(characterField);
        }

        // Spend the given amount of tries to fill some random fields
        GameLogic gameLogic = gameState.getGameLogic();
        int flags = GameState.FLAG_LOCKED;

        int amountOfCharactersToSet = (int) (gameState.characterFields.size() * (100 - gameState.game.difficulty) / 100);
        int maxTries = amountOfCharactersToSet * 3;
        int successfulTries = 0;
        int totalTries = 0;

        while (successfulTries <= amountOfCharactersToSet && totalTries <= maxTries) {
            totalTries += 1;

            if (remainingCharacterFields.isEmpty()) {
                break;
            }

            int index1 = (int) Math.floor(Math.random() * remainingCharacterFields.size());
            CharacterFieldEntity characterField = remainingCharacterFields.remove(index1);

            List<String> allowedCharacters = gameLogic.getAllowedCharacters(characterField.xPos, characterField.yPos, flags);

            if (!allowedCharacters.isEmpty()) {
                successfulTries += 1;

                int index2 = (int) Math.floor(Math.random() * allowedCharacters.size());
                String character = allowedCharacters.get(index2);

                gameLogic.changeCharacter(characterField.xPos, characterField.yPos, flags, character);
            }
        }
    }
}
