package observer;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The abstract class {@code Observable} represents an object that can be
 * observed by other objects. It maintains a collection of {@link Observer}
 * objects and notifies them about changes.
 */
public abstract class Observable {

    /**
     * The list of observers to be notified by changes.
     */
    private final Collection<Observer> observers = new ArrayList<>();

    /**
     * Explicit default constructor.
     */
    protected Observable() {
    }

    /**
     * Adds an observer to the set of observers for this object, provided that
     * it is not the same as some observer already in the set.
     *
     * @param newObserver The observer to be added.
     * @throws NullPointerException If the supplied observer is null.
     */
    public void addObserver(Observer newObserver) {
        if (newObserver == null) {
            throw new NullPointerException("NewObserver is null.");
        } else {
            if (!observers.contains(newObserver)) {
                observers.add(newObserver);
            }
        }
    }

    /**
     * Notifies all observers by calling their {@code update} method.
     *
     * @see Observer#update()
     */
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

}

