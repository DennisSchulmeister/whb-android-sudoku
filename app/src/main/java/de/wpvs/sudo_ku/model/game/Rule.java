package de.wpvs.sudo_ku.model.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for game rules as used by the GameLogic class. Subclasses of this are
 * queried by the GameLogic class during the game to execute the actual game logic.
 *
 * This class is not public, since it is only used internally by the GameLogic class.
 */
abstract class Rule {
    protected GameState gameState;
    protected CharacterFieldEntity[][] characterFields;

    /**
     * Constructor.
     *
     * @param gameState The game for which the rules shall be checked
     * @param characterFields Two-dimensional view on the game board. Organized [xPos][yPos].
     */
    Rule(GameState gameState, CharacterFieldEntity[][] characterFields) {
        this.gameState = gameState;
        this.characterFields = characterFields;
    }

    /**
     * For a given position on the game board get the coordinates of all fields, that may not
     * contain duplicate characters.
     *
     * @param xPos Row
     * @param yPos Column
     * @return List of coordinates where no duplicate characters are allowed
     */
    public List<GameLogic.Coordinate> getFieldsWithoutDuplicates(int xPos, int yPos) {
        return new ArrayList<>(0);
    }

    /**
     * This method checks whether a given character is allowed at the given position. This is used
     * not only during the game before a character is placed, but also to reduce the available
     * characters before they are offered to the player.
     *
     * @param xPos Row
     * @param yPos Column
     * @param flags Special flags on how to treat the character (see constants)
     * @param character The character to set (must be one of the characters of the game)
     * @param set True to set and false to erase the character
     * @return true, when the character is allowed
     */
    public boolean isCharacterAllowed(int xPos, int yPos, int flags, String character, boolean set) {
        return true;
    }

    /**
     * This method is called, after a valid character has been placed on the game board, so that
     * further modifications to the game can be done. Most notably this is used in the letter
     * game to scan for known words, after a letter has been set.
     *
     * @param xPos Row
     * @param yPos Column
     * @param flags Special flags on how to treat the character (see constants)
     * @param character The character to set (must be one of the characters of the game)
     * @param set True to set and false to erase the character
     */
    public void onCharacterChanged(int xPos, int yPos, int flags, String character, boolean set) {
    }
}
