package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.modes.EnemyShotMoving;
import cr.ac.tec.ce3104.physics.Position;

// Class for enemy shots
public class EnemyShot extends Shot{

    /**
     * Constructor of the class.
     * @param difficulty difficulty of the game.
     * @param position position of the shot.
     */
    public EnemyShot(Integer difficulty, Position position) {
        super(new EnemyShotMoving(Shot.getSpeedDenominator(difficulty)), position);
    }
}
