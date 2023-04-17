package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.modes.SquidMoving;
import cr.ac.tec.ce3104.physics.Position;
import cr.ac.tec.ce3104.Game;


// A squid
public class Squid extends Enemy{
    public static final Integer SQUID_SCORE = 10;

    /**
     * Create a new squid and characteristics adapted according to the level of difficulty
     * @param difficulty characteristic difficulty of squid
     * @param position position of the entity
     */
    public Squid(Integer difficulty, Position position, Boolean isList) {
        super(new SquidMoving(Enemy.getSpeedDenominator(difficulty), isList), position, SQUID_SCORE, isList);
    }
}
