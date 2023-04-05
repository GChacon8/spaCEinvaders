package cr.ac.tec.ce3104.physics;

// Enumerator to indicate if an object moves to the left or to the right
public enum HorizontalDirection {
    LEFT,
    RIGHT;

    public HorizontalDirection invert() {
        switch (this) {
            case LEFT -> {
                return HorizontalDirection.RIGHT;
            }
            case RIGHT -> {
                return HorizontalDirection.LEFT;
            }
            default -> {
                assert false;
                return null;
            }
        }
    }
}
