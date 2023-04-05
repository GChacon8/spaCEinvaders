package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.comms.Command;
import cr.ac.tec.ce3104.modes.Mode;
import cr.ac.tec.ce3104.modes.Static;
import cr.ac.tec.ce3104.physics.Dynamics;
import cr.ac.tec.ce3104.physics.Position;
import cr.ac.tec.ce3104.physics.Size;
import cr.ac.tec.ce3104.resources.Sprite;

public abstract class GameObject {
    // Internal Entity Properties
    private Integer id;
    private Position position;
    private Mode mode;
    private GameObjectObserver observer = null;
    private static Integer nextId = 0;

    /**
     * Create a new entity from a sprite and a position
     * @param staticSprite sprite of the entity when it is static
     * @param position
     */
    public GameObject(Sprite staticSprite, Position position) {
        this(new Static(staticSprite), position);
    }
    /**
     * Create a new game entity from a mode(state) and a position
     * @param mode entity mode
     * @param position entity position
     */
    public GameObject(Mode mode, Position position) {
        this.id = nextId++;
        this.position = position;
        this.mode = mode;
    }

    /**
     * Make the command to request the creation of the entity in the game clients
     * @return command to send to different clients
     */
    public Command makePutCommand() {
        return Command.cmdPut(this.id, this.position, this.getZ(), this.mode.getSpeed(), this.mode.getSequence());
    }

    /**
     * Make the command to request the removal of the current entity from the game clients
     * @return command to send to different clients
     */
    public Command makeDeleteCommand() {
        return Command.cmdDelete(this.id);
    }

    /**
     * Add a subscriber to be notified when there are changes in the state of the entity
     * @param observer observer to subscribe to the entity
     */
    public void addObserver(GameObjectObserver observer) {
        assert this.observer == null;
        this.observer = observer;
    }

    /**
     * Changes the position of the entity to a new given position
     * @param position position to relocate the entity
     */
    public void relocate(Position position) {
        this.position = position;
        this.mode.onRelocate(this);
    }

    /**
     * Gets the type of collisions that can be had with the object
     * @return type of collision with the object
     */
    public abstract Dynamics getDynamics();

    /**
     * Change the mode of the rendered entity
     * @param newMode mode you want to transition to
     */
    public void switchTo(Mode newMode) {
        Mode previous = this.mode;
        this.mode = newMode;

        if (this.observer != null) {
            this.observer.onObjectModeChanged(this);
            if (previous.getClass() != newMode.getClass()) {
                String previousName = previous.getClass().getSimpleName();
                String newName = newMode.getClass().getSimpleName();

                this.observer.log("Object " + this + " switched from " + previousName + " to " + newName);
            }
        }
    }

    /**
     * Indicates the routine to be executed when the object interacts with some other entity
     * @param other object with which the interaction occurs
     */
    public void onInteraction(GameObject other) {}

    /**
     * Indicates the routine to be executed if the entity comes into contact with a floating object
     * @param floating floating object to interact with
     */
    public void onFloatingContact(GameObject floating) {}

    /**
     * Indicates whether the current entity is capable of damaging the player
     * @return true if the entity can harm the player, false otherwise
     */
    public Boolean isDangerous() {
        return false;
    }

    protected Integer getZ() {
        return 0;
    }

    /**
     * Constructs a string to describe a game entity in a simple way
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " #" + this.id + " at (" + this.position.getX() + ", " + this.position.getY() + ")";
    }

    /**
     * Gets the id of the represented entity (same for client and server)
     * @return entity identifier
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Gets the current mode the entity is in
     * @return Entity's current mode
     */
    public Mode getMode() {
        return this.mode;
    }

    /**
     * Gets the current position of the entity
     * @return current position of the entity
     */
    public Position getPosition() {
        return this.position;
    }

    /**
     * Gets the horizontal and vertical dimensions of the feature
     * @return entity dimensions
     */
    public Size getSize() {
        return this.mode.getSequence().getSize();
    }
}
