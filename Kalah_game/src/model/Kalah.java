package model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The Kalah class represents a board game known as Kalah, which is a two-player
 * strategy game that involves seeds and pits. The board consists of a number of
 * pits, with each player having a set of pits on their side. Players take turns
 * sowing their seeds, starting from one of their pits, and moving seeds one at
 * a time to the next pit in a clockwise direction. If the last seed is placed
 * in an empty pit on the player's side, the player captures all the seeds in
 * the opposite pit and places them in their own pit. The game ends when all the
 * pits on one player's side are empty. The player with the most seeds in their
 * pits wins the game.
 */
public class Kalah implements Board {

    /**
     * The default level of difficulty for the machine player.
     */
    public static final int DEFAULT_MACHINE_LEVEL = 3;

    /**
     * The initial number of seeds in the winning pits for both players.
     */
    private static final int INITIAL_SEEDS_IN_WINNING_PITS = 0;

    /**
     * The default values for the source and target pit of the last move, if no
     * moves have been made.
     */
    private static final int NO_MOVES_DEFAULT = -1;

    /**
     * The multiplicator used to calculate the score for the human player.
     */
    private static final double HUMAN_SCORE_MULTIPLICATOR = 1.5;

    /**
     * The message to be displayed when an invalid pit is chosen.
     */
    private static final String INVALID_PIT_MESSAGE = "Invalid pit %d.";

    /**
     * The opening player is the player who makes the first move of the game.
     */
    private final Player openingPlayer;

    /**
     * The starting number of seeds in each pit at the beginning of the game.
     */
    private final int startingSeedsInPits;

    /**
     * The number of pits on each player's side of the board.
     */
    private final int pitsPerPlayer;

    /**
     * The total number of pits on the board, including the winning pits for
     * each player.
     */
    private final int totalPits;

    /**
     * The total number of seeds on the board at the beginning of the game.
     */
    private final int totalSeeds;

    /**
     * The level of difficulty for the machine player.
     */
    private int machineLevel;

    /**
     * The player who is currently making a move.
     */
    private Player currentPlayer;

    /**
     * An array containing the number of seeds in each pit on the board.
     */
    private int[] seeds;

    /**
     * The index of the pit from which the last move was made.
     */
    private int sourcePitOfLastMove;

    /**
     * The index of the pit to which the last move was made.
     */
    private int targetPitOfLastMove;

    /**
     * The player who has won the game, if the game is over.
     */
    private Player winner;

    /**
     * A set containing the indices of the pits that the human player can choose
     * from when making a move.
     */
    private Set<Integer> possibleHumanPits;

    /**
     * A set containing the indices of the pits that the machine player can
     * choose from when making a move.
     */
    private Set<Integer> possibleMachinePits;

    /**
     * The score of lucrative pits for the human.
     */
    private int pScoreHuman;

    /**
     * The score of lucrative pits for the machine.
     */
    private double pScoreMachine;

    /**
     * Constructs a new Kalah board with the specified number of pits per player
     * and seeds per pit, and the specified opening player.
     *
     * @param pitsPerPlayer The number of pits on each player's side of the
     *                      board.
     * @param seedsPerPit   The number of seeds in each pit at the beginning of
     *                      the game.
     * @param level         The look-ahead level of the machine.
     * @param openingPlayer The player who will make the first move of the
     *                      game.
     * @throws IllegalArgumentException if the number of pits per player or the
     *                                  number of seeds per pit is less than 1.
     * @throws NullPointerException     if the opening player is null.
     */
    public Kalah(int pitsPerPlayer, int seedsPerPit, int level,
        Player openingPlayer) {
        if (pitsPerPlayer < 1) {
            throw new IllegalArgumentException(
                "Number of pits per player must be at least 1.");
        } else if (seedsPerPit < 1) {
            throw new IllegalArgumentException(
                "Number of seeds per pit must be at least 1.");
        } else if (openingPlayer == null) {
            throw new NullPointerException("Opening player cannot be null.");
        }
        targetPitOfLastMove = NO_MOVES_DEFAULT;
        sourcePitOfLastMove = NO_MOVES_DEFAULT;
        int numberOfPlayers = Player.values().length - 1;
        totalPits = numberOfPlayers * pitsPerPlayer + numberOfPlayers;
        totalSeeds = numberOfPlayers * pitsPerPlayer * seedsPerPit;
        seeds = new int[totalPits];
        startingSeedsInPits = seedsPerPit;
        this.openingPlayer = openingPlayer;
        this.pitsPerPlayer = pitsPerPlayer;
        machineLevel = level;
        currentPlayer = openingPlayer;
        possibleHumanPits = new HashSet<>();
        possibleMachinePits = new HashSet<>();
        for (int i = 0; i < pitsPerPlayer; i++) {
            seeds[i] = startingSeedsInPits;
            possibleHumanPits.add(i);
            possibleMachinePits.add(totalPits - 2 - i);
            seeds[totalPits - 2 - i] = startingSeedsInPits;
        }
        seeds[totalPits - 1] = INITIAL_SEEDS_IN_WINNING_PITS;
        seeds[pitsPerPlayer] = INITIAL_SEEDS_IN_WINNING_PITS;
    }

