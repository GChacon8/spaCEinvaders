package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.Game;
import cr.ac.tec.ce3104.modes.EnemyMoving;
import cr.ac.tec.ce3104.physics.Position;
import cr.ac.tec.ce3104.resources.Animation;

// A crab
public class Crab extends Enemy{
    public static final Integer CRAB_SCORE = 20;

    /**
     * Create a new crab and characteristics adapted according to the level of difficulty
     * @param difficulty characteristic difficulty of squid
     * @param position position of the entity
     * @param isList validates if the crab is created in a list
     * @param isList
     * @param game game of the enemy
     */
    public Crab(Integer difficulty, Position position, Boolean isList, Game game) {
        super(new EnemyMoving(Enemy.getSpeedDenominator(difficulty), isList, Animation.CRAB_ANIMATION), position, CRAB_SCORE, isList, game);
    }
}
