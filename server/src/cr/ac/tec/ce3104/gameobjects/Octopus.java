package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.Game;
import cr.ac.tec.ce3104.modes.EnemyMoving;
import cr.ac.tec.ce3104.physics.Position;
import cr.ac.tec.ce3104.resources.Animation;

// A octopus
public class Octopus extends Enemy{
    public static final Integer OCTOPUS_SCORE = 40;

    /**
     * Create a new squid and characteristics adapted according to the level of difficulty
     * @param difficulty characteristic difficulty of squid
     * @param position position of the entity
     * @param isList
     * @param game game of the enemy
     */
    public Octopus(Integer difficulty, Position position, Boolean isList, Game game) {
        super(new EnemyMoving(Enemy.getSpeedDenominator(difficulty), isList, Animation.OCTOPUS_ANIMATION), position, OCTOPUS_SCORE, isList, game);
    }
}
