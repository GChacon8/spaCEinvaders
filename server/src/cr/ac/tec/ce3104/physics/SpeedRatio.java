package cr.ac.tec.ce3104.physics;

// One-dimensional velocity expressed as distance/time fraction
public class SpeedRatio {
    private Integer numerator = 0;
    private Integer denominator = 0;

    /**
     * constructor used for zero velocity representations
     */
    private SpeedRatio() {}

    /**
     * Create a velocity representation for a stationary object
     * @return speed for stationary object
     */
    public static SpeedRatio stationary() {
        return new SpeedRatio();
    }

    /**
     * Creates a speed represented as a ratio between an amount of movement (displacement), and an amount of time
     * @param numerator amount of movement
     * @param denominator amount of time
     */
    public SpeedRatio(Integer numerator, Integer denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    /**
     * @return amount of momentum associated with velocity
     */
    public Integer getNumerator() {
        return this.numerator;
    }

    /**
     * @return amount of time characteristic of the velocity
     */
    public Integer getDenominator() {
        return this.denominator;
    }

    /**
     * Reverses the direction of movement associated with speed
     * @return Reversed current speed representation
     */
    public SpeedRatio negate() {
        return new SpeedRatio(-this.numerator, this.denominator);
    }
}
