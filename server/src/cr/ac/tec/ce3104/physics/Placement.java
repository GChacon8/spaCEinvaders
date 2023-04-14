package cr.ac.tec.ce3104.physics;

import java.util.Collection;

import cr.ac.tec.ce3104.levels.Level;
import cr.ac.tec.ce3104.gameobjects.GameObject;

// Collect the implications of hypothetically moving an entity to a position, such as collisions
public class Placement {
    private GameObject placed;
    private Bounds bounds;
    private Orientation hitOrientation = null;
    private Integer bestDistanceSquare = null;
    private GameObject interactionTarget = null;
    private Boolean correct;

    /**
     * Create an object that provides information about the position of an entity on the playing field
     * @param placed reference to the game entity
     * @param placedAt position in which the entity is located
     * @param level level associated with entities
     * @param scene collection of objects that make up the current game scene
     */
    public Placement(GameObject placed, Position placedAt, Level level, Collection<GameObject> scene) {
        this(placed, placedAt, level, scene, true);
    }

    /**
     * Create an object that provides information about the position of an entity on the playing field
     * @param placed reference to the game entity
     * @param placedAt position in which the entity is located
     * @param level level associated with entities
     * @param scene collection of objects that make up the current game scene
     * @param correct indicates if the position of the entity is correct
     */
    public Placement(GameObject placed, Position placedAt, Level level, Collection<GameObject> scene, Boolean correct) {
        this(placed, new Bounds(placedAt, placed.getSize()), level, scene, correct);
    }

    /**
     * Create an object that provides information about the position of an entity on the playing field
     * @param placed reference to the game entity
     * @param bounds collision box associated to the entity
     * @param level level associated with entities
     * @param scene collection of objects that make up the current game scene
     * @param correct indicates if the position of the entity is correct
     */
    private Placement(GameObject placed, Bounds bounds, Level level, Collection<GameObject> scene, Boolean correct) {
        this.placed = placed;
        this.bounds = bounds;
        this.correct = correct;

        this.testWalls(level.getGameAreaSize());
        if (placed.getDynamics() != Dynamics.FLOATING) {
            this.testCollisions(scene);
        }
    }

    /**
     * Indicates from which direction an interaction that triggered a hit (collision) came from
     * @return orientation origin of the hit
     */
    public Orientation getHitOrientation() {
        return this.hitOrientation;
    }

    /**
     * Gets which entity is being interacted with
     * @return entity with which the current instance interacts
     */
    public GameObject getInteractionTarget() {
        return this.interactionTarget;
    }

    /**
     * Test if there are collisions against limits of the game screen
     * @param gameAreaSize dimensions of the playing area, given as a virtual resolution
     */
    private void testWalls(Size gameAreaSize) {
        Integer areaWidth = gameAreaSize.getWidth();
        Integer areaHeight = gameAreaSize.getHeight();

        Bounds leftWall = new Bounds(new Position(-1, 0), new Size(1, areaHeight));
        Bounds rightWall = new Bounds(new Position(areaWidth, 0), new Size(1, areaHeight));

        // Collisions with inherent map walls
        if (!this.bounds.rightOf(leftWall) || !this.bounds.leftOf(rightWall)) {
            this.tryHitOrientation(Orientation.HORIZONTAL);
        }
    }

    /**
     * Check for collisions against the other entities present in the current game scene
     * @param scene collection of entities that make up the level currently drawn on the screen
     */
    private void testCollisions(Collection<GameObject> scene) {
        // look for collisions
        for (GameObject other : scene) {
            Bounds otherBounds = other.getBounds();
            if (other == this.placed || !this.bounds.collidesWith(otherBounds)) {
                continue;
            }

            // Collision/interaction cases
            switch (other.getDynamics()) {
                case RIGID -> this.interactionTarget = other;
                case INTERACTIVE -> this.tryInteractionTarget(other);
            }
        }
    }

    /**
     * checks for a collision at a given orientation
     * @param hitOrientation orientation from which the possible collision comes
     */
    private void tryHitOrientation(Orientation hitOrientation) {
        // Vertical rigid collisions take precedence over horizontal ones.
        if (this.hitOrientation == null || this.hitOrientation != Orientation.VERTICAL) {
            this.hitOrientation = hitOrientation;
        }
    }

    /**
     * Checks for an interaction/collision with some other game entity, and resolves what
     * actions should be taken in the game context, such as increasing the player's point
     * count, or reducing their life count
     * @param other entity against which you want to check if there is a collision
     */
    private void tryInteractionTarget(GameObject other) {
        // The closest target is always preferred
        Integer deltaX = other.getPosition().getX() - this.bounds.getOrigin().getX();
        Integer deltaY = other.getPosition().getY() - this.bounds.getOrigin().getY();
        Integer distanceSquare = deltaX * deltaX + deltaY * deltaY;

        if (other.isDangerous() || this.bestDistanceSquare == null || distanceSquare <= this.bestDistanceSquare) {
            this.bestDistanceSquare = distanceSquare;
            this.interactionTarget = other;
        }
    }
}
