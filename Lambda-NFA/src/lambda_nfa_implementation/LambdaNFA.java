package lambda_nfa_implementation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Implementation of a lambda nondeterministic finite automaton.
 */
public class LambdaNFA implements Automaton {

  /**
   * Symbol which is used to recognize the empty word.
   */
  static final char LAMBDA = '~';
  /**
   * Initial State for this LambdaNFA.
   */
  private final State initialState;
  /**
   * Set of accepting States for this LambdaNFA.
   */
  private final List<State> acceptingStates;
  /**
   * Set of all States which are in this lambdaNFA.
   */
  private final State[] states;

  private boolean lambdaTransitionAdded = false;

  /**
   * Constructor which initializes a new LambdaNFA with a number of states, an
   * initial state and a set of accepting states.
   *
   * @param numberStates    Number of States for this LambdaNFA.
   * @param initialState    Initial State for this LambdaNFA.
   * @param acceptingStates Set of accepting States for this LambdaNFA.
   */
  public LambdaNFA(int numberStates, int initialState, int[] acceptingStates) {
    this.states = new State[numberStates];
    for (int i = 0; i < numberStates; i++) {
      states[i] = new State(i + 1);
    }
    this.initialState = states[getStateIndex(initialState)];
    this.acceptingStates = new LinkedList<>();
    for (int acceptingState : acceptingStates) {
      this.acceptingStates.add(states[getStateIndex(acceptingState)]);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValidTransition(int source, int target, char symbol) {
    return (source > 0 && source <= states.length) && (target > 0
        && target <= states.length) && (
        (symbol >= Automaton.FIRST_SYMBOL && symbol <= Automaton.LAST_SYMBOL)
            || symbol == LambdaNFA.LAMBDA);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addTransition(int source, int target, char symbol) {
    Transition newTransition = new Transition(states[getStateIndex(source)],
        symbol, states[getStateIndex(target)]);
    states[getStateIndex(source)].addTransition(newTransition);
    if (symbol == LAMBDA) {
      lambdaTransitionAdded = true;
    }
  }

  /**
   * {@inheritDoc} Uses the longestPrefix method to eliminate redundant code.
   */
  @Override
  public boolean isElement(String word) {
    return word.equals(longestPrefix(word));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String longestPrefix(String word) {
    preComputeAllSetsOfLambdaTargetStates();
    char symbol = LAMBDA;
    String currentPrefix = null;
    State separationState = new State('$');
    Queue<State> targetStatesQueue = initTargetStatesQueue(separationState);
    int cursor = -1;
    while (!targetStatesQueue.isEmpty()) {
      State currentState = targetStatesQueue.poll();
      if (currentState == separationState) {
        cursor++;
        if (cursor < word.length()) {
          targetStatesQueue.offer(separationState);
          symbol = word.charAt(cursor);
        }
      } else {
        if (acceptingStates.contains(currentState)) {
          currentPrefix = word.substring(0, cursor);
        }
        if (cursor < word.length()) {
          addTargetsToQueue(targetStatesQueue, currentState, symbol);
        }
      }
    }
    return currentPrefix;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (State state : states) {
      String printedState = state.toString();
      if (!printedState.isEmpty()) {
        builder.append(printedState).append("\n");
      }
    }
    return builder.toString().trim();
  }

  /**
   * Returns correct index of a state, because number of first state is always
   * 1.
   *
   * @param state State where the index should be returned for.
   * @return Returns index of the state as an int.
   */
  private int getStateIndex(int state) {
    return state - 1;
  }

  /**
   * Initializes the targetStateQueue for the
   * {@link LambdaNFA#longestPrefix(String)} method with a separationState for
   * separating the states which can be read by each symbol, initialState and
   * all states which can be reached by reading the empty word.
   *
   * @param separationState separatingState
   * @return Returns initialized Queue.
   */
  private Queue<State> initTargetStatesQueue(State separationState) {
    Queue<State> queue = new LinkedList<>();
    queue.offer(separationState);
    queue.offer(initialState);
    queue.addAll(initialState.getSetOfLambdaTargetStates());
    return queue;
  }

  /**
   * Helper-method for {@link LambdaNFA#longestPrefix(String)}. Adds the set of
   * targets states, because of runtime efficiency, of the
   * <code>currentState</code>, which can be reached by reading
   * <code>symbol</code> to the targetStatesQueue.
   *
   * @param queue        Queue where targetStates should be added.
   * @param currentState State for which the target states are to be computed.
   * @param symbol       Symbol which is read next at currentState.
   */
  private void addTargetsToQueue(Queue<State> queue, State currentState,
      char symbol) {
    Set<State> targetSet = new HashSet<>();
    for (State target : currentState.getTargets(symbol)) {
      targetSet.add(target);
      targetSet.addAll(target.getSetOfLambdaTargetStates());
    }
    queue.addAll(targetSet);
  }

  /**
   * Computes the Set of all target States which can be reached with a
   * spontaneous transitions for each state of this LambdaNFA, if the NFA added
   * a spontaneous transition to the automaton between the last call of this
   * method or the beginning of the program and the current call.
   */
  private void preComputeAllSetsOfLambdaTargetStates() {
    if (lambdaTransitionAdded) {
      Arrays.asList(states).forEach(State::precomputeSetOfLambdaTargetStates);
      lambdaTransitionAdded = false;
    }
  }
}