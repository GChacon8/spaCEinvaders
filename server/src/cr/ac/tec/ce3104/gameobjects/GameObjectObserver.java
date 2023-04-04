package cr.ac.tec.ce3104.gameobjects;

public interface GameObjectObserver {
    /**
     * Indicates what actions to take when the given entity is removed from the game scenario
     * @param object deleted entity
     */
    void onObjectDeleted(GameObject object);

    /**
     * Indicates what actions to take given a change in the mode of the given entity
     * @param object entity whose mode has changed
     */
    void onObjectModeChanged(GameObject object);

    /**
     * Add a message to print in the administrator console
     * @param message message to print in the administrator console
     */
    void log(String message);
}
