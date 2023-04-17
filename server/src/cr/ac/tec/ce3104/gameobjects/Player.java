package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.Game;
import cr.ac.tec.ce3104.modes.Mode;
import cr.ac.tec.ce3104.modes.PlayerMoving;
import cr.ac.tec.ce3104.modes.PlayerStanding;
import cr.ac.tec.ce3104.physics.Dynamics;
import cr.ac.tec.ce3104.physics.Position;

import static java.lang.Thread.sleep;


public class Player extends GameObject{
    private Integer score;
    private Boolean lost = false;
    private Boolean hasKey = false;
    private Boolean hasShotAvailable = true;
    private Game game;

    /**
     * Constructor to generate the entity that represents the player of the game
     * @param position player starting position
     * @param initialScore initial score associated with the player
     * @param game game the player is in
     */
    public Player(Position position, Integer initialScore, Game game) {
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
            //this.game.testCollisions(this, this.getPosition());
        }

        super.switchTo(newMode);
    }

    @Override
    public void onInteraction(GameObject other) {
        if (other.isDangerous()) {
            //this.die();
            System.out.println("You die");
        }
    }

    @Override
    public void onFloatingContact(GameObject floating) {
        System.out.println("onFloatingContact");
    }

    public void createShoot(Position position) {
        if(hasShotAvailable){
            game.spawn(new PlayerShot(0, position));

            // Waits 2 seconds to reload.
            Runnable reloadShot = () -> {
                this.hasShotAvailable = false;
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                this.hasShotAvailable = true;
            };
            Thread t = new Thread(reloadShot);

            t.start();
        }
    }

    /**
     * Adds the given score difference to the player's current score
     * @param difference difference between current score and updated score
     */
    public void updateScore(Integer difference) {
        this.score += difference;
        this.refreshMode();
    }

    /**
     * Get player's score
     * @return player score
     */
    public Integer getScore() {
        return this.score;
    }
}
