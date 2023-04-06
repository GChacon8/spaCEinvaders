package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.modes.SquidMoving;
import cr.ac.tec.ce3104.physics.Position;

// A squid
public class Squid extends Enemy{
    public static final Integer SQUID_SCORE = 10;

    /**
     * Create a new squid and characteristics adapted according to the level of difficulty
     * @param difficulty characteristic difficulty of squid
     * @param position position of the entity
     */
    public Squid(Integer difficulty, Position position) {
        super(new SquidMoving(Enemy.getSpeedDenominator(difficulty)), position, SQUID_SCORE);
    }
}
