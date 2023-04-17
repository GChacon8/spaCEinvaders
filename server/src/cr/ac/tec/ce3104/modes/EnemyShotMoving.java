package cr.ac.tec.ce3104.modes;

import cr.ac.tec.ce3104.gameobjects.GameObject;
import cr.ac.tec.ce3104.physics.Speed;
import cr.ac.tec.ce3104.physics.SpeedRatio;
import cr.ac.tec.ce3104.physics.VerticalDirection;
import cr.ac.tec.ce3104.resources.Sequence;
import cr.ac.tec.ce3104.resources.Sprite;

import java.util.Random;

import static cr.ac.tec.ce3104.resources.Animation.ENEMY_SHOT_ANIMATION;

public class EnemyShotMoving implements Mode{

    private static final Integer SPEED_NUMERATOR = 4;
    private VerticalDirection direction = VerticalDirection.DOWN;
    private Integer speedDenominator;

    /**
     * Constructor of the class.
     * @param speedDenominator
     */
    public EnemyShotMoving(Integer speedDenominator) {
        this.speedDenominator = speedDenominator;
    }

    @Override
    public Speed getSpeed() {
        SpeedRatio ratio = new SpeedRatio(EnemyShotMoving.SPEED_NUMERATOR, this.speedDenominator);
        return Speed.vertical(ratio);
    }

    @Override
    public Sequence getSequence()  {
        Random rand = new Random();
        Integer randomSprite = rand.nextInt(3);

        if(randomSprite == 0){
            return Sprite.SHOT2;
        }else{
            return Sprite.SHOT3;
        }

    }

    @Override
    public void onRelocate(GameObject playershot) {
        if (playershot.getPosition().getY() > 260) {
            playershot.delete();
        }
    }
}
