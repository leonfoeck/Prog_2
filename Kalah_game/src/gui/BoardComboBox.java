package gui;

import java.util.stream.IntStream;
import javax.swing.JComboBox;

/**
 * The {@code BoardComboBox} class is a subclass of {@link JComboBox} that
 * contains a list of integers ranging from 1 to a specified maximum value. The
 * selected value can be retrieved using the {@link #getInt()} method.
 */
final class BoardComboBox extends JComboBox<Integer> {

    /**
     * Constructs a new {@code BoardComboBox} with a range of integers from 1 to
     * the specified maximum value, and sets the selected value to the specified
     * default value.
     *
     * @param max          The maximum value to be included in the list of
     *                     integers.
     * @param defaultValue The default value to be selected.
     */
    BoardComboBox(int max, int defaultValue) {
        super(IntStream.rangeClosed(1, max).boxed().toArray(Integer[]::new));
        setSelectedIndex(defaultValue - 1);
    }

    /**
     * Gets the selected integer value from the {@code BoardComboBox}.
     *
     * @return The selected integer value.
     * @throws IllegalStateException if an invalid level is selected.
     */
    public int getInt() {
        Object selection = getSelectedItem();
        if (selection instanceof Integer integer) {
            return integer;
        } else {
            throw new IllegalStateException("Invalid level selected.");
        }
    }
}


