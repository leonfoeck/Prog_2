package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import model.Board;
import model.Kalah;
import model.Player;
import observer.Observer;


/**
 * Implements the visual representation of a Kalah game. This class also
 * internally manages the game model.
 */
final class BoardPanel extends JPanel implements Observer {

    /**
     * The observable model wrapper.
     */
    private final transient DisplayData displayData;

    /**
     * The machine worker controller.
     */
    private final transient MachineWorker machineWorker = new MachineWorker();

    /**
     * Stack, which includes all boards before each human move.
     */
    private final transient Deque<Board> lastHumanMoves;

    /**
     * Sum of pits for each player.
     */
    private final transient IntSupplier pitSupplier;

    /**
     * Sum of seeds in each normal pit in a new game.
     */
    private final transient IntSupplier seedSupplier;

    /**
     * Look-ahead for the next machine move.
     */
    private final transient IntSupplier levelSupplier;

    /**
     * Responsible for enabling or disabling an undo button.
     */
    private transient Consumer<Boolean> undoButtonEnabler;

    /**
     * Array of all pits.
     */
    private BoardCell[] pitArray;

    /**
     * Attribute to store the test size of a {@code BoardCell}.
     */
    private int textSize;

    /**
     * Creates a new {@code BoardPanel}.
     *
     * @param pitSupplier   The supplier for the current sum of pits for each
     *                      player.
     * @param seedSupplier  The supplier for the initial sum of seeds in each
     *                      normal pit.
     * @param levelSupplier The supplier for the currently selected machine
     *                      skill level.
     */
    BoardPanel(IntSupplier pitSupplier, IntSupplier seedSupplier,
        IntSupplier levelSupplier) {
        if (pitSupplier == null) {
            throw new NullPointerException("PitSupplier is null.");
        } else if (seedSupplier == null) {
            throw new NullPointerException("SeedSupplier is null.");
        } else if (levelSupplier == null) {
            throw new NullPointerException("LevelSupplier is null");
        } else {
            this.seedSupplier = seedSupplier;
            this.pitSupplier = pitSupplier;
            this.levelSupplier = levelSupplier;
            displayData = new DisplayData(pitSupplier.getAsInt(),
                seedSupplier.getAsInt(), levelSupplier.getAsInt(),
                Player.HUMAN);
            lastHumanMoves = new ArrayDeque<>();
            displayData.addObserver(this);
            setBackground(Color.LIGHT_GRAY);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            createGameBoard();
            addComponentListener(new ResizeListener());
        }
    }

