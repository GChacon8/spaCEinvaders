package cr.ac.tec.ce3104;

import cr.ac.tec.ce3104.comms.Client;
import cr.ac.tec.ce3104.comms.Command;
import cr.ac.tec.ce3104.comms.CommandBatch;
import cr.ac.tec.ce3104.gameobjects.GameObject;
import cr.ac.tec.ce3104.gameobjects.GameObjectObserver;
import cr.ac.tec.ce3104.gameobjects.PlayerAvatar;
import cr.ac.tec.ce3104.levels.Level;
import cr.ac.tec.ce3104.levels.Level1;

import java.util.HashMap;

public class Game implements GameObjectObserver {
    // Entities and game state
    private Level level = new Level1();
    private PlayerAvatar player;
    private HashMap<Integer, GameObject> gameObjects = new HashMap<>();

    // Statistics
    private Integer lives = 3;
    private Integer score = 000000;
    private Integer difficulty = 0;

    // Client status and command queue
    private Integer playerId;
    private HashMap<Integer, Client> clients = new HashMap<>(); // Observers
    private CommandBatch outputQueue = new CommandBatch();

    public Game(Client playerClient) {
        this.playerId = playerClient.getClientId();
        this.attachClient(playerClient);

        this.log("New game");
        // The game starts
        this.reset();
    }

    /**
     * Add a client as a game viewer
     * @param client client to add as viewer
     */
    public synchronized void attachClient(Client client) {
        Integer maxClients = this.clients.get(this.playerId) != null ? 3 : 2;
        if (this.clients.size() >= maxClients) {
            client.sendError("no more spectators are allowed for this game");
            return;
        }

        // The state of the other clients is synchronized so far
        this.commit();

        // The complete game state is built to dedicate it to the new client
        CommandBatch catchUp = new CommandBatch();
        catchUp.add(Command.cmdGameArea(this.level.getGameAreaSize()));

        for (GameObject object : this.gameObjects.values()) {
            catchUp.add(object.makePutCommand());
        }

        client.sendBatch(catchUp);
        this.clients.put(client.getClientId(), client);

        this.log("Client " + client + " has joined");
    }

    /**
     * Removes a client from the viewer list and disconnects them from the game
     * @param client client to remove from the game
     */
    public synchronized void detachClient(Client client) {
        this.clients.remove(client.getClientId());
        this.log("Client " + client + " has left");

        // Changes to the game occur when each client disconnects
        if (this.clients.isEmpty()) {
            this.log("No clients left; game finalized");
            Server.getInstance().removeGame(this.playerId);
        } else if (client.getClientId() == this.playerId) {
            this.log("Player client has left, freezing all entities...");

            // The game stops immediately if the player exits
            for (GameObject object : this.gameObjects.values()) {
                //object.freeze();
            }
        }
    }

    /**
     * Restart current level
     */
    private synchronized void reset() {
        this.log("Begin level reset");
        // `this.player == null` silences log messages (avoiding spam on reset)
        this.player = null;

        // Old objects from the past instance are removed
        for (GameObject object : this.gameObjects.values()) {
            this.outputQueue.add(object.makeDeleteCommand());
        }

        // it cleans
        this.gameObjects.clear();
        this.syncStats();
        this.commit();

        // start of level
        this.player = this.level.setup(this, this.score);
    }

    /**
     * Add commands to the waiting list to synchronize the player status on the clients according to the status on the server
     */
    private void syncStats() {
        this.outputQueue.add(Command.cmdStats(this.lives, this.score));
    }

    /**
     * Send the stored commands on the waiting list
     */
    private void commit() {
        for (Client client : this.clients.values()) {
            client.sendBatch(this.outputQueue);
        }
        this.outputQueue.clear();
    }

    @Override
    public void onObjectDeleted(GameObject object) {

    }

    @Override
    public void onObjectModeChanged(GameObject object) {

    }

    /**
     * Add a message to print in the administrator console
     * @param message message to print in the administrator console
     */
    @Override
    public void log(String message) {
        System.out.println("[GAME " + this.playerId + "] " + message);
    }

    /**
     * @return player client id
     */
    public Integer getPlayerId() {
        return this.playerId;
    }
}
