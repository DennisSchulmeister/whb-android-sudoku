package de.wpvs.sudo_ku.model.game;

import android.content.Context;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;
import de.wpvs.sudo_ku.MyApplication;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.thread.ThreadMutex;

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
    public List<CharacterFieldEntity> characterFields = new LinkedList<>();

    /**
     * Detected words and their associated word number in the game fields.
     */
    @Relation(parentColumn = "uid", entityColumn = "gameUid")
    public List<WordEntity> words = new LinkedList<>();

    /**
     * Error codes, why a game is inconsistent and should not be saved.
     */
    public enum Error {
        ERROR_CHARSET_SIZE,
    }

    public static final int FLAG_NONE = 0;
    public static final int FLAG_PENCIL = 1;
    public static final int FLAG_LOCKED = 2;

    /**
     * Game logic. Will be instantiated on the first access via getGameLogic(). This is done like
     * this, because we need all entities to be fully loaded before accessing the game logic.
     */
    @Ignore
    private GameLogic _gameLogic = null;

    @Ignore
    private ThreadMutex threadMutex = null;

    /**
     * Default constructor.
     */
    public GameState() {
    }

    /**
     * Copy constructor. Creates a deep clone of the given game state, including copies of all
     * contained entities, excluding all other objects like the game logic and thread mutex.
     *
     * @param that Entity to copy
     */
    public GameState(GameState that) {
        this.game = new GameEntity(that.game);

        for (CharacterFieldEntity characterField : that.characterFields) {
            this.characterFields.add(new CharacterFieldEntity(characterField));
        }

        for (WordEntity word : that.words) {
            this.words.add(new WordEntity(word));
        }
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
        gameState.game.difficulty   = Math.max(33, (int) Math.floor(Math.random() * 101));

        gameState.characterFields = GameUtils.createCharacterFields(gameState.game.size);
        GameUtils.prepopulateCharacterFields(gameState);

        return gameState;
    }

    /**
     * Factory method for a new game with predefined configuration. Before saving the method
     * checkGameConsistency() should be called to be sure that the configuration is valid.
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
     * Set a callback object that can be used to temporarily block the foreground thread (usually
     * the UI thread), when the game state must be touched by a background thread. This is
     * unfortunately necessary for the letter game, where database queries are used to search
     * for matches. Once the database queries finish, the game state must be updated for a
     * very short time.
     *
     * The effect is almost the same as if the background thread simply passed a Runnable to be
     * executed in the main thread. But handling is a bit easier for the background process since
     * only ThreadMutex.lock() and ThreadMutex.release() must be called.
     *
     * @param threadMutexCallback Callback object
     */
    public void setThreadMutexCallback(ThreadMutex.Callback threadMutexCallback) {
        this.threadMutex = new ThreadMutex(threadMutexCallback);
    }

    /**
     * Get the thread mutex to synchronize with the main thread, provided the main thread has
     * registered a callback.
     *
     * @return Thread mutex or null, if no callback was registered
     */
    public ThreadMutex getThreadMutex() {
        return this.threadMutex;
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
     * Access the game logic implementation that is used to check all the game rules. Always use
     * this method to access the game logic to make sure, it is actually instantiated.
     *
     * @return Object to check the game rules
     */
    public GameLogic getGameLogic() {
        if (this._gameLogic == null) {
            this._gameLogic = new GameLogic(this);
        }

        return this._gameLogic;
    }

    /**
     * Change a character in the character fields, making sure that the game logic is applied
     * and only valid characters are accepted. This is the preferred way to set a character during
     * the game, instead of direct manipulation of the character field list.
     *
     * @param xPos Row
     * @param yPos Column
     * @param flags Special flags on how to treat the character (see constants)
     * @param character The character to set (must be one of the characters of the game)
     * @param set True to set and false to erase the character
     * @return true, when the character has been accepted
     */
    public boolean setCharacter(int xPos, int yPos, int flags, String character, boolean set) {
        GameLogic gameLogic = this.getGameLogic();

        if (gameLogic.isCharacterAllowed(xPos, yPos, flags, character, set)) {
            gameLogic.changeCharacter(xPos, yPos, flags, character, set);
            return true;
        }

        return false;
    }
}
