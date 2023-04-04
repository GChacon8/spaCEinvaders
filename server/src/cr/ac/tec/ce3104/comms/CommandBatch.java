package cr.ac.tec.ce3104.comms;

import java.util.ArrayList;
import java.util.List;

public class CommandBatch {
    // Command list
    private List<Command> batch = new ArrayList<>();

    // Cached text representation
    private String memoizedString = null;

    /**
     * Add a command to the batch collection
     * @param command command to add to batch
     */
    public void add(Command command) {
        this.batch.add(command);
        this.memoizedString = null;
    }

    /**
     * Clear the collection of created commands
     */
    public void clear() {
        this.batch.clear();
        this.memoizedString = null;
    }

    /**
     * Convert to string
     */
    @Override
    public String toString() {
        if (this.memoizedString == null) {
            this.memoizedString = "";
            for (Command command : this.batch) {
                this.memoizedString += command.toString() + "\n";
            }
        }
        return this.memoizedString;
    }
}