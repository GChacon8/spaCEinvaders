package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.modes.EnemyMoving;
import cr.ac.tec.ce3104.physics.Position;
import cr.ac.tec.ce3104.Game;
import cr.ac.tec.ce3104.resources.Animation;


// A squid
public class Squid extends Enemy{
    public static final Integer SQUID_SCORE = 10;

    /**
     * Create a new squid and characteristics adapted according to the level of difficulty
     * @param difficulty characteristic difficulty of squid
     * @param position position of the entity
     * @param isList
     * @param game game of the enemy
     */
    public Squid(Integer difficulty, Position position, Boolean isList, Game game) {
        super(new EnemyMoving(Enemy.getSpeedDenominator(difficulty), isList, Animation.SQUID_ANIMATION), position, SQUID_SCORE, isList, game);
    }
}
