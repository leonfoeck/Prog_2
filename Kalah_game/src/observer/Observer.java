package observer;

/**
 * A class can implement the Observer interface when it wants to be informed of
 * changes in observable objects.
 */
public interface Observer {

    /**
     * This method is called whenever the observed object is changed. An
     * application calls an Observable object's {@code notifyObservers} method
     * to have all the object's observers notified of the change.
     */
    void update();
}
