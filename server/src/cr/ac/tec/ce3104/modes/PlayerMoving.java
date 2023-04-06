package cr.ac.tec.ce3104.modes;

import cr.ac.tec.ce3104.gameobjects.PlayerAvatar;
import cr.ac.tec.ce3104.physics.HorizontalDirection;
import cr.ac.tec.ce3104.physics.Speed;
import cr.ac.tec.ce3104.physics.SpeedRatio;
import cr.ac.tec.ce3104.resources.Sequence;
import cr.ac.tec.ce3104.resources.Sprite;

// The player moves
public class PlayerMoving implements ControllableMode {
    private static final SpeedRatio SPEED_RATIO = new SpeedRatio(6, 3);
    private HorizontalDirection direction;

    /**
     * Create a new state to indicate that the player is moving in the game scenario
     * @param direction horizontal direction in which the player moves
     */
    public PlayerMoving(HorizontalDirection direction) {
        this.direction = direction;
    }

    @Override
    public Speed getSpeed() {
        SpeedRatio ratio = PlayerMoving.SPEED_RATIO;
        if (this.direction == HorizontalDirection.LEFT) {
            ratio = ratio.negate();
        }

        return Speed.horizontal(ratio);
    }

    @Override
    public HorizontalDirection getDirection() {
        return this.direction;
    }

    @Override
    public Sequence getSequence() {
        /*if (this.initial) {
            return Sprite.STANDING_FIRST;
        }

        return this.direction == HorizontalDirection.LEFT ? Sprite.STANDING_LEFT : Sprite.STANDING_RIGHT;*/
        return Sprite.SPACECRAFT;
    }

    @Override
    public void onMoveLeft(PlayerAvatar player) {
        this.direction = HorizontalDirection.LEFT;
        player.switchTo(this);
    }

    @Override
    public void onMoveRight(PlayerAvatar player) {
        this.direction = HorizontalDirection.RIGHT;
        player.switchTo(this);
    }

    @Override
    public void onShoot(PlayerAvatar player) {
        //player.switchTo(new Jumping(this, player));
    }

    @Override
    public void onRelease(PlayerAvatar player) {
        player.switchTo(new PlayerStanding(this.direction));
    }
}
