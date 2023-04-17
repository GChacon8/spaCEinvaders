package cr.ac.tec.ce3104.modes;

import cr.ac.tec.ce3104.gameobjects.GameObject;
import cr.ac.tec.ce3104.gameobjects.Octopus;
import cr.ac.tec.ce3104.physics.*;
import cr.ac.tec.ce3104.resources.Sequence;

import static cr.ac.tec.ce3104.resources.Animation.OCTOPUS_ANIMATION;

public class OctopusMoving implements Mode{
    private static final Integer SPEED_NUMERATOR = 7;
    private static final Integer SPEED_NUMERATOR_LIST = 2;

    private Integer moves = 0;
    private Boolean isList;
    private HorizontalDirection direction = HorizontalDirection.LEFT;
    private Integer speedDenominator;

    public OctopusMoving(Integer speedDenominator, Boolean isList) {

        this.speedDenominator = speedDenominator;
        this.isList = isList;
    }

    @Override
    public Speed getSpeed() {
        SpeedRatio ratio;
        if(isList){
            ratio = new SpeedRatio(OctopusMoving.SPEED_NUMERATOR_LIST, this.speedDenominator);
        }
        else{
            ratio = new SpeedRatio(OctopusMoving.SPEED_NUMERATOR, this.speedDenominator);
        }
        if (this.direction == HorizontalDirection.LEFT) {
            ratio = ratio.negate();
        }
        return Speed.horizontal(ratio);
    }

    @Override
    public Sequence getSequence() {
        return OCTOPUS_ANIMATION;
    }

    @Override
    public void onRelocate(GameObject enemy) {
        if (moves == 33){
            this.onHit(enemy, null);
        }else {
            moves++;
        }
    }

    @Override
    public void onHit(GameObject enemy, Orientation orientation) {
        // If it hits, it changes direction
        this.direction = this.direction.invert();
        this.speedDenominator = 10 - enemy.getPosition().getY()/32;
        moves = 0;
        enemy.relocate(new Position(enemy.getPosition().getX(),enemy.getPosition().getY()+10));
        enemy.switchTo(this);
    }

    @Override
    public void onShoot(Octopus octopus) {
        Integer posX = octopus.getPosition().getX();
        Integer posY = octopus.getPosition().getY() + 4;
        octopus.createShot(new Position(posX, posY));
    }
}
