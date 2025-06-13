package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import model.Board;
import model.Kalah;

/**
 * JFrame that represents the Kalah game. It contains a {@code BoardPanel} that
 * displays the game board. It also has several combo boxes to set the number of
 * pits, seeds per pit, and the machine player's level. Additionally, it has
 * buttons for a new game, switch player, undo, and quit operation.
 */
final class BoardFrame extends JFrame {

    /**
     * Maximum sum of pits and seeds for the given game.
     */
    private static final int MAX_PITS_SEEDS = 20;

    /**
     * Maximum level of the machine, to avoid long calculations.
     */
    private static final int MAX_LEVEL = 12;

    /**
     * The {@code JPanel} of the game board.
     */
    private final BoardPanel boardPanel;

    /**
     * The {@code BoardComboBox} representing the number of pits.
     */
    private final BoardComboBox pitsBox;

    /**
     * The {@code BoardComboBox} representing the number of seeds per pit.
     */
    private final BoardComboBox seedsBox;

    /**
     * The {@code BoardComboBox} representing the level of the machine player.
     */
    private final BoardComboBox levelBox;

    /**
     * Creates a new {@code BoardFrame}.
     */
    BoardFrame() {
        super("Kalah");
        seedsBox = new BoardComboBox(MAX_PITS_SEEDS,
            Board.DEFAULT_SEEDS_PER_PIT);
        levelBox = new BoardComboBox(MAX_LEVEL, Kalah.DEFAULT_MACHINE_LEVEL);
        pitsBox = new BoardComboBox(MAX_PITS_SEEDS,
            Board.DEFAULT_PITS_PER_PLAYER);
        boardPanel = new BoardPanel(pitsBox::getInt, seedsBox::getInt,
            levelBox::getInt);
        levelBox.addActionListener(e -> boardPanel.setLevel(levelBox.getInt()));
        setLayout(new BorderLayout());
        add(boardPanel, BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);
        initializeQuitOperation();
    }

    /**
     * Builds the lower bar and returns it as a {@code JPanel}.
     *
     * @return The created button bar.
     */
    private JPanel buildButtonBar() {
        JPanel buttonBar = new JPanel(new FlowLayout());
        buttonBar.add(new JLabel("p:"));
        buttonBar.add(pitsBox);
        buttonBar.add(new JLabel("s:"));
        buttonBar.add(seedsBox);
        buttonBar.add(new JLabel("l:"));
        buttonBar.add(levelBox);
        buttonBar.add(createButton("New", KeyEvent.VK_N,
            boardPanel.createNewGameListener(), false));
        buttonBar.add(createButton("Switch", KeyEvent.VK_S,
            boardPanel.createSwitchListener(), false));
        buttonBar.add(createButton("Undo", KeyEvent.VK_U, null, true));
        buttonBar.add(createButton("Quit", KeyEvent.VK_Q, e -> {
            boardPanel.stopMachineWorker();
            dispose();
        }, false));

        return buttonBar;
    }

    /**
     * Creates a standard JButton with a shortcut key and an
     * {@code ActionListener}.
     *
     * @param name       The name of the button.
     * @param keyEvent   The shortcut key.
     * @param listener   The {@code ActionListener}.
     * @param undoButton Indicator if the button should be an undo Button.
     * @return The newly created Button.
     */
    private JButton createButton(String name, int keyEvent,
        ActionListener listener, boolean undoButton) {
        JButton button = new JButton(name);
        button.setMnemonic(keyEvent);
        button.setDisplayedMnemonicIndex(0);
        if (undoButton) {
            button.addActionListener(
                boardPanel.createUndoListener(button::setEnabled));
            button.setEnabled(false);
        } else {
            button.addActionListener(listener);
        }
        return button;
    }

    /**
     * Initializes the close operation of this {@code JFrame}.
     */
    private void initializeQuitOperation() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                boardPanel.stopMachineWorker();
            }
        });
    }
}
