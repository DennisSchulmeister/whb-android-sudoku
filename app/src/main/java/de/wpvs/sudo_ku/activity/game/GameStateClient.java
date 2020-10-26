package de.wpvs.sudo_ku.activity.game;

import de.wpvs.sudo_ku.model.game.GameState;

/**
 * Callback interface to hand over the game state object to the fragments and give them a simple
 * API to connect with each other. The aim here is to keep the game activity as well as the
 * fragments as loosely coupled as possible. Thus, the only way the interact with each other is
 * by a simply message exchange under control of the game activity.
 */
public interface GameStateClient {
    int MESSAGE_REFRESH_VIEWS = 1;
    int MESSAGE_FIELD_SELECTED = 2;

    /**
     * Handle passed to the clients to be able to send messages among each other.
     */
    interface GameMessageExchange {
        /**
         * Send an empty message to all clients (including oneself).
         *
         * @param what Message code (see constants)
         */
        void sendEmptyMessage(int what);

        /**
         * Send a message with field coordinates to all clients (including oneself).
         *
         * @param what Message code (see constants)
         * @param xPos Horizontal coordinate
         * @param yPos Vertical coordinate
         */
        void sendFieldMessage(int what, int xPos, int yPos);
    }

    /**
     * Called once to make the game state object available.
     *
     * @param gameState State of the current game
     * @param gameMessageExchange Object used to send message to the other clients
     */
    void setGameState(GameState gameState, GameMessageExchange gameMessageExchange);

    /**
     * Handle message sent by one of the other clients.
     *
     * @param what Message code (see constants)
     * @param xPos Horizontal field number or -1
     * @param yPos Vertical field number or -1
     */
    void onGameStateMessage(int what, int xPos, int yPos);
}