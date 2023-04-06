package cr.ac.tec.ce3104.physics;

// Numerator indicating possible directions on a vertical axis
public enum VerticalDirection {
    UP,
    DOWN;

    public VerticalDirection invert() {
        switch (this) {
            case UP -> {
                return VerticalDirection.DOWN;
            }
            case DOWN -> {
                return VerticalDirection.UP;
            }
            default -> {
                assert false;
                return null;
            }
        }
    }
}