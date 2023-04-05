package cr.ac.tec.ce3104.modes;

import cr.ac.tec.ce3104.physics.Speed;
import cr.ac.tec.ce3104.resources.Sequence;
import cr.ac.tec.ce3104.resources.Sprite;

// Mode used for static objects and for freezing entities
public class Static implements Mode{
    /**
     * Creates a game mode indicating that a game entity remains static
     * @param sprite reference to sprite containing the static image of the entity
     */
    public Static(Sprite sprite) {
        this.sprite = sprite;
    }

    // Static speed
    @Override
    public Speed getSpeed() {
        return Speed.stationary();
    }

    // Static sequence
    @Override
    public Sequence getSequence() {
        return this.sprite;
    }

    // Static sprite
    private Sprite sprite;
}