    /**
     * Asserts that the depth is valid.
     *
     * @param depth The depth to be checked.
     * @throws AssertionError If the depth is invalid.
     */
    private static void assertDepthValid(int depth) {
        assert depth >= 0 : "Invalid depth " + depth + ".";
    }

    /**
     * Asserts that the game is not null.
     *
     * @param game The game to be checked.
     * @throws AssertionError If the game is null.
     */
    private static void assertGameNotNull(Board game) {
        assert game != null : "Game is null";
    }

    /**
     * Asserts that the player is not null.
     *
     * @param player The player to be checked.
     * @throws AssertionError If the player is null.
     */
    private static void assertIsRealPlayer(Player player) {
        assert isRealPlayer(player) : "Player is null.";
    }

    /**
     * Checks if the player is a Human or a Machine.
     *
     * @param player The player to be checked.
     * @return True if the player is the human or the machine.
     */
    private static boolean isRealPlayer(Player player) {
        return player == Player.HUMAN || Player.MACHINE == player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getOpeningPlayer() {
        return openingPlayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player next() {
        if (isGameOver()) {
            throw new IllegalStateException("The Game is already over.");
        } else {
            return currentPlayer;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Board move(int pit) {
        if (pit < 0 || pit >= pitsPerPlayer) {
            throw new IllegalArgumentException(
                "Invalid human pit " + pit + ".");
        } else if (isGameOver()) {
            throw new IllegalMoveException("The game is already over.");
        } else if (currentPlayer != Player.HUMAN) {
            throw new IllegalMoveException("It is not the machines turn.");
        } else if (isPitEmpty(pit)) {
            return null;
        } else {
            return executeMove(pit);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Board machineMove() throws InterruptedException {
        if (isGameOver()) {
            throw new IllegalMoveException("The game is already over.");
        } else if (currentPlayer != Player.MACHINE) {
            throw new IllegalMoveException("It is the humans turn.");
        } else {
            return evaluateBestChildGame(this, 1).game;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException(
                "Invalid machine level " + level + ".");
        } else {
            machineLevel = level;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGameOver() {
        return allPitsEmpty(Player.HUMAN) || allPitsEmpty(Player.MACHINE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getWinner() {
        if (!isGameOver()) {
            throw new IllegalStateException("Game isn't over.");
        }
        return winner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSeeds(int pit) {
        if (isValidPit(pit)) {
            return seeds[pit];
        } else {
            throw new IllegalArgumentException(
                String.format(INVALID_PIT_MESSAGE, pit));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int sourcePitOfLastMove() {
        return sourcePitOfLastMove;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int targetPitOfLastMove() {
        return targetPitOfLastMove;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPitsPerPlayer() {
        return pitsPerPlayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSeedsPerPit() {
        return startingSeedsInPits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSeedsOfPlayer(Player player) {
        if (!isRealPlayer(player)) {
            throw new IllegalArgumentException(
                "Player must be the human or the machine.");
        }
        int seedsOfPlayer = 0;
        for (int pit : getPossibleMoves(player)) {
            seedsOfPlayer += seeds[pit];
        }
        return seedsOfPlayer + seeds[getIndexWinningPit(player)];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Kalah clone() {
        Kalah copy;
        try {
            // Create a shallow copy of the object
            copy = (Kalah) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
        // Create deep copies of the mutable fields
        copy.seeds = seeds.clone();
        copy.possibleHumanPits = new HashSet<>(possibleHumanPits);
        copy.possibleMachinePits = new HashSet<>(possibleMachinePits);

        return copy;
    }

    /**
     * Returns a string representation of the current state of the board.
     *
     * @return A string representation of the current state of the board.
     */
    @Override
    public String toString() {
        int digitsBiggestSeedNumber = String.valueOf(
            Arrays.stream(seeds).max().getAsInt()).length();
        int columnWidth = digitsBiggestSeedNumber + 1;
        String formatSpecifier = "%" + columnWidth + "d";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= pitsPerPlayer; i++) {
            String formattedSeedHuman = String.format(formatSpecifier,
                seeds[totalPits - 1 - i]);
            builder.append(formattedSeedHuman);
        }
        builder.append("\n").append(" ".repeat(digitsBiggestSeedNumber));
        for (int i = 0; i <= pitsPerPlayer; i++) {
            String formattedSeedHuman = String.format(formatSpecifier,
                seeds[i]);
            builder.append(formattedSeedHuman);
        }
        return builder.substring(1);
    }

    /**
     * Executes a move in a game of Kalah.
     *
     * @param pit The index of the pit where the move originates from. The pit
     *            must be a valid pit for the current player and must not be
     *            empty.
     * @return A new Kalah instance representing the state of the game after the
     *         move has been executed, or null if the move was invalid.
     */
    private Kalah executeMove(int pit) {
        assert isValidPit(pit) : String.format(INVALID_PIT_MESSAGE, pit);
        if (isPitEmpty(pit)) {
            return null;
        }
        Kalah newKalah = clone();
        newKalah.sourcePitOfLastMove = pit;
        pit = newKalah.sowSeeds(pit, true, currentPlayer);

        // only sow last seed if no catch has taken place because of performance
        int lastPit = newKalah.getNextIndex(pit, currentPlayer);
        newKalah.targetPitOfLastMove = lastPit;
        if (!newKalah.didCapture(newKalah.sourcePitOfLastMove, lastPit,
            currentPlayer)) {
            newKalah.fillPit(lastPit, 1);
            if (lastPit != getIndexWinningPit(currentPlayer)) {
                newKalah.currentPlayer = currentPlayer.getOpponent();
            }
        } else {
            newKalah.currentPlayer = currentPlayer.getOpponent();
        }
        if (newKalah.isGameOver()) {
            newKalah.setWinner();
        }
        return newKalah;
    }

    /**
     * Sets the winning player if the game is over. The {@code winner} is
     * {@link Player#NOBODY} if the game ends in a draw.
     */
    private void setWinner() {
        if (isGameOver()) {
            int seedsOfHuman = getSeedsOfPlayer(Player.HUMAN);
            int seedsOfMachine = getSeedsOfPlayer(Player.MACHINE);
            if (seedsOfHuman > seedsOfMachine) {
                winner = Player.HUMAN;
            } else if (seedsOfMachine > seedsOfHuman) {
                winner = Player.MACHINE;
            } else {
                winner = Player.NOBODY;
            }
        }
    }

    /**
     * Removes all seeds from the <code>sourcePit</code> and sows them in a
     * counter-clockwise direction to subsequent pits, leaving the final seed
     * for the calling method to handle.
     *
     * @param sourcePit       The pit index from which the seeds will be sowed.
     * @param distributeSeeds Flag if the method is used for distributing seeds
     *                        or searching the target pit of a move.
     * @param player          The player, who will sow the seeds.
     * @return The pit index where the last seed was sowed.
     */
    private int sowSeeds(int sourcePit, boolean distributeSeeds,
        Player player) {
        assertPitValid(sourcePit);
        assertIsRealPlayer(player);
        int sumSeedsInSelectedPit = seeds[sourcePit];
        if (distributeSeeds) {
            emptyPit(sourcePit);
            sumSeedsInSelectedPit--;
        }
        while (sumSeedsInSelectedPit > 0) {
            sourcePit = getNextIndex(sourcePit, player);
            if (distributeSeeds) {
                fillPit(sourcePit, 1);
            }
            sumSeedsInSelectedPit--;
        }
        return sourcePit;
    }

    /**
     * Returns a set of all possible moves for the given player.
     *
     * @param player The player for whom to get the possible moves.
     * @return A set of indexes representing the possible moves for the player.
     */
    private Set<Integer> getPossibleMoves(Player player) {
        assertIsRealPlayer(player);
        if (player == Player.MACHINE) {
            return possibleMachinePits;
        } else {
            return possibleHumanPits;
        }
    }

    /**
     * Adds the given number of seeds to the specified pit. Updates the p-score
     * and possible moves for the players as necessary.
     *
     * @param pit The pit to add seeds to.
     * @param sum The number of seeds to add. Must be greater than 0 and less
     *            than or equal to the total number of seeds in the game.
     */
    private void fillPit(int pit, int sum) {
        assertPitValid(pit);
        assert sum > 0 && sum <= totalSeeds : "Invalid sum of seeds " + sum
            + ".";
        if (pit != getIndexWinningPit(Player.HUMAN)
            && pit != getIndexWinningPit(Player.MACHINE)) {
            boolean pitWasEmpty = isPitEmpty(pit);
            boolean pitWasLucrative = pitIsLucrative(pit);
            int oppositePit = oppositePit(pit);
            seeds[pit] += sum;
            if (pitWasEmpty) {
                Player pitOwner = getPlayerForPit(pit);
                getPossibleMoves(pitOwner).add(pit);
                if (pitIsLucrative(oppositePit)) {
                    modifyPScore(pitOwner, -1);
                }
            } else if (!pitWasLucrative && isLucrativeCapture((oppositePit))) {
                modifyPScore(getPlayerForPit(oppositePit), 1);
            }
        } else {
            seeds[pit] += sum;
        }
    }

    /**
     * Returns the player who owns the pit at the given index.
     *
     * @param pit The index of the pit to get the owner of.
     * @return The player who owns the pit.
     */
    private Player getPlayerForPit(int pit) {
        assertPitValid(pit);
        return pit <= pitsPerPlayer ? Player.HUMAN : Player.MACHINE;
    }

    /**
     * Determines if the given pit index is valid for this game.
     *
     * @param pit The pit index to check.
     * @return true if the pit index is valid, false otherwise.
     */
    private boolean isValidPit(int pit) {
        return pit >= 0 && pit < totalPits;
    }

    /**
     * Empties the pit at the given index, updating the p-score and possible
     * moves for the players as necessary.
     *
     * @param pit The index of the pit to empty.
     */
    private void emptyPit(int pit) {
        assertPitValid(pit);
        Player pitOwner = getPlayerForPit(pit);
        int oppositePit = oppositePit(pit);
        if (isLucrativeCapture(oppositePit)) {
            Player oppositeOwner = pitOwner.getOpponent();
            modifyPScore(oppositeOwner, -1);
        }
        seeds[pit] = 0;
        getPossibleMoves(pitOwner).remove(pit);
        if (pitIsLucrative(oppositePit)) {
            modifyPScore(pitOwner, 1);
        }
    }

    /**
     * Determines if capturing the seeds in the pit at the given index is
     * considered a "lucrative capture". This is defined as an empty pit that is
     * opposite a pit that has at least twice as many seeds as the starting
     * number of seeds in each pit.
     *
     * @param pit The index of the pit to check for a lucrative capture.
     * @return true if capturing the seeds in the pit is a lucrative capture,
     *         false otherwise.
     */
    private boolean isLucrativeCapture(int pit) {
        return isPitEmpty(pit) && pitIsLucrative(oppositePit(pit));
    }

    /**
     * Determines if the pit at the given index is considered "lucrative", which
     * is defined as having at least twice as many seeds as the starting number
     * of seeds in each pit.
     *
     * @param pit The index of the pit to check.
     * @return true if the pit is lucrative, false otherwise.
     */
    private boolean pitIsLucrative(int pit) {
        return seeds[pit] >= 2 * startingSeedsInPits;
    }

    /**
     * Modifies the p-score for the given player by the given value.
     *
     * @param player The player whose p-score to modify.
     * @param value  The value to add to the player's p-score.
     */
    private void modifyPScore(Player player, int value) {
        assertIsRealPlayer(player);
        if (player == Player.HUMAN) {
            pScoreHuman += value;
        } else {
            pScoreMachine += value;
        }
    }

    /**
     * Determines if capturing the seeds in the target pit is a valid move given
     * the source pit and the current player. If the move is valid, the seeds in
     * the target pit and its opposite pit are captured and added to the current
     * player's winning pit.
     *
     * @param sourcePit The index of the source pit.
     * @param targetPit The index of the target pit.
     * @param player    Player who performs the capture.
     * @return true if the move was a valid capture, false otherwise.
     */
    private boolean didCapture(int sourcePit, int targetPit, Player player) {
        assertPitValid(sourcePit);
        assertPitValid(targetPit);
        assertIsRealPlayer(player);
        if (isValidCapture(sourcePit, targetPit, player)) {
            fillPit(getIndexWinningPit(player),
                seeds[oppositePit(targetPit)] + 1);
            emptyPit(oppositePit(targetPit));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Calculates the score for the current game state, based on the given
     * search depth. The score is calculated using a combination of the s-score
     * (the difference in seeds between the two players' winning pits), the
     * c-score (the difference in the total number of seeds that can be captured
     * by the two players), the p-score (the difference of the number of
     * lucrative pits on each player's side), and the v-score (a measure of the
     * final outcome of the game).
     *
     * @param depth The search depth at which this score is being calculated.
     * @return The score for the current game state.
     */
    private double scoreCalculation(int depth) {
        assertDepthValid(depth);
        double sScore = seeds[getIndexWinningPit(Player.MACHINE)]
            - HUMAN_SCORE_MULTIPLICATOR * seeds[getIndexWinningPit(
            Player.HUMAN)];
        double cScore = sumCaptureableEnemySeeds(Player.MACHINE)
            - HUMAN_SCORE_MULTIPLICATOR * sumCaptureableEnemySeeds(
            Player.HUMAN);
        double pScore = pScoreMachine - HUMAN_SCORE_MULTIPLICATOR * pScoreHuman;
        return 3 * sScore + cScore + pScore + calculateVScore(depth);
    }

    /**
     * Calculates the v-score for the current game state, based on the given
     * search depth and the winner of the game (if the game has already been
     * won).
     *
     * @param depth The search depth at which this v-score is being calculated.
     * @return The v-score for the current game state.
     */
    private double calculateVScore(int depth) {
        assertDepthValid(depth);
        if (winner == Player.MACHINE) {
            return 500D / depth;
        } else if (winner == Player.HUMAN) {
            return -(HUMAN_SCORE_MULTIPLICATOR * (500D / depth));
        } else {
            return 0;
        }
    }

    /**
     * Calculates the total number of seeds that can be captured by the given
     * player in the current game state.
     *
     * @param player The player for whom to calculate the total number of
     *               captureable seeds.
     * @return The total number of captureable seeds for the given player.
     */
    private int sumCaptureableEnemySeeds(Player player) {
        assertIsRealPlayer(player);
        int[] capturedSeeds = new int[seeds.length];
        int sum = 0;
        for (int sourcePit : getPossibleMoves(player)) {
            int targetPit = sowSeeds(sourcePit, false, player);
            if (isValidCapture(sourcePit, targetPit, player)) {
                int oppositeSeeds = seeds[oppositePit(targetPit)];
                if (targetPit <= sourcePit) {
                    oppositeSeeds += 1;
                }
                if (oppositeSeeds > capturedSeeds[targetPit]) {
                    sum -= capturedSeeds[targetPit];
                    sum += oppositeSeeds;
                    capturedSeeds[targetPit] = oppositeSeeds;
                }
            }
        }
        return sum;
    }

    /**
     * Determines if capturing the seeds in the target pit is a valid move given
     * the source pit and the current player.
     *
     * @param sourcePit The index of the source pit.
     * @param targetPit The index of the target pit.
     * @param player    The current player.
     * @return true if the capture is valid, false otherwise.
     */
    private boolean isValidCapture(int sourcePit, int targetPit,
        Player player) {
        assertPitValid(sourcePit);
        assertPitValid(targetPit);
        assertIsRealPlayer(player);
        return seeds[sourcePit] <= 2 * pitsPerPlayer + 1 && (
            isPitEmpty(targetPit) || targetPit == sourcePit)
            && player == getPlayerForPit(targetPit)
            && targetPit != getIndexWinningPit(player) && !isPitEmpty(
            oppositePit(targetPit));
    }

    /**
     * Returns the index of the pit next pit, taking into account that the
     * player cannot throw seeds into their opponent's winning pit.
     *
     * @param pit    The index of the pit to start from.
     * @param player The player who is making the move.
     * @return The index of the pit that the seeds would end up in.
     */
    private int getNextIndex(int pit, Player player) {
        assertPitValid(pit);
        assertIsRealPlayer(player);
        int nextPit = (pit + 1) % (totalPits);
        if (nextPit == getIndexWinningPit(player.getOpponent())) {
            return (nextPit + 1) % (totalPits);
        } else {
            return nextPit;
        }
    }

    /**
     * Returns the index of the pit opposite the given pit.
     *
     * @param pit The index of the pit to find the opposite of.
     * @return The index of the pit opposite the given pit.
     */
    private int oppositePit(int pit) {
        assertPitValid(pit);
        assert pit != getIndexWinningPit(Player.HUMAN)
            && pit != getIndexWinningPit(
            Player.MACHINE) : "Pit should be normal pit.";
        return pitsPerPlayer * 2 - pit;
    }

    /**
     * Determines if all pits belonging to the given player are empty.
     *
     * @param player The player whose pits to check.
     * @return true if all pits belonging to the given player are empty, false
     *         otherwise.
     */
    private boolean allPitsEmpty(Player player) {
        assertIsRealPlayer(player);
        if (player == Player.MACHINE) {
            return possibleMachinePits.isEmpty();
        } else {
            return possibleHumanPits.isEmpty();
        }
    }

    /**
     * Returns the index of the winning pit for the given player.
     *
     * @param player The player whose winning pit to return.
     * @return The index of the winning pit for the given player.
     */
    private int getIndexWinningPit(Player player) {
        assertIsRealPlayer(player);
        if (player == Player.HUMAN) {
            return pitsPerPlayer;
        } else {
            return totalPits - 1;
        }
    }

    /**
     * Determines if the specified pit is empty.
     *
     * @param pit The index of the pit to check.
     * @return true if the pit is empty, false otherwise.
     */
    private boolean isPitEmpty(int pit) {
        assertPitValid(pit);
        return seeds[pit] == 0;
    }

    /**
     * Asserts that the pit is valid.
     *
     * @param pit The pit to be checked.
     * @throws AssertionError If the depth is invalid.
     */
    private void assertPitValid(int pit) {
        assert isValidPit(pit) : String.format(INVALID_PIT_MESSAGE, pit);
    }

    /**
     * Evaluates the best child game for the given game with a lookahead of
     * {@link #machineLevel} + 1 - <code>depth</code>. If the current player is
     * the human, the worst child for the machine is selected. If the current
     * player is the machine, the best child score for the machine is selected.
     *
     * @param currentGame The game for which the best child should be
     *                    calculated.
     * @param depth       The current depth of the child games for this node.
     * @return A TreeNodeValue of the best child for the current game.
     */
    private TreeNodeValue evaluateBestChildGame(Kalah currentGame, int depth)
        throws InterruptedException {
        assertGameNotNull(currentGame);
        assertDepthValid(depth);
        if (Thread.interrupted()) {
            throw new InterruptedException("Termination requested.");
        }
        Kalah bestChildGame = null;
        double bestChildGameScore = 0;
        boolean playerIsHuman = currentGame.currentPlayer == Player.HUMAN;
        for (Integer possibleMove : currentGame.getPossibleMoves(
            currentGame.currentPlayer)) {
            Kalah childGame = currentGame.executeMove(possibleMove);
            double valueChild = calculateChildNodeValue(childGame, depth);
            boolean currentIsBetter =
                playerIsHuman ? bestChildGameScore > valueChild
                    : bestChildGameScore < valueChild;
            if (currentIsBetter || bestChildGame == null) {
                bestChildGameScore = valueChild;
                bestChildGame = childGame;
            }
        }
        return new TreeNodeValue(bestChildGame, bestChildGameScore);
    }

    /**
     * Checks if the child game is an inner or a leaf node of the game tree and
     * then evaluates it accordingly.
     *
     * @param childGame The child game, which will be checked.
     * @param depth     The current depth of the child game in the game tree.
     * @return The best score for the child game.
     */
    private double calculateChildNodeValue(Kalah childGame, int depth)
        throws InterruptedException {
        assertGameNotNull(childGame);
        assertDepthValid(depth);
        double score = childGame.scoreCalculation(depth);
        return childGame.isGameOver() || depth >= childGame.machineLevel ? score
            : score + evaluateBestChildGame(childGame, depth + 1).score;
    }

    /**
     * Represents the contents of a node in the game tree that is used to
     * compute the result of {@link Kalah#machineMove()}.
     *
     * @param game  The game state of the node, must not be {@code null}.
     * @param score The calculated score of that game state, considering all
     *              children of the node.
     */
    private record TreeNodeValue(Board game, double score) {

        /**
         * Constructs a new {@code TreeNodeValue}.
         *
         * @param game  The game state of the node, must not be {@code null}.
         * @param score The calculated score of that game state, considering all
         *              children of the node.
         */
        private TreeNodeValue {
            assertGameNotNull(game);
        }
    }
}