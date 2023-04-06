package cr.ac.tec.ce3104.modes;

import cr.ac.tec.ce3104.gameobjects.GameObject;
import cr.ac.tec.ce3104.physics.HorizontalDirection;
import cr.ac.tec.ce3104.physics.Speed;
import cr.ac.tec.ce3104.physics.SpeedRatio;
import cr.ac.tec.ce3104.physics.VerticalDirection;
import cr.ac.tec.ce3104.resources.Sequence;
import cr.ac.tec.ce3104.resources.Sprite;

public class PlayerShotMoving implements Mode{
    private static final Integer SPEED_NUMERATOR = 4;
    private VerticalDirection direction = VerticalDirection.UP;
    private Integer speedDenominator;

    public PlayerShotMoving(Integer speedDenominator) {
        this.speedDenominator = speedDenominator;
    }

    @Override
    public Speed getSpeed() {
        SpeedRatio ratio = new SpeedRatio(PlayerShotMoving.SPEED_NUMERATOR, this.speedDenominator);
        if (this.direction == VerticalDirection.UP) {
            ratio = ratio.negate();
        }

        return Speed.vertical(ratio);
    }

    @Override
    public Sequence getSequence() {
        return Sprite.SHOT1;
    }

    @Override
    public void onRelocate(GameObject playershot) {
        if (playershot.getPosition().getY() < -20) {
            playershot.delete();
        }
    }
}
