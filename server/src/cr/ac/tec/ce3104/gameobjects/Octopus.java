package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.Game;
import cr.ac.tec.ce3104.modes.OctopusMoving;
import cr.ac.tec.ce3104.physics.Position;

// A octopus
public class Octopus extends Enemy{
    public static final Integer OCTOPUS_SCORE = 40;

    /**
     * Create a new squid and characteristics adapted according to the level of difficulty
     * @param difficulty characteristic difficulty of squid
     * @param position position of the entity
     */
    public Octopus(Integer difficulty, Position position, Boolean isList, Game game) {
        super(new OctopusMoving(Enemy.getSpeedDenominator(difficulty), isList), position, OCTOPUS_SCORE, isList, game);
    }
}
