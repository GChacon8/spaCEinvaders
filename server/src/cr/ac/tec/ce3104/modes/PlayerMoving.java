package cr.ac.tec.ce3104.modes;

import cr.ac.tec.ce3104.gameobjects.Player;
import cr.ac.tec.ce3104.physics.HorizontalDirection;
import cr.ac.tec.ce3104.physics.Position;
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
        return Sprite.SPACECRAFT;
    }

    @Override
    public void onMoveLeft(Player player) {
        this.direction = HorizontalDirection.LEFT;
        player.switchTo(this);
    }

    @Override
    public void onMoveRight(Player player) {
        this.direction = HorizontalDirection.RIGHT;
        player.switchTo(this);
    }

    @Override
    public void onShoot(Player player) {
        Integer posX = player.getPosition().getX() + 3;
        Integer posY = player.getPosition().getY() - 4;
        player.createShoot(new Position(posX, posY));
        player.switchTo(this);
    }

    @Override
    public void onRelease(Player player) {
        player.switchTo(new PlayerStanding(this.direction));
    }
}
