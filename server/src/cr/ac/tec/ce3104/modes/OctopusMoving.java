package cr.ac.tec.ce3104.modes;

import cr.ac.tec.ce3104.physics.HorizontalDirection;
import cr.ac.tec.ce3104.physics.Speed;
import cr.ac.tec.ce3104.physics.SpeedRatio;
import cr.ac.tec.ce3104.resources.Sequence;

import static cr.ac.tec.ce3104.resources.Animation.OCTOPUS_ANIMATION;

public class OctopusMoving implements Mode{
    private static final Integer SPEED_NUMERATOR = 2;

    private HorizontalDirection direction = HorizontalDirection.LEFT;
    private Integer speedDenominator;

    public OctopusMoving(Integer speedDenominator) {
        this.speedDenominator = speedDenominator;
    }

    @Override
    public Speed getSpeed() {
        SpeedRatio ratio = new SpeedRatio(OctopusMoving.SPEED_NUMERATOR, this.speedDenominator);
        if (this.direction == HorizontalDirection.LEFT) {
            ratio = ratio.negate();
        }
        return Speed.horizontal(ratio);
    }

    @Override
    public Sequence getSequence() {
        return OCTOPUS_ANIMATION;
    }
}
