package cr.ac.tec.ce3104.physics;

// Position(x,y)
public class Position {
    private Integer x;
    private Integer y;

    /**
     * Create a coordinate representation
     * @param x valor horizontal component
     * @param y valor vertical component
     */
    public Position(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return horizontal component
     */
    public Integer getX() {
        return this.x;
    }

    /**
     * @return vertical component
     */
    public Integer getY() {
        return this.y;
    }
}
