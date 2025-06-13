package lambda_nfa_ui;

import java.io.InputStreamReader;
import java.util.Scanner;
import lambda_nfa_implementation.Automaton;
import lambda_nfa_implementation.LambdaNFA;

/**
 * Implements the user interface to a Lambda-nondeterministic finite automaton.
 * <p>
 * The following commands are available to the user:
 * <ul>
 *     <li>INIT n - Initializes a LambdaNFA with n states, initial state = 1
 *     and accepting state = n.</li>
 *     <li>ADD i j c - Adds transition from state with number i to the state
 *     with the number j which character c reads to the automaton.</li>
 *     <li>CHECK "s" - Checks whether word s is in the automaton's language
 *     or not.</li>
 *     <li>Prefix "s" - Computes the longest prefix of s that is in the
 *     automaton's language.</li>
 *     <li>DISPLAY - Prints the automaton as a lexicographically ordered
 *     list of transitions.</li>
 *     <li>GENERATE - Loads any automaton hardcoded in the program.</li>
 *     <li>HELP - Outputs a meaningful help text.</li>
 *     <li>QUIT - Quits the program.</li>
 * </ul>
 */
public final class Shell {

    /**
     * Command length for the INIT, CHECK and PREFIX commands.
     */
    private static final int INPUT_LENGTH_INIT_CHECK_PREFIX = 2;

    /**
     * Command length for the ADD command.
     */
    private static final int INPUT_LENGTH_FOR_ADD = 4;

    /**
     * Command length for the DISPLAY, HELP, QUIT and GENERATE commands
     */
    private static final int INPUT_LENGTH_DISPLAY_HELP_QUIT_GENERATE = 1;

    /**
     * Prompt for the users command.
     */
    private static final String PROMPT = "nfa> ";

    /**
     * Separates the word to search for. Is used to also check for the empty
     * word.
     */
    private static final char WORD_DELIMITATION = '"';

    /**
     * Used to find words in the given alphabet surrounded by
     * {@link Shell#WORD_DELIMITATION}.
     */
    private static final String WORDS_IN_ALPHABET_DELIMITATION_PATTERN =
        WORD_DELIMITATION + "[" + Automaton.FIRST_SYMBOL + "-"
            + Automaton.LAST_SYMBOL + "]*" + WORD_DELIMITATION;

    /**
     * Test automaton for the GENERATE command.
     */
    private static final int[][] TEST_AUTOMATON = {{1, 1, 'a'}, {1, 1, 'b'},
        {1, 2, '~'}, {2, 3, 'b'}, {2, 4, 'a'}, {3, 4, '~'}, {3, 5, '~'},
        {4, 5, 'b'}};

    /**
     * Empty and private constructor so no-one can make an instance of this
     * class.
     */
    private Shell() {
    }

    /**
     * Main method, Initializes Scanner to read Input from user and passes it to
     * the execute method.
     *
     * @param args Input for main method.
     */
    public static void main(String[] args) {
        Scanner stdin = new Scanner(new InputStreamReader(System.in));
        execute(stdin);
    }

    /**
     * Executes the Read-eval-print loop.
     *
     * @param stdin Scanner to read user input.
     */
    private static void execute(Scanner stdin) {
        boolean quit = false;
        Automaton lambdaNFA = null;
        while (!quit) {
            System.out.print(PROMPT);
            String input = stdin.nextLine();
            if (input == null) {
                break;
            }
            String[] tokens = input.trim().split("\\s+");
            if (inputHasCorrectLength(tokens)) {
                switch (tokens[0].toUpperCase().charAt(0)) {
                    case 'I' -> {
                        lambdaNFA = handleInitCommand(tokens, lambdaNFA);
                    }
                    case 'A' -> handleAddTransitionCommand(tokens, lambdaNFA);
                    case 'C' -> handleCheckCommand(tokens, lambdaNFA);
                    case 'P' -> handlePrefixCommand(tokens, lambdaNFA);
                    case 'D' -> handlePrintCommand(lambdaNFA);
                    case 'G' -> {
                        lambdaNFA = handleGenerateCommand();
                    }
                    case 'H' -> handleHelpCommand();
                    case 'Q' -> {
                        quit = true;
                    }
                    default -> error("Unknown command!");
                }
            }
        }
    }

