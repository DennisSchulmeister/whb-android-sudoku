package de.wpvs.sudo_ku.model;

/**
 * Enumeration of all implemented game variants. This is used to be able to correctly persist a
 * running game in the database.
 */
public enum GameType {
    /**
     * Simplified Sudoku with 4 times 4 numbers
     */
    SUDOKU_4x4,

    /**
     * Classical SUdoku with 9 times 9 numbers
     */
    SUDOKU_9x9,

    /**
     * Crossword-style sudoku with Unix commands instead of numbers
     */
    CROSSWORD,
}
