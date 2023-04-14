package cr.ac.tec.ce3104.physics;

// position-size box that delimits an entity
public class Bounds {
    private final Position origin;
    private final Size size;

    /**
     * Create a collision box based on a size
     * @param origin collision box origin position
     * @param size dimensions of the object whose collision box you want to create
     */
    public Bounds(Position origin, Size size) {
        this.origin = origin;
        this.size = size;
    }

    /**
     * Gets the origin coordinate of the collision box
     * @return origin coordinate of the collision box
     */
    public Position getOrigin() {
        return this.origin;
    }

    /**
     * Gets the dimensions of the collision box
     * @return collision box dimensions
     */
    public Size getSize() {
        return this.size;
    }

    /**
     * Given another collision box, check if with respect to it, the collision box is to its left
     * @param reference collision box with respect to which you want to know if the current collision box is to its left
     * @return true if the current collision box is to the left of the given object, false otherwise
     */
    public Boolean leftOf(Bounds reference) {
        return this.origin.getX() + this.size.getWidth() <= reference.origin.getX();
    }

    /**
     * Given another collision box, check if with respect to it, the collision box is to its right
     * @param reference collision box with respect to which you want to know if the current collision box is to its right
     * @return true if the current collision box is to the right of the given object, false otherwise
     */
    public Boolean rightOf(Bounds reference) {
        return this.origin.getX() >= reference.origin.getX() + reference.size.getWidth();
    }

    /**
     * Given another collision box, check if with respect to it, the collision box is above it
     * @param reference collision box with respect to which you want to know if the current collision box is on top
     * @return true if the current collision box is on top of the given object, false otherwise
     */
    public Boolean aboveOf(Bounds reference) {
        return this.origin.getY() + this.size.getHeight() <= reference.origin.getY();
    }

    /**
     * Given another collision box, check if with respect to it, the collision box is below
     * @param reference collision box with respect to which you want to know if the current collision box is below
     * @return true if the current collision box is below the given object, false otherwise
     */
    public Boolean belowOf(Bounds reference) {
        return this.origin.getY() >= reference.origin.getY() + reference.size.getHeight();
    }

    /**
     * Given another collision box, indicates if there is an overlap with the current collision box
     * @param other reference collision box
     * @return true if there is a collision between boxes, false otherwise
     */
    public Boolean collidesWith(Bounds other) {
        return !this.leftOf(other) && !this.rightOf(other) && !this.aboveOf(other) && !this.belowOf(other);
    }
}
