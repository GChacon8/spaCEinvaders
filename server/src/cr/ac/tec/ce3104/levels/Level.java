package cr.ac.tec.ce3104.levels;

import cr.ac.tec.ce3104.Game;
import cr.ac.tec.ce3104.gameobjects.Player;
import cr.ac.tec.ce3104.physics.Size;

public interface Level {
    /**
     * Gets the size of the reference game area (virtual game resolution)
     * @return virtual game resolution
     */
    Size getGameAreaSize();

    /**
     * Performs the necessary operations to draw the level on a given game
     * @param game game on which you want to draw the level
     * @param initialScore initial score with which the level begins
     * @return Reference to the entity representing the player
     */
    Player setup(Game game, Integer initialScore, Integer initialLives);
}
