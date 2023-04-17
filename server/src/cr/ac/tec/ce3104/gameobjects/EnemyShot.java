package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.modes.EnemyShotMoving;
import cr.ac.tec.ce3104.modes.Mode;
import cr.ac.tec.ce3104.physics.Position;

public class EnemyShot extends Shot{

    public EnemyShot(Integer difficulty, Position position) {
        super(new EnemyShotMoving(Shot.getSpeedDenominator(difficulty)), position);
    }


}
