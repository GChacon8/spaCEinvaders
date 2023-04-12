package cr.ac.tec.ce3104.levels;

import cr.ac.tec.ce3104.Game;
import cr.ac.tec.ce3104.gameobjects.*;
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
    public Player setup(Game game, Integer initialScore) {
        // Bunkers
        Integer i;
        Integer posX = 34;
        Integer posY = 190;
        Integer count = 0;
        while(count <= 3){
            for (i = 0; i <= 2; i++){
                game.spawn(new Bunker(new Position(posX, posY)));
                posX += 4;
            }

            posX -= 16;
            posY += 2;
            for (i = 0; i <= 4; i++){
                game.spawn(new Bunker(new Position(posX, posY)));
                posX += 4;
            }

            posX -= 22;
            posY += 4;
            for (i = 0; i <= 5; i++){
                game.spawn(new Bunker(new Position(posX, posY)));
                posX += 4;
            }

            posX -= 16;
            posY += 2;
            game.spawn(new Bunker(new Position(posX, posY)));

            posX += 4;
            game.spawn(new Bunker(new Position(posX, posY)));

            posX -= 12;
            posY += 2;
            game.spawn(new Bunker(new Position(posX, posY)));

            posX += 4;
            game.spawn(new Bunker(new Position(posX, posY)));

            posX += 12;
            game.spawn(new Bunker(new Position(posX, posY)));

            posX += 4;
            game.spawn(new Bunker(new Position(posX, posY)));

            posX -= 20;
            posY += 4;
            game.spawn(new Bunker(new Position(posX, posY)));

            posX += 4;
            game.spawn(new Bunker(new Position(posX, posY)));

            posX += 12;
            game.spawn(new Bunker(new Position(posX, posY)));

            posX += 4;
            game.spawn(new Bunker(new Position(posX, posY)));

            posX += 45;
            posY = 190;
            count++;
        }

        // Enemies
        /*posX = 74;
        posY = 70;
        for (i = 0; i <= 8; i++) {
            game.spawn(new EnemyFactory().createEnemy(EnemyType.OCTOPUS, -6, new Position(posX, posY)));
            posX += 20;
        }

        posX = 74;
        posY = 90;
        for (i = 0; i <= 8; i++) {
            game.spawn(new EnemyFactory().createEnemy(EnemyType.CRAB, -6, new Position(posX, posY)));
            posX += 20;
        }

        posX = 74;
        posY = 110;
        for (i = 0; i <= 8; i++) {
            game.spawn(new EnemyFactory().createEnemy(EnemyType.SQUID, -6, new Position(posX, posY)));
            posX += 20;
        }*/

        // PLayer
        Integer initialX = 230 - Sprite.SPACECRAFT.getSize().getHeight();
        return game.spawn(new Player(new Position(123, initialX), initialScore, game));
    }
}
