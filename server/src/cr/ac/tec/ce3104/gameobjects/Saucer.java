package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.modes.SaucerMoving;
import cr.ac.tec.ce3104.physics.HorizontalDirection;
import cr.ac.tec.ce3104.physics.Position;

// A flaying saucer
public class Saucer extends Enemy{
    /**
     * Create a new flaying saucer and characteristics adapted according to the level of difficulty
     * @param difficulty characteristic difficulty of squid
     * @param direction direction to move
     * @param position position of the entity
     * @param score score of the flaying saucer
     */
    public Saucer(Integer difficulty, HorizontalDirection direction, Position position, Integer score) {
        super(new SaucerMoving(Enemy.getSpeedDenominator(difficulty), direction), position, score);
    }
}
