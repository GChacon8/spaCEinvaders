package cr.ac.tec.ce3104.modes;

import cr.ac.tec.ce3104.gameobjects.PlayerAvatar;
import cr.ac.tec.ce3104.physics.HorizontalDirection;
import cr.ac.tec.ce3104.physics.Speed;
import cr.ac.tec.ce3104.resources.Sequence;
import cr.ac.tec.ce3104.resources.Sprite;

// The player stands still
public class PlayerStanding implements ControllableMode {
    private HorizontalDirection direction;
    private Boolean initial = false;

    /**
     * Creates a new mode for the player which indicates that the player's avatar is immobile
     * @param direction horizontal direction the player can move
     */
    public PlayerStanding(HorizontalDirection direction) {
        this.direction = direction;
    }

    /**
     * Creates a new mode for the player at the start of game position
     * @return standing mode used in the starting position.
     */
    public static PlayerStanding initial() {
        PlayerStanding mode = new PlayerStanding(HorizontalDirection.RIGHT);
        mode.initial = true;
        return mode;
    }

    @Override
    public Speed getSpeed() {
        return Speed.stationary();
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
        player.switchTo(new PlayerMoving(HorizontalDirection.LEFT));
    }

    @Override
    public void onMoveRight(PlayerAvatar player) {
        player.switchTo(new PlayerMoving(HorizontalDirection.RIGHT));
    }

    @Override
    public void onShoot(PlayerAvatar player) {
        //player.switchTo(new Jumping(this, player));
    }
}
