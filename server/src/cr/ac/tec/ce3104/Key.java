package cr.ac.tec.ce3104;

// Enumerator to characterize keystrokes from the client
public enum Key {
    LEFT,
    RIGHT,
    SHOOT;

    /**
     * Parses a key from its textualization
     * @param source representative text
     * @return respective key
     */
    public static Key parse(String source) {
        return switch (source) {
            case "left" -> Key.LEFT;
            case "right" -> Key.RIGHT;
            case "shoot" -> Key.SHOOT;
            default -> null;
        };
    }
}
