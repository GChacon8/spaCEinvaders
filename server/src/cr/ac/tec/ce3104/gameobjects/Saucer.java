package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.modes.SquidMoving;
import cr.ac.tec.ce3104.physics.Position;

// A flaying saucer
public class Saucer extends Enemy{
    /**
     * Create a new flaying saucer and characteristics adapted according to the level of difficulty
     * @param difficulty characteristic difficulty of squid
     * @param position position of the entity
     * @param score score of the flaying saucer
     */
    public Saucer(Integer difficulty, Position position, Integer score) {
        super(new SquidMoving(Enemy.getSpeedDenominator(difficulty)), position, score);
    }
}
