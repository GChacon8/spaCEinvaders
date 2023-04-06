package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.modes.Mode;
import cr.ac.tec.ce3104.physics.Dynamics;
import cr.ac.tec.ce3104.physics.Position;

// A shoot
public abstract class Shot extends GameObject{
    /**
     * Constructor that creates an enemy from an existing enemy mode of operation
     * @param mode how the enemy should be created
     * @param position position in which the enemy is initially placed
     */
    protected Shot(Mode mode, Position position) {
        super(mode, position);
    }

    @Override
    public Dynamics getDynamics() {
        return Dynamics.INTERACTIVE;
    }

    @Override
    public Boolean isDangerous() {
        return true;
    }

    @Override
    protected Integer getZ() {
        return 2;
    }

    /**
     * Gets every how many ticks there is a movement of the entity
     * @param difficulty indicates the level of difficulty, the higher it is, the more it affects the speed of the enemy
     * @return number of ticks for a move by the player
     */
    protected static Integer getSpeedDenominator(Integer difficulty) {
        return Math.max(1, 3 - difficulty);
    }
}
