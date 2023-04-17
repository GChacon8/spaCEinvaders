package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.Game;
import cr.ac.tec.ce3104.modes.Mode;
import cr.ac.tec.ce3104.physics.Dynamics;
import cr.ac.tec.ce3104.physics.Position;

// A enemy
public abstract class Enemy extends GameObject {
    private Integer score;
    private Boolean isList;

    protected Game game;

    /**
     * Constructor that creates an enemy from an existing enemy mode of operation
     * @param mode how the enemy should be created
     * @param position position in which the enemy is initially placed
     * @param score score given by the enemy when eliminated by the player
     */
    protected Enemy(Mode mode, Position position, Integer score, Game game) {
        super(mode, position);
        this.score = score;
        this.isList = false;
        this.game = game;
    }
    /**
     * Constructor that creates an enemy from an existing enemy mode of operation
     * @param mode how the enemy should be created
     * @param position position in which the enemy is initially placed
     * @param score score given by the enemy when eliminated by the player
     * @param isList validates if the enemy is created in a list
     */
    protected Enemy(Mode mode, Position position, Integer score, Boolean isList) {
        super(mode, position);
        this.score = score;
        this.isList = isList;
    }
    @Override
    public Dynamics getDynamics() {
        return Dynamics.INTERACTIVE;
    }

    /**
     * Obtains the score that should be added to the player if he eliminates the enemy
     * @return score given by the enemy when eliminated by the player
     */
    public Integer getScore() {
        return this.score;
    }

    @Override
    public Boolean isDangerous() {
        return true;
    }

    @Override
    protected Integer getZ() {
        return 2;
    }

    /**
     * Gets every how many ticks there is a movement of the entity
     * @param difficulty indicates the level of difficulty, the higher it is, the more it affects the speed of the enemy
     * @return number of ticks for a move by the player
     */
    protected static Integer getSpeedDenominator(Integer difficulty) {
        return Math.max(1, 3 - difficulty);
    }

    public void createShot(Position position){
        game.spawn(new EnemyShot(0, position));
    }
}
