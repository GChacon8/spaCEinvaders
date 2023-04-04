package cr.ac.tec.ce3104.gameobjects;

import cr.ac.tec.ce3104.comms.Command;
import cr.ac.tec.ce3104.modes.Mode;
import cr.ac.tec.ce3104.physics.Position;

public abstract class GameObject {
    // Internal Entity Properties
    private Integer id;
    private Position position;
    private Mode mode;
    private GameObjectObserver observer = null;
    private static Integer nextId = 0;

    /**
     * Make the command to request the creation of the entity in the game clients
     * @return command to send to different clients
     */
    public Command makePutCommand() {
        return Command.cmdPut(this.id, this.position, this.getZ(), this.mode.getSpeed(), this.mode.getSequence());
    }

    /**
     * Make the command to request the removal of the current entity from the game clients
     * @return command to send to different clients
     */
    public Command makeDeleteCommand() {
        return Command.cmdDelete(this.id);
    }

    protected Integer getZ() {
        return 0;
    }
}
