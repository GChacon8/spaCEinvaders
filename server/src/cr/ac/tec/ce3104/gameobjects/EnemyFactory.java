package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.physics.Position;

// A factory of enemies
public class EnemyFactory {
    /**
     * Interface for the creation of enemies of various types
     * @param type desired enemy type
     * @param difficulty enemy characteristic difficulty
     * @param position enemy starting position
     * @return a enemy
     */
    public Enemy createEnemy(EnemyType type, Integer difficulty, Position position, Boolean isList) {
        switch (type) {
            case SQUID -> {
                return new Squid(difficulty, position, isList);
            }
            case CRAB -> {
                return new Crab(difficulty, position, isList);
            }
            case OCTOPUS -> {
                return new Octopus(difficulty, position, isList);
            }
            default -> {
                assert false;
                return null;
            }
        }
    }
}
