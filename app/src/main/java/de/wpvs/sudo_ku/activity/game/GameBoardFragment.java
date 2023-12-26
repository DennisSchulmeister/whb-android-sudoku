package de.wpvs.sudo_ku.activity.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.Semaphore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.model.game.GameLogic;
import de.wpvs.sudo_ku.model.game.GameState;

/**
 * View fragment with the game board. This paints the fields and handles all direct UI interactions
 * with them (selecting a field, unselecting a field, highlighting related fields, ...).
 *
 * This implementation is using a webview to render the game board in HTML. While this definitely
 * is not the most efficient thing to do, it saves us from writing a full custom widget for the
 * time being.
 */
public class GameBoardFragment extends Fragment implements GameStateClient {
    private GameState gameState;
    private GameMessageExchange gameMessageExchange;

    private WebView webView;
    private boolean gameBoardInitialized = false;
    private final Semaphore gameStateReady = new Semaphore(0);

    /**
     * Callback methods called from the JavaScript code running in the web view. Note, that the
     * methods of this class will not run on the UI thread.
     */
    private class WebViewInterface {
        private final Context context;

        /**
         * Constructor
         * @param context Parent context
         */
        public WebViewInterface(Context context) {
            this.context = context;
        }

        /**
         * Inform all fragments that a field has been selected or unselected.
         *
         * @param xPos Horizontal coordinate
         * @param yPos Vertical coordinate
         */
        @JavascriptInterface
        public void onFieldSelected(int xPos, int yPos) {
            GameBoardFragment.this.requireActivity().runOnUiThread(() -> {
                GameBoardFragment.this.gameMessageExchange.sendFieldMessage(GameStateClient.MESSAGE_FIELD_SELECTED, xPos, yPos);
            });
        }
    }

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
        return inflater.inflate(R.layout.game_board_fragment, container, false);
    }

    /**
     * Callback for further initialisation of the views, once they are completely created.
     *
     * @param view Root view
     * @param savedInstanceState Saved instance state
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.webView = view.findViewById(R.id.game_board_fragment_webview);
        this.webView.setBackgroundColor(0);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new WebViewInterface(this.getContext()), "Android");

        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Block webview thread until the game state is available
                // Then initialize game board
                GameBoardFragment.this.gameStateReady.acquireUninterruptibly();

                int size = GameBoardFragment.this.gameState.game.size;
                int sectionSize = (int) Math.sqrt(size);

                String javascript = "createGameBoard(" + size + ", " + sectionSize + ");";
                view.evaluateJavascript(javascript, null);

                // Refresh view to display the field numbers after the board has been set up
                GameBoardFragment.this.refreshView();
            }
        });

        this.webView.loadUrl("file:///android_asset/game_board/index.html");
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
        this.gameBoardInitialized = false;
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
                this.refreshView();
                break;
            case GameStateClient.MESSAGE_FIELD_SELECTED:
                this.highlightRelatedFields(xPos, yPos);
                break;
        }
    }

    /**
     * Update the game board after the game state has changed. Also create the game board HTML
     * structure, if this has not been done, already.
     */
    private void refreshView() {
        if (!this.gameBoardInitialized) {
            this.gameBoardInitialized = true;
            this.gameStateReady.release(1);
        }

        String characterFieldsJson = new Gson().toJson(this.gameState.characterFields);
        String javascript = "updateCharacterFields(" + characterFieldsJson + ")";
        this.webView.evaluateJavascript(javascript, null);
    }

    /**
     * Respond to the player selecting a filed, by highlighting all related fields that may not
     * contain duplication characters.
     */
    private void highlightRelatedFields(int xPos, int yPos) {
        String coordinatesJson;

        if (xPos >= 0 && yPos >= 0) {
            List<GameLogic.Coordinate> coordinates = this.gameState.getGameLogic().getFieldsWithoutDuplicates(xPos, yPos);
            coordinatesJson = new Gson().toJson(coordinates);
        } else {
            coordinatesJson = "[]";
        }

        String javascript = "highlightFields(" + coordinatesJson + ")";
        this.webView.evaluateJavascript(javascript, null);
    }
}