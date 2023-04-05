package cr.ac.tec.ce3104.resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

// Control directory "assets/"
public class Assets {
    /**
     * Gets all sprite image file paths in the game assets folder
     * @return Image file paths containing sprites
     * @throws IOException error that occurs due to a problem encountered when trying to open one of the files
     */
    public static File[] listSpritePaths() throws IOException {
        ArrayList<File> paths = new ArrayList<>();

        for (File category : new File("/home/emanuel/Desktop/spaCEinvaders/assets/sprites").listFiles()) {
            paths.addAll(Arrays.asList(category.listFiles()));
        }

        return paths.toArray(new File[0]);
    }
}
