package de.wpvs.sudo_ku.model.game;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the Sudoku rule that a character must be unique within the same column,
 * row and section.
 *
 * This class is not public, since it is only used internally by the GameLogic class.
 */
class RuleNoDuplicates extends Rule {
    private final double sectionSize;

    /**
     * Constructor.
     *
     * @param gameState The game for which the rules shall be checked
     * @param characterFields Two-dimensional view on the game board. Organized [xPos][yPos].
     */
    RuleNoDuplicates(GameState gameState, CharacterFieldEntity[][] characterFields) {
        super(gameState, characterFields);
        this.sectionSize = Math.sqrt(gameState.game.size);
    }

    /**
     * Return all fields with the same row or column or section as not being allows to contain
     * duplicate characters.
     *
     * @param xPos Row
     * @param yPos Column
     * @return Coordinates of fields needing unique characters
     */
    @Override
    public List<GameLogic.Coordinate> getFieldsWithoutDuplicates(int xPos, int yPos) {
        List<GameLogic.Coordinate> coordinates = new ArrayList<>();

        // Horizontal axis
        for (int x = 0; x < this.gameState.game.size; x++) {
            GameLogic.Coordinate coordinate = new GameLogic.Coordinate();
            coordinate.xPos = x;
            coordinate.yPos = yPos;
            coordinates.add(coordinate);
        }

        // Vertical axis
        for (int y = 0; y < this.gameState.game.size; y++) {
            if (y != yPos) {
                GameLogic.Coordinate coordinate = new GameLogic.Coordinate();
                coordinate.xPos = xPos;
                coordinate.yPos = y;
                coordinates.add(coordinate);
            }
        }

        // Section
        int xMin = (int) (Math.floor(xPos / this.sectionSize) * this.sectionSize);
        int yMin = (int) (Math.floor(yPos / this.sectionSize) * this.sectionSize);
        int xMax = xMin + (int) this.sectionSize - 1;
        int yMax = yMin + (int) this.sectionSize - 1;

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                if (x != xPos && y != yPos) {
                    GameLogic.Coordinate coordinate = new GameLogic.Coordinate();
                    coordinate.xPos = x;
                    coordinate.yPos = y;
                    coordinates.add(coordinate);
                }
            }
        }

        return coordinates;
    }

    /**
     * Check, whether the given character is already present in one of the other fields returned
     * by getFieldsWithoutDuplicates(). Unless the pencil flag is set, in which case there is no
     * restriction.
     *
     * @param xPos Row
     * @param yPos Column
     * @param flags Special flags on how to treat the character (see constants)
     * @param character The character to set (must be one of the characters of the game)
     * @param set True to set and false to erase the character
     * @return true, if the character is allowed
     */
    @Override
    public boolean isCharacterAllowed(int xPos, int yPos, int flags, String character, boolean set) {
        // Penciled in characters are always allowed
        if ((flags & GameState.FLAG_PENCIL) != 0) {
            return true;
        }

        if (!set) {
            return true;
        }

        // Normal characters must be unique in the same row, column or section
        List<GameLogic.Coordinate> coordinates = this.getFieldsWithoutDuplicates(xPos, yPos);

        for (GameLogic.Coordinate coordinate : coordinates) {
            CharacterFieldEntity characterField = this.characterFields[coordinate.xPos][coordinate.yPos];

            if ((coordinate.xPos != xPos || coordinate.yPos != yPos) && (characterField.character.equals(character))) {
                return false;
            }
        }

        return true;
    }
}