    /**
     * Checks whether all inputs to initialize an automaton are correct. If all
     * are correct, a new automaton is created.
     *
     * @param input     User input.
     * @param lambdaNFA Currently used lambdaNFA where the transition should be
     *                  added.
     * @return Returns the NFA if it could be initialized. Old lambdaNFA if
     *     inputs are wrong.
     */
    private static Automaton handleInitCommand(String[] input,
        Automaton lambdaNFA) {
        Integer stateNumber = tryReadingPositiveInteger(input[1]);
        if (stateNumber != null) {
            return new LambdaNFA(stateNumber, 1, new int[]{stateNumber});
        } else {
            return lambdaNFA;
        }
    }

    /**
     * Checks if all inputs to add a transition to the automaton are correct. If
     * all inputs are correct the transition is added to the automaton.
     *
     * @param input User input.
     */
    private static void handleAddTransitionCommand(String[] input,
        Automaton lambdaNFA) {
        if (automataNotNull(lambdaNFA)) {
            Integer source = tryReadingPositiveInteger(input[1]);
            Integer target = tryReadingPositiveInteger(input[2]);
            Character symbol = tryReadingCharacter(input[3]);
            if (source != null && target != null && symbol != null) {
                if (lambdaNFA.isValidTransition(source, target, symbol)) {
                    lambdaNFA.addTransition(source, target, symbol);
                } else {
                    error("Incorrect input for creating a transition.");
                }
            }
        }
    }

    /**
     * Checks if all inputs for the CHECK command are correct, if they are it is
     * checked whether the given word is in the language of the automaton.
     *
     * @param input     User input.
     * @param lambdaNFA Currently used NFA.
     */
    private static void handleCheckCommand(String[] input,
        Automaton lambdaNFA) {
        if (automataNotNull(lambdaNFA)) {
            String word = tryReadingWordInAlphabet(input[1]);
            if (word != null) {
                if (lambdaNFA.isElement(word)) {
                    System.out.println("In language.");
                } else {
                    System.out.println("Not in language.");
                }
            }
        }
    }

    /**
     * First checks if all inputs for the PREFIX command are correct. Then tries
     * to execute the PREFIX command, which checks if a word has a prefix, which
     * is accepted by the language the NFA represents.
     *
     * @param input     User input.
     * @param lambdaNFA Currently used NFA.
     */
    private static void handlePrefixCommand(String[] input,
        Automaton lambdaNFA) {
        if (automataNotNull(lambdaNFA)) {
            String word = tryReadingWordInAlphabet(input[1]);
            if (word != null) {
                String longestPrefix = lambdaNFA.longestPrefix(word);
                if (longestPrefix != null) {
                    System.out.println(
                        WORD_DELIMITATION + longestPrefix + WORD_DELIMITATION);
                } else {
                    System.out.println("No prefix in language.");
                }
            }
        }
    }

    /**
     * Tries to execute the PRINT command, which Prints the automaton as a
     * lexicographically ordered list of transitions.
     *
     * @param lambdaNFA Currently used NFA.
     */
    private static void handlePrintCommand(Automaton lambdaNFA) {
        if (automataNotNull(lambdaNFA)) {
            System.out.println(lambdaNFA);
        }
    }

    /**
     * Executes the GENERATE command, which initializes a hardcoded automaton.
     * Mostly used for debugging and testing.
     *
     * @return Returns hardcoded automaton.
     */
    private static Automaton handleGenerateCommand() {
        Automaton lambdaNFA = new LambdaNFA(5, 1, new int[]{5});
        for (int[] ints : TEST_AUTOMATON) {
            lambdaNFA.addTransition(ints[0], ints[1], (char) ints[2]);
        }
        return lambdaNFA;
    }

