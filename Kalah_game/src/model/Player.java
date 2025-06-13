package model;

/**
 * Represents a player in the Kalah game. There are two players: {@code HUMAN},
 * and {@code MACHINE} and a state which represents a drawn game
 * {@code NOBODY}.
 */
public enum Player {

    /**
     * Represents the human player.
     */
    HUMAN("You"),

    /**
     * Represents the machine player.
     */
    MACHINE("Machine"),

    /**
     * Represents a draw in the game.
     */
    NOBODY(null) {
        /**
         * Always throws illegal state Exception this method should not be
         * called.
         * @return Throws IllegalStateException.
         */
        @Override
        public String toString() {
            throw new IllegalStateException("Nobody hasn't got a display name");
        }
    };

    /**
     * The display name for this player.
     */
    private final String displayName;

    /**
     * Constructs a new player with the given display name.
     *
     * @param displayName the display name for this player
     */
    Player(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the opponent of this player.
     *
     * @return The opponent of this player. {@code HUMAN} if this player is
     *         {@code MACHINE}, {@code MACHINE} if this player is {@code HUMAN}.
     * @throws IllegalStateException If the method is called on
     *                               <code>NOBODY</code>.
     */
    public Player getOpponent() {
        return switch (this) {
            case MACHINE -> HUMAN;
            case HUMAN -> MACHINE;
            default -> throw new IllegalStateException(
                "Nobody hasn't got an opponent.");
        };
    }

    /**
     * Returns a string representation of this player.
     *
     * @return the display name of this player.
     */
    @Override
    public String toString() {
        return displayName;
    }
}