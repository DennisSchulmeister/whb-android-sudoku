package de.wpvs.sudo_ku.storage;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.room.Embedded;
import androidx.room.Relation;
import de.wpvs.sudo_ku.MyApplication;
import de.wpvs.sudo_ku.R;

/**
 * The class encapsulates all entities (data transfer objects) that make up a saved game. This is
 * used to persist games in the database, so that they can be resumed even days or weeks after
 * they have been started. This is also the data structure used internally by the game logic to
 * represent the game state (hence the class name).
 */
public class GameState {
    /**
     * Head data.
     */
    @Embedded
    public GameEntity game = new GameEntity();

    /**
     * Values of the individual game fields.
     *
     * NOTE: Don't use this to directly change the field characters during the game but use
     * method setCharacter() below, to make sure that the game logic is applied and only
     * valid characters are accepted.
     */
    @Relation(parentColumn = "uid", entityColumn = "gameUid")
    List<CharacterFieldEntity> characterFields = new ArrayList<>();

    /**
     * Detected words and their associated word number in the game fields.
     */
    @Relation(parentColumn = "uid", entityColumn = "gameUid")
    List<WordEntity> words = new ArrayList<>();

    /**
     * Error codes, why a game is inconsistent and should not be saved.
     */
    public enum Error {
        ERROR_CHARSET_SIZE,
    }

    /**
     * Factory method for a new game with random configuration. The returned object will be
     * consistent in all regards and can directly be saved.
     *
     * @return New game with random parameters
     */
    public static GameState createRandomGame() {
        Context context     = MyApplication.getInstance();
        GameState gameState = new GameState();

        String[] gameTypes  = context.getResources().getStringArray(R.array.game_type_keys);
        String[] boardSizes = context.getResources().getStringArray(R.array.board_size_keys);

        int gameTypeIndex  = (int) Math.floor(Math.random() * gameTypes.length);
        int boardSizeIndex = (int) Math.floor(Math.random() * boardSizes.length);

        gameState.game.gameType     = GameEntity.GameType.valueOf(gameTypes[gameTypeIndex]);
        gameState.game.size         = Integer.parseInt(boardSizes[boardSizeIndex]);
        gameState.game.characterSet = GameUtils.createCharacterSet(gameState.game.gameType, gameState.game.size);
        gameState.game.difficulty   = Math.max(0.33f, (float) Math.random());

        gameState.characterFields = GameUtils.createCharacterFields(gameState.game.size);
        GameUtils.prepopulateCharacterFields(gameState);

        return gameState;
    }

    /**
     * Factory method for a new game with predefined configuration. The returned object will be
     * consistent in all regards and can directly be saved.
     *
     * @param gameEntity Game configuration (will be directly used in the new GameState instance)
     * @return New game with the given game configuration
     */
    public static GameState createParametrizedGame(GameEntity gameEntity) {
        GameState gameState = new GameState();
        gameState.game = gameEntity;

        gameState.characterFields = GameUtils.createCharacterFields(gameState.game.size);
        GameUtils.prepopulateCharacterFields(gameState);

        return gameState;
    }

    /**
     * Check that all parameters are consistent and the game is safe to be saved. Note, that this
     * currently only checks that the amount of available characters matches the game board size.
     * Especially it is not checked, that every parameters has a value, since each parameter has
     * an assigned default value, that is set when the new GameEntity object is created.
     *
     * @return A list of all found errors
     */
    public Map<Error, String> checkGameConsistency() {
        Map<Error, String> errors = new HashMap<>();
        Context context = MyApplication.getInstance();

        if (this.game.characterSet == null || this.game.characterSet.size() != this.game.size) {
            String message = "";

            switch (this.game.gameType) {
                case NUMBER_GAME:
                    message = context.getString(R.string.new_game_error_wrong_amount_of_numbers, this.game.size);
                    break;
                case LETTER_GAME:
                    message = context.getString(R.string.new_game_error_wrong_amount_of_letters, this.game.size);
                    break;
            }

            errors.put(Error.ERROR_CHARSET_SIZE, message);
        }

        return errors;
    }

    /**
     * Change a character in the character fields, making sure that the game logic is applied
     * and only valid characters are accepted. This is the preferred way to set a character during
     * the game, instead of directly manipulation the character field list.
     *
     * @param xPos Row
     * @param yPos Column
     * @param pencil Whether to pencil in the character (true) or really set it (false)
     * @param character The character to set (must be one of the characters of the game)
     * @return true, when the character has been accepted
     */
    public boolean setCharacter(int xPos, int yPos, boolean pencil, String character) {
        // TODO
        return true;
    }
}