    /**
     * Sets the machine skill level for the next move.
     *
     * @param level The next skill level to be used, must be at least 1.
     */
    public void setLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Level is invalid.");
        } else {
            displayData.get().setLevel(level);
        }
    }

    /**
     * Starts a new machine move if the game is not over.
     */
    private void startMachineWorker() {
        if (!displayData.get().isGameOver()) {
            machineWorker.startMachineMove();
        }
    }

    /**
     * Sets up a new game board with the given pits and indices of the pits.
     */
    private void createGameBoard() {
        int pits = pitSupplier.getAsInt();
        int totalPits = pits * 2 + 2;
        pitArray = new BoardCell[totalPits];
        JPanel gridPanel = createPanel(1, pits + 2, false);
        JPanel topIndexPanel = createPanel(1, pits + 2, true);
        JPanel bottomIndexPanel = createPanel(1, pits + 2, true);
        addIndexLabels(topIndexPanel, bottomIndexPanel, totalPits, totalPits);
        gridPanel.add(createPit(0, totalPits));
        createNormalPits(gridPanel, topIndexPanel, bottomIndexPanel, totalPits,
            pits);
        addIndexLabels(topIndexPanel, bottomIndexPanel, pits + 1, pits + 1);
        gridPanel.add(createPit(0, pits + 1));
        add(topIndexPanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(bottomIndexPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates a new JPanel with the given {@code GridLayout}, sets the opaque
     * to false and sets a border if needed.
     *
     * @param rows      The sum of rows of the {@code GridLayout}.
     * @param cols      The sum of columns of the {@code GridLayout}.
     * @param setBorder Indicator if a border should be set.
     * @return The newly created panel.
     */
    private JPanel createPanel(int rows, int cols, boolean setBorder) {
        JPanel panel = new JPanel(new GridLayout(rows, cols));
        panel.setOpaque(false);
        if (setBorder) {
            panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }
        return panel;
    }

    /**
     * Creates a single pit and adds it to the {@code pitArray}.
     *
     * @param seeds    Initial seeds in the pit.
     * @param pitIndex Pit index in the game.
     * @return The newly created pit.
     */
    private BoardCell createPit(int seeds, int pitIndex) {
        BoardCell cell = new BoardCell(seeds, pitIndex);

        //Pit indices are 1-based.
        pitArray[pitIndex - 1] = cell;
        return cell;
    }

    /**
     * Adds index labels to the given index panels.
     *
     * @param topIndexPanel    The top index panel.
     * @param bottomIndexPanel The bottom index panel.
     * @param topPitNumber     The text to be added to the top index panel.
     * @param bottomPitNumber  The text to be added to the bottom index panel.
     */
    private void addIndexLabels(JPanel topIndexPanel, JPanel bottomIndexPanel,
        int topPitNumber, int bottomPitNumber) {
        topIndexPanel.add(
            new JLabel(String.valueOf(topPitNumber), SwingConstants.CENTER));
        bottomIndexPanel.add(
            new JLabel(String.valueOf(bottomPitNumber), SwingConstants.CENTER));
    }

    /**
     * Creates all the normal pits for the Kalah game.
     *
     * @param gridPanel        The panel where the pits should be added.
     * @param topIndexPanel    The index panel which displays the indices of the
     *                         top pits.
     * @param bottomIndexPanel The index panel, which displays the indices of
     *                         the bottom pits.
     * @param totalPits        Total sum of pits in the current game.
     * @param pits             Sum of pits per player.
     */
    private void createNormalPits(JPanel gridPanel, JPanel topIndexPanel,
        JPanel bottomIndexPanel, int totalPits, int pits) {
        MouseListener moveListener = new MoveListener();
        for (int i = 0; i < pits; i++) {
            int machineIndex = totalPits - 1 - i;
            int humanIndex = 1 + i;
            JPanel subPanel = createPanel(2, 1, false);
            addIndexLabels(topIndexPanel, bottomIndexPanel, machineIndex,
                humanIndex);
            subPanel.add(createPit(seedSupplier.getAsInt(), machineIndex));
            BoardCell humanCell = createPit(seedSupplier.getAsInt(),
                humanIndex);
            humanCell.addMouseListener(moveListener);
            subPanel.add(humanCell);
            gridPanel.add(subPanel);
        }
    }


    /**
     * Stops the machine move that is currently being calculated.
     */
    public void stopMachineWorker() {
        machineWorker.stop();
    }

    /**
     * Informs this {@code BoardPanel} that the board may have been changed.
     */
    @Override
    public void update() {
        checkGameOver();
        for (int i = 0; i < pitArray.length; i++) {
            Board board = displayData.get();
            pitArray[i].highlight = i == board.targetPitOfLastMove()
                || i == board.sourcePitOfLastMove();
            pitArray[i].setText(String.valueOf(board.getSeeds(i)));
        }
        repaint();
    }

    /**
     * Checks if the game is over and performs necessary steps accordingly.
     */
    private void checkGameOver() {
        if (displayData.get().isGameOver()) {
            SwingUtilities.invokeLater(
                () -> PopUpMessages.showGameOver(displayData.get(),
                    getTopLevelAncestor()));
        }
    }

    /**
     * Checks if a player missed its turn and performs necessary steps
     * accordingly.
     *
     * @param player The player to be checked for.
     * @param board  The current game.
     * @return true if the player missed its turn false if the game is already
     *         over or the player didn't miss its turn.
     */
    private boolean hasPlayerMissedTurn(Player player, Board board) {
        if (!board.isGameOver() && player != board.next()) {
            SwingUtilities.invokeLater(
                () -> PopUpMessages.showPlayerMissedTurn(player,
                    getTopLevelAncestor()));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates and returns an {@code ActionListener} that creates a new game,
     * when its {@code actionPerformed} method is called.
     *
     * @return The created {@code ActionListener}.
     */
    public ActionListener createNewGameListener() {
        return new NewGameListener(false);
    }

    /**
     * Creates and returns an {@code ActionListener} that creates a new game
     * when its {@code actionPerformed} method is called.
     *
     * @param undoButtonEnabler A consumer object, responsible for enabling or
     *                          disabling an undo button.
     * @return The created {@code ActionListener}.
     */
    public ActionListener createUndoListener(
        Consumer<Boolean> undoButtonEnabler) {
        if (undoButtonEnabler == null) {
            throw new NullPointerException("UndoButtonEnabler is null.");
        }
        this.undoButtonEnabler = undoButtonEnabler;
        return new UndoListener();
    }

    /**
     * Creates and returns an {@code ActionListener} that creates a new game
     * with old pits and seeds and switched opening player, when its
     * {@code actionPerformed} method is called.
     *
     * @return The created {@code ActionListener}.
     */
    public ActionListener createSwitchListener() {
        return new NewGameListener(true);
    }

    /**
     * Represents a single pit for a player on the board.
     */
    private class BoardCell extends JLabel {

        /**
         * Default height and width for this JLabel.
         */
        private static final int DEFAULT_SIZE = 100;

        /**
         * Pit index of this pit in the given game.
         */
        private final int pitNumber;

        /**
         * Indicator if this cell should be highlighted.
         */
        private boolean highlight;

        /**
         * Creates a new {@code BoardCell}, which represents a pit in the Kalah
         * game.
         *
         * @param seedNumber Initial sum of seeds in this pit.
         * @param pitNumber  Pit index of this pit in the given game.
         */
        BoardCell(int seedNumber, int pitNumber) {
            super(Integer.toString(seedNumber), SwingConstants.CENTER);

            // Pit indices are 1-based.
            this.pitNumber = pitNumber - 1;
            highlight = false;
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setPreferredSize(new Dimension(DEFAULT_SIZE, DEFAULT_SIZE));
            setForeground(Color.BLACK);
        }

        /**
         * Draws this {@code BoardCell}, sets the text size depending on the
         * window size and sets the opaque if necessary.
         *
         * @param graphics The {@code Graphics} object used for painting.
         */
        @Override
        protected void paintComponent(Graphics graphics) {
            setFont(
                new Font(getFont().getName(), getFont().getStyle(), textSize));
            setOpaque(highlight);
            super.paintComponent(graphics);
        }
    }

    /**
     * Listens for an action that calls for the creation of a new game.
     */
    private class NewGameListener implements ActionListener {

        /**
         * Stores if the first player of the new game will be switched.
         */
        private final boolean switchFirstPlayer;

        /**
         * Creates a new {@code NewGameListener}.
         *
         * @param switchFirstPlayer Indicator if the first player should be
         *                          switched.
         */
        NewGameListener(boolean switchFirstPlayer) {
            this.switchFirstPlayer = switchFirstPlayer;
        }

        /**
         * Creates a new game and performs the machines move, if it is the
         * machines turn.
         *
         * @param actionEvent The event details which are ignored.
         */
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            stopMachineWorker();
            lastHumanMoves.clear();
            undoButtonEnabler.accept(false);
            Player openingPlayer = displayData.get().getOpeningPlayer();
            int pits = pitSupplier.getAsInt();
            int seeds = seedSupplier.getAsInt();
            if (!switchFirstPlayer) {
                removeAll();
                createGameBoard();
                revalidate();
            } else {
                openingPlayer = openingPlayer.getOpponent();
                pits = displayData.get().getPitsPerPlayer();
                seeds = displayData.get().getSeedsPerPit();
            }
            displayData.set(new Kalah(pits, seeds, levelSupplier.getAsInt(),
                openingPlayer));
            if (openingPlayer == Player.MACHINE) {
                startMachineWorker();
            }
        }
    }

    /**
     * Listens for a resizing event to recalculate the text size of a
     * {@code BoardCell}.
     */
    private class ResizeListener extends ComponentAdapter {

        /**
         * Recalculates the text size of a {@code BoardCell}, depending on the
         * width of the current window.
         *
         * @param event The event details, which are ignored.
         */
        @Override
        public void componentResized(ComponentEvent event) {
            if (getComponents() == null || getComponents().length <= 1) {
                throw new RuntimeException(
                    "There are not enough cells on the board.");
            } else {
                textSize = getComponent(1).getWidth() / 100 + 8;
            }
        }

    }

    /**
     * Listens for an action that calls for the undo of the last human move.
     */
    private class UndoListener implements ActionListener {

        /**
         * Undoes the last human move, if their already was a human move.
         *
         * @param actionEvent The event details which are ignored.
         */
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            stopMachineWorker();
            if (!lastHumanMoves.isEmpty()) {
                displayData.set(lastHumanMoves.pop());
                undoButtonEnabler.accept(!lastHumanMoves.isEmpty());
            }
        }
    }

    /**
     * Listens to the moves performed by the human player.
     */
    private class MoveListener extends MouseAdapter {

        /**
         * Performs a move by the human if possible and starts the next machine
         * move.
         *
         * @param event The event details.
         */
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event != null && event.getSource() instanceof BoardCell cell) {
                if (!machineWorker.isRunning() && !displayData.get()
                    .isGameOver()) {
                    Board beforeMove = displayData.get();
                    Board boardAfterMove = beforeMove.move(cell.pitNumber);
                    if (boardAfterMove != null) {
                        lastHumanMoves.push(beforeMove.clone());
                        undoButtonEnabler.accept(true);
                        displayData.set(boardAfterMove);
                        if (!hasPlayerMissedTurn(Player.MACHINE,
                            displayData.get())) {
                            startMachineWorker();
                        }
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }
    }

    /**
     * Implements a worker thread to simulate a machine move in a Kalah game. It
     * starts a new thread using {@link #startMachineMove()}.
     */
    private class MachineWorker {

        /**
         * Represents the worker thread.
         */
        private Thread machineThread = null;

        /**
         * Starts a new thread to simulate a machine move. If the game is over,
         * an IllegalStateException is thrown. The new thread will call
         * {@link #calculateMachineMove()}.
         *
         * @throws IllegalStateException if the game is already over.
         */
        public void startMachineMove() {
            if (displayData.get().isGameOver()) {
                throw new IllegalStateException("Game is over.");
            } else {
                machineThread = new Thread(this::calculateMachineMove);
                machineThread.start();
            }
        }

        /**
         * Calculates and sets the next machine move by calling
         * {@link #calculateMove(Board)}. The calculation continues until a
         * valid move is not found or the human didn't miss their turn.
         */
        private void calculateMachineMove() {
            Board result = displayData.get().clone();
            do {
                result = calculateMove(result);
                if (result != null) {
                    displayData.set(result);
                    result.setLevel(levelSupplier.getAsInt());
                }
            } while (result != null && hasPlayerMissedTurn(Player.HUMAN,
                result));
        }

        /**
         * Calculates a machine move on the provided board and returns the
         * result. If the calculation takes less than 1000 milliseconds, the
         * thread will sleep for the remaining time.
         *
         * @param board The board to make the move on.
         * @return The result of the move, or null if the thread was
         *         interrupted.
         */
        private Board calculateMove(Board board) {
            long start = System.currentTimeMillis();
            try {
                Board result = board.machineMove();
                long end = System.currentTimeMillis();
                long duration = end - start;
                if (duration < 1000) {
                    Thread.sleep(1000 - duration);
                }
                return result;
            } catch (InterruptedException e) {
                return null;
            }
        }

        /**
         * Returns whether the {@code machineThread} is running.
         *
         * @return true if the {@code machineThread} is not null and alive,
         *         false otherwise.
         */
        public boolean isRunning() {
            return machineThread != null && machineThread.isAlive();
        }

        /**
         * Stops the machineThread if it is not null.
         */
        public void stop() {
            if (machineThread != null) {
                machineThread.interrupt();
            }
        }
    }
}
