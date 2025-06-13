package gui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Contains the entry point for the GUI program. It starts the Kalah GUI by
 * calling the {@code createAndShowGUI} method, which creates and displays the
 * GUI window.
 */
public final class Main {

    /**
     * An empty and private constructor to prevent instantiation of this class.
     */
    private Main() {
    }

    /**
     * Responsible for starting the GUI program. It does this by invoking the
     * createAndShowGUI method using SwingUtilities.invokeLater.
     *
     * @param args Unused arguments for the main method.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    /**
     * Spawns and displays the Kalah GUI in the middle of the screen.
     */
    private static void createAndShowGUI() {
        JFrame window = new BoardFrame();
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}
