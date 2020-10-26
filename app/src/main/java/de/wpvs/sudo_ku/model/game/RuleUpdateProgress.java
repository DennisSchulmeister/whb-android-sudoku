package de.wpvs.sudo_ku.model.game;

/**
 * This class updates the progress field in the game after each change to the game field.
 */
class RuleUpdateProgress extends Rule {
    /**
     * Constructor.
     *
     * @param gameState The game for which the rules shall be checked
     * @param characterFields Two-dimensional view on the game board. Organized [xPos][yPos].
     */
    public RuleUpdateProgress(GameState gameState, CharacterFieldEntity[][] characterFields) {
        super(gameState, characterFields);
    }

    /**
     * Update progress after a character has been placed.
     *
     * @param xPos Row
     * @param yPos Column
     * @param flags Special flags on how to treat the character (see constants)
     * @param character The character to set (must be one of the characters of the game)
     */
    @Override
    public void onCharacterChanged(int xPos, int yPos, int flags, String character) {
        if ((flags & GameState.FLAG_PENCIL) != 0) {
            return;
        }

        int amountAllFields = this.gameState.characterFields.size();
        int amountFilledFields = 0;

        for (CharacterFieldEntity characterField : this.gameState.characterFields) {
            if (!characterField.character.isEmpty()) {
                amountFilledFields += 1;
            }
        }

        this.gameState.game.progress = Math.round((float) amountFilledFields / (float) amountAllFields * 100);
    }
}
