package de.wpvs.sudo_ku.activity.game;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import de.wpvs.sudo_ku.R;
import de.wpvs.sudo_ku.model.game.CharacterFieldEntity;
import de.wpvs.sudo_ku.model.game.GameLogic;
import de.wpvs.sudo_ku.model.game.GameState;
import de.wpvs.sudo_ku.model.game.GameUtils;

/**
 * View fragment with the game controls. Listens for selection changed messages from the game
 * board to update its views and actively changes the game state when the player selects or
 * erases characters.
 */
public class GameControlsFragment extends Fragment implements GameStateClient {
    private GameState gameState;
    private GameMessageExchange gameMessageExchange;

    private int xPosSelected = -1;
    private int yPosSelected = -1;
    private int flags = GameState.FLAG_NONE;

    private ImageButton pencilButton;
    private int pencilColorActive;
    private int pencilColorInactive;
    private boolean pencil = false;

    private int characterColorActive;
    private int characterColorInactive;
    private int characterColorNotAllowed;
    private int characterColorLocked;

    private final int[] characterButtonIds = {
            R.id.game_controls_character_button_1,
            R.id.game_controls_character_button_2,
            R.id.game_controls_character_button_3,
            R.id.game_controls_character_button_4,
            R.id.game_controls_character_button_5,
            R.id.game_controls_character_button_6,
            R.id.game_controls_character_button_7,
            R.id.game_controls_character_button_8,
            R.id.game_controls_character_button_9,
            R.id.game_controls_character_button_10,
            R.id.game_controls_character_button_11,
            R.id.game_controls_character_button_12,
            R.id.game_controls_character_button_13,
            R.id.game_controls_character_button_14,
            R.id.game_controls_character_button_15,
            R.id.game_controls_character_button_16,
    };

    private final Map<String, Button> characterButtons = new HashMap<>();

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
        return inflater.inflate(R.layout.game_controls_fragment, container, false);
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

        // Resolve colors
        Resources resources = this.getResources();
        this.pencilColorActive = resources.getColor(R.color.game_pencil_button_active);
        this.pencilColorInactive = resources.getColor(R.color.game_pencil_button_inactive);

        this.characterColorActive = resources.getColor(R.color.game_character_button_active);
        this.characterColorInactive = resources.getColor(R.color.game_character_button_inactive);
        this.characterColorNotAllowed = resources.getColor(R.color.game_character_button_not_allowed);
        this.characterColorLocked = resources.getColor(R.color.game_character_button_locked);

        // Toggle pencil flag via pencil button
        this.pencilButton = view.findViewById(R.id.game_controls_fragment_pencil_button);
        this.updatePencilButton();

        this.pencilButton.setOnClickListener(v -> {
            this.pencil = !this.pencil;
            this.flags ^= GameState.FLAG_PENCIL;
            this.updatePencilButton();
        });
    }

    /**
     * Receive the game state and the message exchange object from the parent activity.
     *
     * @param gameState State of the current game
     * @param gameMessageExchange Object used to send message to the other clients
     */
    @Override
    public void setGameState(GameState gameState, GameMessageExchange gameMessageExchange) {
        // Save reference to game state
        this.gameState = gameState;
        this.gameMessageExchange = gameMessageExchange;

        // Initialize character buttons
        GameUtils.sortCharacterSet(this.gameState.game.characterSet, this.gameState.game.gameType);

        for (int i = 0; i < this.gameState.game.characterSet.size() && i < this.characterButtonIds.length; i++) {
            Button button = this.getView().findViewById(this.characterButtonIds[i]);
            String character = this.gameState.game.characterSet.get(i);

            button.setText(character);
            button.setEnabled(true);
            button.setOnClickListener(v -> this.onCharacterButtonClick(character));

            this.characterButtons.put(character, button);
        }

        this.updateCharacterButtons();
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
                this.updateCharacterButtons();
                break;
            case GameStateClient.MESSAGE_FIELD_SELECTED:
                this.xPosSelected = xPos;
                this.yPosSelected = yPos;
                this.updateCharacterButtons();
                break;
        }
    }

    /**
     * Update styling of the pencil button, depending on whether pencil mode is active.
     */
    private void updatePencilButton() {
        if (this.pencil) {
            this.pencilButton.getDrawable().setTint(this.pencilColorActive);
        } else {
            this.pencilButton.getDrawable().setTint(this.pencilColorInactive);
        }
    }

    /**
     * Update the character buttons to reflect the game state of the currently selected field.
     * If no field is selected, all buttons are inactive. Otherwise the set value is higlighted
     * and only disallowed characters are made inactive.
     */
    private void updateCharacterButtons() {
        // Get selected character field, if any
        GameLogic gameLogic = this.gameState.getGameLogic();
        CharacterFieldEntity characterField = gameLogic.getCharacterField(this.xPosSelected, this.yPosSelected);

        // Update buttons
        if (characterField == null) {
            for (Button button : this.characterButtons.values()) {
                button.setTextColor(this.characterColorInactive);
                button.setEnabled(false);
            }
        } else {
            List<String> allowedCharacters = gameLogic.getAllowedCharacters(this.xPosSelected, this.yPosSelected, this.flags);

            for (String character : this.characterButtons.keySet()) {
                Button button = this.characterButtons.get(character);
                assert button != null;

                if (character.equals(characterField.character)) {
                    if (!characterField.locked) {
                        button.setTextColor(this.characterColorActive);
                    } else {
                        button.setTextColor(this.characterColorLocked);
                    }
                } else {
                    button.setTextColor(this.characterColorInactive);
                }

                if (!allowedCharacters.contains(character)) {
                    button.setTextColor(this.characterColorNotAllowed);
                }

                button.setEnabled(allowedCharacters.contains(character) && !characterField.locked);
            }
        }
    }

    /**
     * Update game state after a button has been clicked.
     * @param character Clicked character
     */
    private void onCharacterButtonClick(String character) {
        GameLogic gameLogic = this.gameState.getGameLogic();
        CharacterFieldEntity characterField = gameLogic.getCharacterField(this.xPosSelected, this.yPosSelected);

        if (characterField == null) {
            return;
        } else if (!pencil && characterField.character.equals(character)) {
            character = "";
        } else if (pencil && characterField.pencil.contains(character)) {
            character = "";
        }

        boolean accepted = this.gameState.setCharacter(this.xPosSelected, this.yPosSelected, this.flags, character);

        if (accepted) {
            this.gameMessageExchange.sendEmptyMessage(GameStateClient.MESSAGE_REFRESH_VIEWS);
        }
    }
}
