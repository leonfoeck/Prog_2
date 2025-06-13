package gui;

import java.awt.Component;
import javax.swing.JOptionPane;
import model.Board;
import model.Player;

/**
 * A class that provides methods for displaying game dialogs to the user.
 */
final class PopUpMessages {

    /**
     * Title, which is displayed when the game is over.
     */
    private static final String GAME_OVER_TITLE = "Game Over";

    /**
     * Private constructor to prevent instantiation.
     */
    private PopUpMessages() {
    }

    /**
     * Shows a dialog displaying the outcome of the game represented by the
     * given {@link Board}.
     *
     * @param board  The game board.
     * @param parent The parent component for the dialog.
     * @throws IllegalArgumentException if the given board is {@code null}.
     */
    public static void showGameOver(Board board, Component parent) {
        if (board == null) {
            throw new NullPointerException("Board is null.");
        }
        if (board.isGameOver()) {
            Player winner = board.getWinner();
            int humanSeeds = board.getSeedsOfPlayer(Player.HUMAN);
            int machineSeeds = board.getSeedsOfPlayer(Player.MACHINE);
            if (winner == Player.NOBODY) {
                showDialog("Nobody wins. Tie with " + humanSeeds
                    + " seeds for each player.", parent, GAME_OVER_TITLE);
            } else if (winner == Player.HUMAN) {
                showDialog("Congratulations! You won with " + humanSeeds
                    + " seeds versus " + machineSeeds
                    + " seeds of the machine.", parent, GAME_OVER_TITLE);
            } else {
                showDialog("Sorry! Machine wins with " + machineSeeds
                        + " seeds versus your " + humanSeeds + ".", parent,
                    GAME_OVER_TITLE);
            }
        }
    }

    /**
     * Shows a dialog indicating that the specified player missed a turn.
     *
     * @param player The player who missed a turn.
     * @param parent The parent component for the dialog.
     * @throws IllegalArgumentException if the specified player is
     *                                  {@link Player#NOBODY} or null.
     */
    public static void showPlayerMissedTurn(Player player, Component parent) {
        if (player == Player.NOBODY || player == null) {
            throw new IllegalArgumentException("Invalid player.");
        }
        showDialog(player + " must miss a turn.", parent,
            player + " missed turn.");
    }

    /**
     * Shows a dialog with the given message and title.
     *
     * @param message The message to be displayed.
     * @param parent  The parent component for the dialog.
     * @param title   The title of the dialog.
     */
    private static void showDialog(String message, Component parent,
        String title) {
        JOptionPane.showMessageDialog(parent, message, title, 1);
    }
}
