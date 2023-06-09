package cr.ac.tec.ce3104.modes;

import cr.ac.tec.ce3104.gameobjects.Player;
import cr.ac.tec.ce3104.physics.HorizontalDirection;

// A controllable mode is one that responds to input events
public interface ControllableMode extends Mode{
    /**
     * Gets which direction the sprite moves
     * @return sprite address of current mode
     */
    HorizontalDirection getDirection();

    /**
     * Indicates how a mode reacts to a key release event
     * @param player reference to player avatar
     */
    default void onRelease(Player player) {}

    /**
     * Indicates how the mode should react to a Jump event
     * @param player reference to player avatar
     */
    default void onShoot(Player player) {}

    /**
     * Indicates how the mode should react to a movement to the left event
     * @param player reference to player avatar
     */
    default void onMoveLeft(Player player) {}

    /**
     * Indicates how the mode should react to a movement to the right event
     * @param player reference to player avatar
     */
    default void onMoveRight(Player player) {}
}
