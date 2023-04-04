package cr.ac.tec.ce3104.physics;

// Size (width, height)
public class Size {
    private Integer width;
    private Integer height;

    /**
     * Represents the dimensions of an object
     * @param width object width
     * @param height object height
     */
    public Size(Integer width, Integer height) {
        this.width = width;
        this.height = height;
    }

    /**
     * @return width
     */
    public Integer getWidth() {
        return this.width;
    }

    /**
     * @return height
     */
    public Integer getHeight() {
        return this.height;
    }
}
