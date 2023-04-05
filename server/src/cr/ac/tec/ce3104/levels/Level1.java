package cr.ac.tec.ce3104.levels;

import cr.ac.tec.ce3104.Game;
import cr.ac.tec.ce3104.gameobjects.Bunker;
import cr.ac.tec.ce3104.gameobjects.PlayerAvatar;
import cr.ac.tec.ce3104.physics.Position;
import cr.ac.tec.ce3104.physics.Size;
import cr.ac.tec.ce3104.resources.Sprite;

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
        //Bunkers
        game.spawn(new Bunker(new Position(48, 180)));
        game.spawn(new Bunker(new Position(92, 180)));
        game.spawn(new Bunker(new Position(138, 180)));
        game.spawn(new Bunker(new Position(182, 180)));

        // PLayer
        Integer initialX = 220 - Sprite.SPACECRAFT.getSize().getHeight();
        return game.spawn(new PlayerAvatar(new Position(123, initialX), initialScore, game));
    }
}
