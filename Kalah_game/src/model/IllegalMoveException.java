package model;

import java.io.Serial;

/**
 * Thrown to indicate that there was an attempt to perform a move when the game
 * was over or by a player, while it was not that player's turn.
 *
 * @see Board#move(int)
 * @see Board#machineMove()
 */
public class IllegalMoveException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code IllegalMoveException} with no detail message.
     */
    public IllegalMoveException() {
        super();
    }

    /**
     * Constructs an {@code IllegalMoveException} with the specified detail
     * message.
     *
     * @param message The detail message.
     */
    public IllegalMoveException(String message) {
        super(message);
    }
}