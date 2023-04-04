package cr.ac.tec.ce3104.comms;

import cr.ac.tec.ce3104.resources.Sequence;
import cr.ac.tec.ce3104.physics.Size;
import cr.ac.tec.ce3104.physics.Speed;
import cr.ac.tec.ce3104.physics.Position;

import org.json.simple.JSONValue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Command {
    // Internal JSON object
    private JSONObject json;

    /**
     * Initialize a new command with no content
     */
    public Command() {
        this.json = new JSONObject();
    }

    /**
     * Initialize a new command from a string that is formatted as a json object
     * @param source string containing json object to represent
     */
    public Command(String source) {
        this.json = (JSONObject)JSONValue.parse(source);
    }

    /**
     * Creates a json object that contains information about an error and returns a command built from that json
     * @param message error message to enter as value
     * @return error command created
     */
    public static Command cmdError(String message) {
        return new Command().putString("error", message);
    }

    /**
     * Create the initial communication command with a client
     * @param clientId id of the client to which the command is sent
     * @param gameIds ids of active games on the current server
     * @return command built from the given information
     */
    public static Command cmdStart(Integer clientId, List<Integer> gameIds) {
        return new Command().putInt("whoami", clientId).putList("games", gameIds);
    }

    /**
     * Constructs a command that indicates the screen parameters that a client must use
     * @param size screen dimensions
     * @return command built from the given information
     */
    public static Command cmdGameArea(Size size) {
        return new Command().putInt("width", size.getWidth())
                .putInt("height", size.getHeight());
    }

    /**
     * Create a command with the information of a game match
     * @param lives lives of the game player
     * @param score player of the match score
     * @return command created to be sent to the client
     */
    public static Command cmdStats(Integer lives, Integer score) {
        return new Command().putString("op", "stats")
                .putInt("lives", lives)
                .putInt("score", score);
    }

    /**
     * Create a command to tell the client to draw a new game entity
     * @param id identifier of the entity to create
     * @param position position in which you want to create the entity
     * @param z drawing depth of the entity to create
     * @param speed indicates the horizontal and vertical speeds of the created object
     * @param sequence indicates the series of sprites that correspond to the animation of an object
     * @return command created from the given information
     */
    public static Command cmdPut(Integer id, Position position, Integer z, Speed speed, Sequence sequence) {
        List<Integer> sequenceIds = new ArrayList<>();
        /*for (Sprite sprite : sequence.getSprites()) {
            sequenceIds.add(sprite.getId());
        }*/

        return new Command().putString("op", "put")
                .putInt("id", id)
                .putInt("x", position.getX())
                .putInt("y", position.getY())
                .putInt("z", z)
                .putInt("num_x", speed.getX().getNumerator())
                .putInt("num_y", speed.getY().getNumerator())
                .putInt("denom_x", speed.getX().getDenominator())
                .putInt("denom_y", speed.getY().getDenominator())
                .putList("seq", sequenceIds);
    }

    /**
     * Create a command indicating that an entity should be deleted
     * @param id id of the entity to be deleted
     * @return command created to be sent to the client
     */
    public static Command cmdDelete(Integer id) {
        return new Command().putString("op", "delete")
                .putInt("id", id);
    }

    /**
     * Puts a key-value pair into the current JSON object. The value is an Integer
     * @param key string key to add to json object
     * @param value integer value to insert to json object
     * @return updated current command
     */
    public Command putInt(String key, Integer value) {
        return this.put(key, value);
    }

    /**
     * Puts a key-value pair into the current JSON object. The value is a string
     * @param key key of the string value to insert
     * @param value string value to insert to json object
     * @return updated current command
     */
    public Command putString(String key, String value) {
        return this.put(key, value);
    }

    /**
     * Create a field that stores a key-list in the json object of the current command
     * @param key key to identify list
     * @param list list to be inserted as JSON array
     * @return updated current command
     */
    @SuppressWarnings("unchecked")
    public Command putList(String key, List<Integer> list) {
        JSONArray array = new JSONArray();
        array.addAll(list);
        return this.put(key, array);
    }

    /**
     * Add a key-value pair to the json object of the command
     * @param key value key to insert
     * @param value value to insert
     * @return updated current command
     */
    @SuppressWarnings("unchecked")
    private Command put(String key, Object value) {
        this.json.put(key, value);
        return this;
    }

    /**
     * Extract an integer value from a json object
     * @param key key that identifies the field that contains the integer to extract
     * @return extracted integer
     */
    public Integer expectInt(String key) {
        Long value = (Long)this.json.get(key);
        return value != null ? value.intValue() : null;
    }

    /**
     * Extract a string value from a json object
     * @param key key that identifies the field that contains the string to extract
     * @return extracted string
     */
    public String expectString(String key) {
        return (String)this.json.get(key);
    }

    /**
     * @return convert a textual representation
     */
    @Override
    public String toString() {
        return this.json.toJSONString();
    }
}
