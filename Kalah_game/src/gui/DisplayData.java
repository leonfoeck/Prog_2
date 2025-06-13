package gui;

import model.Board;
import model.Kalah;
import model.Player;
import observer.Observable;

/**
 * A wrapper for a {@link Board} object that can be observed.
 *
 * @see Board
 * @see Observable
 */
final class DisplayData extends Observable {

    /**
     * The wrapped {@code Board} object.
     */
    private Board board;

    /**
     * Constructs a new instance of the DisplayData class.
     *
     * @param pitsPerPlayer The number of pits per player.
     * @param seedsPerPit   The number of seeds per pit.
     * @param level         The game level.
     * @param openingPlayer The opening player.
     * @throws IllegalArgumentException if the number of pits per player is less
     *                                  than 1, the number of seeds per pit is
     *                                  less than 1, or the opening player is
     *                                  {@code null}.
     */
    DisplayData(int pitsPerPlayer, int seedsPerPit, int level,
        Player openingPlayer) {
        if (pitsPerPlayer < 1) {
            throw new IllegalArgumentException(
                "Number of pits per player must be at least 1.");
        } else if (seedsPerPit < 1) {
            throw new IllegalArgumentException(
                "Number of seeds per pit must be at least 1.");
        } else if (level < 1) {
            throw new IllegalArgumentException("Level must be at least 1.");
        } else if (openingPlayer == null) {
            throw new NullPointerException("Opening player cannot be null.");
        } else {
            set(new Kalah(pitsPerPlayer, seedsPerPit, level, openingPlayer));
        }
    }

    /**
     * Sets the wrapped {@code Board} object.
     *
     * @param newBoard The new instance.
     */
    public void set(Board newBoard) {
        if (newBoard == null) {
            throw new NullPointerException("NewBoard is null.");
        } else {
            board = newBoard;
            notifyObservers();
        }
    }

    /**
     * Gets the wrapped {@code Board} object.
     *
     * @return The stored instance, is never {@code null}.
     */
    public Board get() {
        return board;
    }

}
