package cr.ac.tec.ce3104.modes;

import cr.ac.tec.ce3104.gameobjects.GameObject;
import cr.ac.tec.ce3104.physics.HorizontalDirection;
import cr.ac.tec.ce3104.physics.Speed;
import cr.ac.tec.ce3104.physics.SpeedRatio;
import cr.ac.tec.ce3104.resources.Sequence;
import cr.ac.tec.ce3104.resources.Sprite;

public class SaucerMoving implements Mode{
    private static final Integer SPEED_NUMERATOR = 4;

    private HorizontalDirection direction;
    private Integer speedDenominator;

    public SaucerMoving(Integer speedDenominator, HorizontalDirection direction) {
        this.speedDenominator = speedDenominator;
        this.direction = direction;
    }

    @Override
    public Speed getSpeed() {
        SpeedRatio ratio = new SpeedRatio(SaucerMoving.SPEED_NUMERATOR, this.speedDenominator);
        if (this.direction == HorizontalDirection.LEFT) {
            ratio = ratio.negate();
        }
        return Speed.horizontal(ratio);
    }

    @Override
    public Sequence getSequence() {
        return Sprite.FLYING_SAUCER;
    }

    @Override
    public void onRelocate(GameObject saucer) {
        if (saucer.getPosition().getX() < -20 || saucer.getPosition().getX() > 260) {
            saucer.delete();
        }
    }
}