    /**
     * Prints a help message for the user.
     */
    private static void handleHelpCommand() {
        System.out.println("""
            The following commands are available to use:
                    
            INIT n - Initializes a LambdaNFA with n states, initial state = 1 and
            accepting state = n.
            ADD i j c - Adds transition from state with number i to the state with
            the number j which character c reads to the automaton.
            CHECK "s" - Checks whether word s is in the automaton's language or not.
            Prefix "s" - Computes the longest prefix of s that is in the automaton's
            language.
            DISPLAY - Prints the automaton as a lexicographically ordered list of
            transitions.
            GENERATE - Loads any automaton hardcoded in the program.
            HELP - Outputs a meaningful help text.
            QUIT - Quits the program.""");
    }

    /**
     * Attempts to read a string from the user and verifies if the string
     * matches the {@link Shell#WORDS_IN_ALPHABET_DELIMITATION_PATTERN}. If it
     * does the word in the alphabet is returned without
     * {@link Shell#WORD_DELIMITATION} at the end and the beginning. If it
     * doesn't match the pattern an appropriate error message is printed.
     *
     * @param word Word to be checked.
     * @return Returns the word if all characters in the String are in the
     *     defined alphabet. Returns
     *     <code>null</code> if at least one character is not in the alphabet
     *     or word isn't surrounded by {@link Shell#WORD_DELIMITATION}.
     */
    private static String tryReadingWordInAlphabet(String word) {
        if (word.matches(WORDS_IN_ALPHABET_DELIMITATION_PATTERN)) {
            return word.substring(1, word.length() - 1);
        } else {
            error("Input should match with the following pattern: "
                + WORDS_IN_ALPHABET_DELIMITATION_PATTERN);
            return null;
        }
    }

    /**
     * Tries to read a positive Integers from the user input. If the input isn't
     * correct an appropriate error message is printed.
     *
     * @param input User input.
     * @return Returns the int the user inputted. <code>null</code> if input
     *     isn't a positive int.
     */
    private static Integer tryReadingPositiveInteger(String input) {
        Integer posInt;
        try {
            posInt = Integer.parseInt(input);
            if (posInt <= 0) {
                error(posInt + " should be positive.");
                return null;
            }
        } catch (NumberFormatException e) {
            error(input + " should be a positive Integer.");
            posInt = null;
        }
        return posInt;

    }

    /**
     * Tries to read a char from the user. Prints an error message, if no char
     * was read.
     *
     * @param input User input
     * @return Char if input was a char null if it wasn't.
     */
    private static Character tryReadingCharacter(String input) {
        if (input.length() == 1) {
            return input.charAt(0);
        } else {
            error(input + " should have been a char.");
            return null;
        }
    }

    /**
     * Prints error message in the given Format.
     *
     * @param errorMessage Error message which should be printed.
     */
    private static void error(final String errorMessage) {
        System.out.println("Error! " + errorMessage);
    }

    /**
     * Checks if the given automaton isn't <code>null</code>. Writes an error if
     * it is.
     *
     * @param lambdaNFA Automaton which should be checked.
     * @return <code>true</code> if automation is <code>null</code>.
     *     <code>false</code> if it isn't.
     */
    private static boolean automataNotNull(Automaton lambdaNFA) {
        if (lambdaNFA == null) {
            error("Please initialize your automata before you want "
                + "to execute commands on it.");
            return false;
        }
        return true;
    }

    /**
     * Checks first, if input exists and then, if a command has the correct
     * parameter length.
     *
     * @param tokens User Input split by Whitespace.
     * @return Returns <code>true</code> if parameter length of command is
     *     correct and if no inputs exists or parameter length for command is
     *     wrong returns
     *     <code>false</code>.
     */
    private static boolean inputHasCorrectLength(final String[] tokens) {
        if (tokens.length > 0 && tokens[0].length() > 0) {
            int expectedLength = switch (tokens[0].toUpperCase().charAt(0)) {
                case 'I', 'C', 'P' -> INPUT_LENGTH_INIT_CHECK_PREFIX;
                case 'A' -> INPUT_LENGTH_FOR_ADD;
                case 'D', 'H', 'G', 'Q' ->
                    INPUT_LENGTH_DISPLAY_HELP_QUIT_GENERATE;
                default -> tokens.length; // unknown command is handled later
            };
            if (tokens.length != expectedLength) {
                error("Wrong amount of parameters for command " + tokens[0]
                    + ".");
                return false;
            }
            return true;
        } else {
            error("No Input!");
            return false;
        }
    }
}