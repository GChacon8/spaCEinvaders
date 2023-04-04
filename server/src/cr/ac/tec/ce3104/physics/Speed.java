package cr.ac.tec.ce3104.physics;

// Velocity(x,y)
public class Speed {
    private SpeedRatio x;
    private SpeedRatio y;

    /**
     * Creates an instance of velocity that represents a steady state
     * @return instance of Speed for steady state
     */
    public static Speed stationary() {
        return new Speed(SpeedRatio.stationary(), SpeedRatio.stationary());
    }

    /**
     * creates a horizontal velocity only
     * @param horizontal speed value
     * @return reference to Speed object created from given value
     */
    public static Speed horizontal(SpeedRatio horizontal) {
        return new Speed(horizontal, SpeedRatio.stationary());
    }

    /**
     * creates a vertical velocity only
     * @param vertical speed value
     * @return reference to Speed object created from given value
     */
    public static Speed vertical(SpeedRatio vertical) {
        return new Speed(SpeedRatio.stationary(), vertical);
    }

    /**
     * Constructs a composite velocity representation from velocities in two axes
     * @param x velocity value on horizontal axis
     * @param y velocity value in vertical axis
     */
    public Speed(SpeedRatio x, SpeedRatio y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Get velocity value for horizontal axis
     * @return horizontal axis speed
     */
    public SpeedRatio getX() {
        return this.x;
    }

    /**
     * Get velocity value for vertical axis
     * @return vertical axis speed
     */
    public SpeedRatio getY() {
        return this.y;
    }
}
