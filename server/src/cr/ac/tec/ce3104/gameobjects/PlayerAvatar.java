package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.Game;
import cr.ac.tec.ce3104.modes.Mode;
import cr.ac.tec.ce3104.modes.PlayerMoving;
import cr.ac.tec.ce3104.modes.PlayerStanding;
import cr.ac.tec.ce3104.physics.Dynamics;
import cr.ac.tec.ce3104.physics.Position;

public class PlayerAvatar extends GameObject{
    private Integer score;
    private Boolean lost = false;
    private Boolean hasKey = false;
    private Game game;

    /**
     * Constructor to generate the entity that represents the player of the game
     * @param position player starting position
     * @param initialScore initial score associated with the player
     * @param game game the player is in
     */
    public PlayerAvatar(Position position, Integer initialScore, Game game) {
        super(PlayerStanding.initial(), position);
        this.score = initialScore;
        this.game = game;
    }

    /**
     * Indicates if the player is in a state where they have lost the game
     * @return true if the player lost the game, false otherwise
     */
    public Boolean hasLost() {
        return this.lost;
    }

    @Override
    public Dynamics getDynamics() {
        return this.lost ? Dynamics.FLOATING : Dynamics.INTERACTIVE;
    }

    /**
     * Change the dynamics mode of the player.
     * @param newMode mode you want to transition to
     */
    @Override
    public void switchTo(Mode newMode) {
        if (this.getMode() != newMode && newMode instanceof PlayerMoving) {
            //Placement placement = this.game.testCollisions(this, this.getPosition());

            /*GameObject target = placement.getInteractionTarget();
            if (target == null || !(target instanceof Vines) || !this.inLastVines((Vines)target)) {
                this.lastVines = null;
            }*/
        }

        super.switchTo(newMode);
    }

    public void createShoot(Position position) {
        game.spawn(new PlayerShot(0, position));
    }
}
