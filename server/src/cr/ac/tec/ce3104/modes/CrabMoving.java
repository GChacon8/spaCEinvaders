package cr.ac.tec.ce3104.modes;

import cr.ac.tec.ce3104.gameobjects.GameObject;
import cr.ac.tec.ce3104.physics.HorizontalDirection;
import cr.ac.tec.ce3104.physics.Orientation;
import cr.ac.tec.ce3104.physics.Speed;
import cr.ac.tec.ce3104.physics.SpeedRatio;
import cr.ac.tec.ce3104.resources.Sequence;

import static cr.ac.tec.ce3104.resources.Animation.CRAB_ANIMATION;

public class CrabMoving implements Mode{
    private static final Integer SPEED_NUMERATOR = 2;
    private Integer moves = 0;

    private HorizontalDirection direction = HorizontalDirection.LEFT;
    private Integer speedDenominator;

    public CrabMoving(Integer speedDenominator) {
        this.speedDenominator = speedDenominator;
    }

    @Override
    public Speed getSpeed() {
        SpeedRatio ratio = new SpeedRatio(CrabMoving.SPEED_NUMERATOR, this.speedDenominator);
        if (this.direction == HorizontalDirection.LEFT) {
            ratio = ratio.negate();
        }
        return Speed.horizontal(ratio);
    }

    @Override
    public Sequence getSequence() {
        return CRAB_ANIMATION;
    }

    @Override
    public void onRelocate(GameObject enemy) {
        if (moves == 30){
            moves = 0;
            this.onHit(enemy, null);
        }else {
            moves++;
        }
    }

    @Override
    public void onHit(GameObject enemy, Orientation orientation) {
        // If it hits, it changes direction
        this.direction = this.direction.invert();
        enemy.switchTo(this);
    }
}
