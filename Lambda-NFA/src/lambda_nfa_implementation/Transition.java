package lambda_nfa_implementation;

/**
 * Class is used to implement a state transition of a lambda nondeterministic
 * finite automaton.
 */
public class Transition implements Comparable<Transition> {

  /**
   * Target state of this transition, which is reached when reading
   * {@link Transition#symbol} at {@link Transition#start}.
   */
  private final State target;
  /**
   * Starting state of this transition.
   */
  private final State start;
  /**
   * Symbol of this transition. If symbol is read at {@link Transition#start}
   * {@link Transition#target} is reached.
   */
  private final char symbol;

  /**
   * Initializes a new Transition with a starting state {@link Transition#start}
   * a symbol {@link Transition#symbol} and a target Transition
   * {@link Transition#target}.
   *
   * @param start  Starting state of this transition.
   * @param symbol Symbol which should trigger this transition.
   * @param target Target state of this transition.
   */
  public Transition(final State start, final char symbol, final State target) {
    this.target = target;
    this.symbol = symbol;
    this.start = start;
  }

  /**
   * Getter for the transition {@link Transition#symbol}.
   *
   * @return {@link Transition#symbol}.
   */
  char getSymbol() {
    return symbol;
  }

  /**
   * Getter for the target state {@link Transition#target}.
   *
   * @return {@link Transition#target}.
   */
  State getTarget() {
    return target;
  }

  /**
   * Implements a comparison between two transitions. First compares by starting
   * state then by target State then by symbol. Empty word: "~" is the smallest
   * symbol.
   *
   * @param other the transition to be compared.
   * @return {@code < 0} if {@code this < other}, {@code = 0} if equal, and
   * {@code > 0} if {@code this > other}.
   */
  @Override
  public int compareTo(Transition other) {
    if (start.getStateNumber() == other.start.getStateNumber()) {
      if (target.getStateNumber() == other.target.getStateNumber()) {
        if (symbol == LambdaNFA.LAMBDA && other.symbol != LambdaNFA.LAMBDA) {
          return -1;
        } else if (symbol != LambdaNFA.LAMBDA
            && other.symbol == LambdaNFA.LAMBDA) {
          return 1;
        } else {
          return symbol - other.symbol;
        }
      } else {
        return target.getStateNumber() - other.target.getStateNumber();
      }
    } else {
      return start.getStateNumber() - other.start.getStateNumber();
    }
  }

  /**
   * Converts Transition into a String in this format: (startingState,
   * targetState) symbol.
   *
   * @return transition as a String.
   */
  @Override
  public String toString() {
    return "(" + start.getStateNumber() + ", " + target.getStateNumber() + ") "
        + symbol;
  }
}