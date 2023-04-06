package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.modes.PlayerShotMoving;
import cr.ac.tec.ce3104.physics.Position;

public class PlayerShot extends Shot{
    /**
     * Create a shot when the player press space or w
     * @param difficulty entity velocity
     * @param position same as player
     */
    public PlayerShot(Integer difficulty, Position position) {
        super(new PlayerShotMoving(Shot.getSpeedDenominator(difficulty)), position);
    }
}
