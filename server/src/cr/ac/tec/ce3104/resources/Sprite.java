package cr.ac.tec.ce3104.resources;

import cr.ac.tec.ce3104.physics.Size;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

// Sprites
public class Sprite extends Sequence{
    // Enemies
    public static final Sprite SQUID1   = Sprite.byId(0);
    public static final Sprite SQUID2   = Sprite.byId(1);
    public static final Sprite CRAB1    = Sprite.byId(2);
    public static final Sprite CRAB2    = Sprite.byId(3);
    public static final Sprite OCTOPUS1 = Sprite.byId(4);
    public static final Sprite OCTOPUS2 = Sprite.byId(5);

    // Explosion
    public static final Sprite EXPLOSION = Sprite.byId(6);

    // Flying saucer
    public static final Sprite FLYING_SAUCER = Sprite.byId(7);

    // Bunker
    public static final Sprite BUNKER = Sprite.byId(8);

    // Spacecraft
    public static final Sprite SPACECRAFT = Sprite.byId(9);

    // Shoots
    public static final Sprite SHOT1 = Sprite.byId(10);
    public static final Sprite SHOT2 = Sprite.byId(11);
    public static final Sprite SHOT3 = Sprite.byId(12);
    public static final Sprite SHOT4 = Sprite.byId(13);

    // known sprites
    private static HashMap<Integer, Sprite> sprites = null;

    // Sprite id and size
    private Integer id;
    private Size size;

    /**
     * Create a sprite instance from a file path
     * @param path image file path
     * @throws IOException error in case of problems opening image file
     */
    private Sprite(File path) throws IOException {
        String filename = path.getName();
        this.id = Integer.parseInt(filename.substring(0, filename.indexOf('-')));

        BufferedImage image = ImageIO.read(path);
        this.size = new Size(image.getWidth(), image.getHeight());
    }

    /**
     * Create and load an image as a sprite to be used in the game
     * @param id identifier associated with the sprite image file. Same for server and clients
     * @return sprite associated with the provided id
     */
    public static Sprite byId(Integer id) {
        if (Sprite.sprites == null) {
            Sprite.sprites = new HashMap<>();

            try {
                for (File path : Assets.listSpritePaths()) {
                    Sprite sprite = new Sprite(path);
                    Sprite.sprites.put(sprite.id, sprite);
                }
            } catch (IOException exception) {
                throw new ExceptionInInitializerError(exception);
            }
        }

        return Sprite.sprites.get(id);
    }

    // Gets this sprite in the form of an array of one
    @Override
    public Sprite[] getSprites() {
        return new Sprite[] { this };
    }

    // Gets the size of the sprite
    @Override
    public Size getSize() {
        return this.size;
    }

    /**
     * Gets the id associated with the current sprite instance
     * @return identifier of the image of the current sprite
     */
    public Integer getId() {
        return this.id;
    }
}
