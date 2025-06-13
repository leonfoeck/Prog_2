package lambda_nfa_implementation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Class implements a State of a lambda nondeterministic finite automaton.
 */
public class State {

  /**
   * Length of the alphabet which the automaton accepts plus the empty word.
   */
  private static final int ALPHABET_LENGTH =
      Automaton.LAST_SYMBOL - Automaton.FIRST_SYMBOL + 1;
  /**
   * Name of the state.
   */
  private final int stateNumber;
  /**
   * Adjacency list for the transitions that start at this state. Sorted
   * alphabetically by input symbols and empty word is at index zero.
   */
  private List<Collection<Transition>> charAdj;
  /**
   * Set of states which can be reached by reading the empty word.
   */
  private Set<State> setOfLambdaTargetStates;

  /**
   * Constructor of the State class, which sets the name of the state and
   * initializes the adjacency list with <code>null</code>.
   *
   * @param stateNumber Name which this state should get.
   */
  public State(int stateNumber) {
    this.stateNumber = stateNumber;
  }

  /**
   * Getter for the name of a state.
   *
   * @return returns the Name of a state as an Integer.
   */
  int getStateNumber() {
    return stateNumber;
  }

  /**
   * Returns states that can be reached with several spontaneous transitions
   * from this node.
   *
   * @return unmodifiable set of lambda transitions or an empty set, if the set
   * of lambda target states is null.
   */
  Set<State> getSetOfLambdaTargetStates() {
    if (setOfLambdaTargetStates != null) {
      return Collections.unmodifiableSet(setOfLambdaTargetStates);
    } else {
      return Collections.emptySet();
    }
  }

  /**
   * Adds a new transition for this state to the adjacency list. For efficiency
   * and memory reasons, the adjacency list is not initialized until the state
   * receives its first transition. This makes it easier to see, whether a state
   * has transitions or not.
   *
   * @param transition Transition which should be added.
   */
  void addTransition(Transition transition) {
    if (charAdj == null) {
      charAdj = new ArrayList<>(ALPHABET_LENGTH + 1);
      for (int i = 0; i < ALPHABET_LENGTH + 1; i++) {
        charAdj.add(null);
      }
    }
    Collection<Transition> transitions = charAdj.get(
        getIndexOfAlphabetSymbol(transition.getSymbol()));
    if (transitions == null) {
      transitions = new LinkedList<>();
      charAdj.set(getIndexOfAlphabetSymbol(transition.getSymbol()),
          transitions);
    }
    transitions.add(transition);
  }

  /**
   * Sorts all transitions for this state and returns it as one sorted list.
   *
   * @return Sorted List of transitions.
   */
  private List<Transition> getOrderedTransitions() {
    List<Transition> adj = new LinkedList<>();
    for (Collection<Transition> transitions : charAdj) {
      if (transitions != null) {
        adj.addAll(transitions);
      }
    }
    Collections.sort(adj);
    return adj;
  }

  /**
   * Precomputes the set of lambda target states for this state.
   */
  void precomputeSetOfLambdaTargetStates() {
    setOfLambdaTargetStates = new HashSet<>();
    Map<State, Boolean> visited = new HashMap<>();
    Queue<State> bfsQueue = new LinkedList<>();
    bfsQueue.offer(this);
    visited.put(this, true);
    while (!bfsQueue.isEmpty()) {
      State state = bfsQueue.poll();
      if (state != this) {
        setOfLambdaTargetStates.add(state);
      }
      Collection<State> lambdaTargets = state.getTargets(LambdaNFA.LAMBDA);
      for (State lambdaTarget : lambdaTargets) {
        if (!visited.getOrDefault(lambdaTarget, false)) {
          bfsQueue.offer(lambdaTarget);
          visited.put(lambdaTarget, true);
        }
      }
    }
  }

  /**
   * Computes set of target states, which can be reached from this state by
   * reading the symbol.
   *
   * @param symbol char which triggers the transition.
   * @return Returns HashSet of states which can be reached. Returns an empty
   * Collection if this state has no transitions.
   */
  Set<State> getTargets(char symbol) {
    if (charAdj != null) {
      Collection<Transition> transitions = charAdj.get(
          getIndexOfAlphabetSymbol(symbol));
      if (transitions != null) {
        HashSet<State> stateTargets = new HashSet<>();
        for (Transition transition : transitions) {
          stateTargets.add(transition.getTarget());
        }
        return Collections.unmodifiableSet(stateTargets);
      }
    }
    return Collections.emptySet();
  }

  /**
   * Prints the transitions for this state as a lexicographically ordered list.
   * Empty String is returned if state has no transitions. Used to avoid the
   * unnecessary execution of {@link State#getOrderedTransitions()}.
   *
   * @return Transitions for this state as a String.
   */
  @Override
  public String toString() {
    if (charAdj == null) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    for (Transition orderedTransition : getOrderedTransitions()) {
      builder.append(orderedTransition).append("\n");
    }
    return builder.substring(0, builder.length() - 1);
  }

  /**
   * Computes index of the adjacency list given a char in the alphabet.
   *
   * @param ch Letter for which the index is to be calculated.
   * @return 0 if parameter is the empty word or an int from 1 to 26
   * alphabetically ordered so for a 1 and so on.
   */
  private int getIndexOfAlphabetSymbol(char ch) {
    if (ch == LambdaNFA.LAMBDA) {
      return 0;
    } else {
      return (ch - Automaton.FIRST_SYMBOL) + 1;
    }
  }
}