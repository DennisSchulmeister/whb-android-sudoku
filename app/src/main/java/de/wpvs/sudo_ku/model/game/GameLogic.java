package de.wpvs.sudo_ku.model.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Central entry point for the game logic. An instance of this class can be retrieved by calling
 * getGameLogic() on the game state object. Internally multiple instances of the abstract Rule
 * class are used to perform the various rule checks. But this completely invisible to the client.
 */
public class GameLogic {
    private final GameState gameState;
    private final CharacterFieldEntity[][] characterFields;
    private final List<Rule> rules = new ArrayList<>();

    /**
     * Tuple with coordinates in the game board.
     */
    public static class Coordinate {
        public int xPos = 0;
        public int yPos = 0;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Coordinate that = (Coordinate) o;

            if (xPos != that.xPos) return false;
            return yPos == that.yPos;
        }
    }

    /**
     * Constructor. Depending on the given game type a different set of rules will be activated.
     *
     * @param gameState The game for which the logic shall apply.
     */
    GameLogic(GameState gameState) {
        // Decompose the character field list into a two-dimensional array
        this.gameState = gameState;
        this.characterFields = new CharacterFieldEntity[gameState.game.size][gameState.game.size];

        for (CharacterFieldEntity characterField : gameState.characterFields) {
            this.characterFields[characterField.xPos][characterField.yPos] = characterField;
        }

        // Activate rules
        this.rules.add(new RuleNoDuplicates(gameState, this.characterFields));
        this.rules.add(new RuleUpdateProgress(gameState, this.characterFields));

        if (gameState.game.gameType == GameEntity.GameType.LETTER_GAME) {
            this.rules.add(new RuleKnownWords(gameState, this.characterFields));
        }
    }

    /**
     * Utility method for fast access to a searched field, since we already have the game fields
     * decomposed into a two-dimensional array, here.
     *
     * @param xPos Row
     * @param yPos Column
     * @return Searched field or null
     */
    public CharacterFieldEntity getCharacterField(int xPos, int yPos) {
        if ((xPos >= 0 && xPos < this.characterFields.length)
            && (yPos >= 0 && yPos < this.characterFields[xPos].length)) {

            return this.characterFields[xPos][yPos];
        }

        return null;
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
        List<GameLogic.Coordinate> coordinates = new ArrayList<>();

        for (Rule rule : this.rules) {
            for (Coordinate coordinate : rule.getFieldsWithoutDuplicates(xPos, yPos)) {
                if (!coordinates.contains(coordinate)) {
                    coordinates.add(coordinate);
                }
            }
        }

        return coordinates;
    }

    /**
     * Get a list of all characters that are allowed on the given field. The list is assembled
     * by repeatedly calling isCharacterAllowed() for each possible character in the game. Thus,
     * the client can rely on the validity of the character without calling isCharacterAllowed()
     * a second time.
     *
     * @param xPos Row
     * @param yPos Column
     * @param flags Special flags on how to treat the character (see constants)
     * @return List of allowed characters.
     */
    public List<String> getAllowedCharacters(int xPos, int yPos, int flags) {
        List<String> allowedCharacters = new ArrayList<>(this.gameState.game.size);

        for (String character : this.gameState.game.characterSet) {
            if (this.isCharacterAllowed(xPos, yPos, flags, character)) {
                allowedCharacters.add(character);
            }
        }

        return allowedCharacters;
    }

    /**
     * Check whether a given character is allowed at the given position.
     *
     * @param xPos Row
     * @param yPos Column
     * @param flags Special flags on how to treat the character (see constants)
     * @param character The character to set (must be one of the characters of the game)
     * @return true, when the character is allowed
     */
    public boolean isCharacterAllowed(int xPos, int yPos, int flags, String character) {
        for (Rule rule : this.rules) {
            if (!rule.isCharacterAllowed(xPos, yPos, flags, character)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Modify game state to place a previously validated character on the board. This will place
     * the character on the board as well as execute any further logic on the game like scanning
     * for known words in the letter game.
     *
     * It is assumed, that this method will be called only for valid characters. Therefore, no
     * checking is done here to avoid double work.
     *
     * @param xPos Row
     * @param yPos Column
     * @param flags Special flags on how to treat the character (see constants)
     * @param character The character to set (must be one of the characters of the game)
     */
    public void changeCharacter(int xPos, int yPos, int flags, String character) {
        // Put character on the board
        CharacterFieldEntity characterField;

        try {
            characterField = this.characterFields[xPos][yPos];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return;
        }

        if ((flags & GameState.FLAG_PENCIL) != 0) {
            if (!character.isEmpty()) {
                if (!characterField.pencil.contains(character)) {
                    characterField.pencil.add(character);
                }
            } else {
                characterField.pencil.remove(character);
            }
        } else if (!characterField.locked || (flags & GameState.FLAG_LOCKED) != 0) {
            characterField.character = character;
        }

        if ((flags & GameState.FLAG_LOCKED) != 0) {
            characterField.locked = true;
        }

        // Execute additional rule logic
        for (Rule rule : this.rules) {
            rule.onCharacterChanged(xPos, yPos, flags, character);
        }
    }
}

