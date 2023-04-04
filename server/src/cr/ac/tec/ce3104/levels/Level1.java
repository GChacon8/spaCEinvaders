package cr.ac.tec.ce3104.levels;

import cr.ac.tec.ce3104.Game;
import cr.ac.tec.ce3104.gameobjects.PlayerAvatar;
import cr.ac.tec.ce3104.physics.Size;

public class Level1 implements Level{
    /**
     * Sets the client's game screen resolution
     */
    @Override
    public Size getGameAreaSize() {
        // native NES resolution
        // aspect ratio 16:15 in case you want to climb
        return new Size(256, 240);
    }

    @Override
    public PlayerAvatar setup(Game game, Integer initialScore) {
        return null;
    }
}
