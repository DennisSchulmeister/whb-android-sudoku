package de.wpvs.sudo_ku.activity.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.model.game.GameState;

/**
 * View fragment with a list of matched words. Passively watches the game state to display a list
 * all already matched words in the letter game. Also handles some UI interactions like clicking
 * a word to open its description.
 */
public class GameMatchedWordsFragment extends Fragment implements GameStateClient {
    GameState gameState;
    GameMessageExchange gameMessageExchange;

    /**
     * Callback to inflate the view hierarchy. To prevent crashes new views can be created here,
     * but not yet searched with findViewById(). Initialisation of the views after creation must
     * thus happen in onViewCreated().
     *
     * @param inflater UI inflater
     * @param container Parent container
     * @param savedInstanceState Saved instance state
     * @return New root view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.game_matched_words_fragment, container, false);
    }

    /**
     * Callback for further initialisation of the views, once they are completely created.
     *
     * @param view Root view
     * @param savedInstanceState Saved instance state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Receive the game state and the message exchange object from the parent activity.
     *
     * @param gameState State of the current game
     * @param gameMessageExchange Object used to send message to the other clients
     */
    @Override
    public void setGameState(GameState gameState, GameMessageExchange gameMessageExchange) {
        this.gameState = gameState;
        this.gameMessageExchange = gameMessageExchange;
    }

    /**
     * Handle messages indicating changes to the game state.
     *
     * @param what Message code (see constants)
     * @param xPos Horizontal field number or -1
     * @param yPos Vertical field number or -1
     */
    @Override
    public void onGameStateMessage(int what, int xPos, int yPos) {
        switch (what) {
            case GameStateClient.MESSAGE_REFRESH_VIEWS:
                // TODO
                break;
            case GameStateClient.MESSAGE_FIELD_SELECTED:
                // TODO
                break;
        }
    }
}
