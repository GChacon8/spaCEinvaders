package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.physics.Dynamics;
import cr.ac.tec.ce3104.physics.Position;
import cr.ac.tec.ce3104.resources.Sprite;

public class Bunker extends GameObject{

    /**
     * Builder to create a bunker entity
     * @param position in which you want to place the bunker on the game screen
     */
    public Bunker(Position position) {
        super(Sprite.BUNKER1, position);
    }

    @Override
    public Dynamics getDynamics() {
        return Dynamics.RIGID;
    }
}
