package cr.ac.tec.ce3104.modes;

import cr.ac.tec.ce3104.gameobjects.GameObject;
import cr.ac.tec.ce3104.physics.Orientation;
import cr.ac.tec.ce3104.physics.Speed;
import cr.ac.tec.ce3104.resources.Sequence;

// A mode is an object that defines the movement mechanics of an entity.
public interface Mode {
    /**
     * Gets the velocity of the entity
     */
    Speed getSpeed();

    /**
     * Gets the current sprite sequence
     */
    Sequence getSequence();

    /**
     * Function that handles relocation events
     * @param object game object to be handled
     */
    default void onRelocate(GameObject object) {}

    /**
     * Indicates how an object is handled if it encounters a collision with a dangerous object
     * @param object object to manage
     * @param orientation mode characteristic direction
     */
    //default void onHit(GameObject object, Orientation orientation) {}
}
