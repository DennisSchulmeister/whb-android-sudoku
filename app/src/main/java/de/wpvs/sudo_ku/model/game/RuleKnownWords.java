package de.wpvs.sudo_ku.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.wpvs.sudo_ku.thread.ThreadMutex;
import de.wpvs.sudo_ku.thread.database.DatabaseThread;
import de.wpvs.sudo_ku.thread.database.MatchKnownWords;

/**
 * This class implements the extended rule of the letter game which highlights known words in the
 * game board. For this it is checked, whether the last put character is part of at least one known
 * word horizontally or vertically.
 *
 * This class is not public, since it is only used internally by the GameLogic class.
 */
class RuleKnownWords extends Rule {
    /**
     * Constructor.
     *
     * @param gameState The game for which the rules shall be checked
     * @param characterFields Two-dimensional view on the game board. Organized [xPos][yPos].
     */
    RuleKnownWords(GameState gameState, CharacterFieldEntity[][] characterFields) {
        super(gameState, characterFields);
    }

    /**
     * Check the given character and either erase all matched words that have been existing at
     * the changed field (when character is empty) or search for machtes (otherwise).
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

        this.eraseMatchedWords(xPos, yPos);

        if (!character.isEmpty()) {
            this.matchKnownWords(xPos, yPos);
        }
    }

    /**
     * Erase all matched words which contained the given field because the character has been
     * erased.
     *
     * @param xPos Row
     * @param yPos Column
     */
    private void eraseMatchedWords(int xPos, int yPos) {
        CharacterFieldEntity erasedCharacterField = this.characterFields[xPos][yPos];

        for (int wordNumber : erasedCharacterField.words.toArray(new Integer[0])) {
            // Horizontal axis
            for (int x = 0; x < this.gameState.game.size; x++) {
                this.characterFields[x][yPos].words.remove(Integer.valueOf(wordNumber));
            }

            // Vertical axis
            for (int y = 0; y < this.gameState.game.size; y++) {
                this.characterFields[xPos][y].words.remove(Integer.valueOf(wordNumber));
            }

            // Word list
            Iterator<WordEntity> iterator = this.gameState.words.iterator();

            while (iterator.hasNext()) {
                WordEntity word = iterator.next();
                
                if (word.wordNumber == wordNumber) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Check whether the given field is part of at least one known word. If so mark all fields of
     * the found words with the found words.
     *
     * @param xPos Row
     * @param yPos Column
     */
    private void matchKnownWords(int xPos, int yPos) {
        // Collect words to search along vertical axis
        Map<String, List<CharacterFieldEntity>> searchedWords = new HashMap<>();

        for (int yStart = 0; yStart <= yPos; yStart++) {
            for (int yStop = this.gameState.game.size - 1; yStop >= yPos; yStop--) {
                StringBuilder word = new StringBuilder();
                List<CharacterFieldEntity> characterFields = new ArrayList<>(yStop - yStart + 1);
                boolean skip = false;

                for (int y = yStart; y <= yStop; y++) {
                    CharacterFieldEntity characterField = this.characterFields[xPos][y];

                    if (characterField.character.isEmpty()) {
                        skip = true;
                        break;
                    }

                    characterFields.add(characterField);
                    word.append(characterField.character);
                }

                if (skip) {
                    continue;
                }

                if (searchedWords.containsKey(word.toString())) {
                    searchedWords.get(word.toString()).addAll(characterFields);
                } else {
                    searchedWords.put(word.toString(), characterFields);
                }
            }
        }

        // Collect words to search along horizontal axis
        for (int xStart = 0; xStart <= xPos; xStart++) {
            for (int xStop = this.gameState.game.size - 1; xStop >= xPos; xStop--) {
                StringBuilder word = new StringBuilder();
                List<CharacterFieldEntity> characterFields = new ArrayList<>(xStop - xStart + 1);
                boolean skip = false;

                for (int x = xStart; x <= xStop; x++) {
                    CharacterFieldEntity characterField = this.characterFields[x][yPos];

                    if (characterField.character.isEmpty()) {
                        skip = true;
                        break;
                    }

                    characterFields.add(characterField);
                    word.append(characterField.character);
                }

                if (skip) {
                    continue;
                }

                if (searchedWords.containsKey(word.toString())) {
                    searchedWords.get(word.toString()).addAll(characterFields);
                } else {
                    searchedWords.put(word.toString(), characterFields);
                }
            }
        }

        // Run task on the database thread to search matches and update the game state accordingly
        MatchKnownWords<List<CharacterFieldEntity>> task = new MatchKnownWords<>(searchedWords);
        ThreadMutex threadMutex = this.gameState.getThreadMutex();

        task.setCallback(new MatchKnownWords.Callback<List<CharacterFieldEntity>>() {
            @Override
            public void receiveResult(Map<String, List<CharacterFieldEntity>> matchedWords) {
                if (threadMutex != null) {
                    threadMutex.lock();
                }

                for (String word : matchedWords.keySet()) {
                    // Find already existing word number or insert new word
                    int highestWordNumber = 0;
                    int wordNumber = -1;
                    boolean wordAlreadyExsits = false;

                    for (WordEntity wordEntity: RuleKnownWords.this.gameState.words) {
                        highestWordNumber = Math.max(highestWordNumber, wordEntity.wordNumber);

                        if (wordEntity.word.equals(word)) {
                            wordAlreadyExsits = true;
                            wordNumber = wordEntity.wordNumber;
                            break;
                        }
                    }

                    if (!wordAlreadyExsits) {
                        wordNumber = highestWordNumber + 1;

                        WordEntity wordEntity = new WordEntity();
                        wordEntity.gameUid = RuleKnownWords.this.gameState.game.uid;
                        wordEntity.word = word;
                        wordEntity.wordNumber = wordNumber;

                        RuleKnownWords.this.gameState.words.add(wordEntity);
                    }

                    // Mark all fields that make up the word
                    for (CharacterFieldEntity characterField : matchedWords.get(word)) {
                        characterField.words.add(wordNumber);
                    }
                }

                if (threadMutex != null) {
                    threadMutex.release();
                }
            }
        });

        DatabaseThread.getInstance().post(task);
    }
}
