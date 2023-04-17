package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.physics.Position;
import cr.ac.tec.ce3104.Game;

// A factory of enemies
public class EnemyFactory {
    /**
     * Interface for the creation of enemies of various types
     * @param type desired enemy type
     * @param difficulty enemy characteristic difficulty
     * @param position enemy starting position.
     * @param isList
     * @param game game of the enemy
     * @return a enemy
     */
    public Enemy createEnemy(EnemyType type, Integer difficulty, Position position, Boolean isList, Game game) {
        switch (type) {
            case SQUID -> {
                return new Squid(difficulty, position, isList, game);
            }
            case CRAB -> {
                return new Crab(difficulty, position, isList, game);
            }
            case OCTOPUS -> {
                return new Octopus(difficulty, position, isList, game);
            }
            default -> {
                assert false;
                return null;
            }
        }
    }
}
