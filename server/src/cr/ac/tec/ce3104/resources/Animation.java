package cr.ac.tec.ce3104.resources;

// A sequence of several looping sprites
public class Animation extends Sequence{
    // Sprite sequence
    private Sprite[] sprites;

    // Enemy animations
    public static final Animation SQUID_ANIMATION   = new Animation(0, 1);
    public static final Animation CRAB_ANIMATION    = new Animation(2, 3);
    public static final Animation OCTOPUS_ANIMATION = new Animation(4, 5);

    // Gets the sprites
    @Override
    public Sprite[] getSprites() {
        return this.sprites;
    }

    /**
     * Creates an animation instance from a list of sprite image ids
     * @param spriteIds initialization list with the identifiers of the sprites that make up the animation
     */
    private Animation(Integer... spriteIds) {
        this.sprites = new Sprite[spriteIds.length];
        for (Integer i = 0; i < spriteIds.length; ++i) {
            this.sprites[i] = Sprite.byId(spriteIds[i]);
        }
    }
}
