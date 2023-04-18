package cr.ac.tec.ce3104;

import cr.ac.tec.ce3104.comms.Client;
import cr.ac.tec.ce3104.comms.Command;
import cr.ac.tec.ce3104.comms.CommandBatch;
import cr.ac.tec.ce3104.gameobjects.*;
import cr.ac.tec.ce3104.levels.Level;
import cr.ac.tec.ce3104.levels.Level1;
import cr.ac.tec.ce3104.modes.ControllableMode;
import cr.ac.tec.ce3104.physics.Dynamics;
import cr.ac.tec.ce3104.physics.Orientation;
import cr.ac.tec.ce3104.physics.Placement;
import cr.ac.tec.ce3104.physics.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import static java.lang.Thread.sleep;

public class Game implements GameObjectObserver {
    // Entities and game state
    private Level level = new Level1();
    private Player player;
    private HashMap<Integer, GameObject> gameObjects = new HashMap<>();

    // Statistics
    private Integer lives = 3;
    private Integer score = 000000;
    private Integer difficulty = 0;

    // Client status and command queue
    private Integer playerId;
    private HashMap<Integer, Client> clients = new HashMap<>(); // Observers
    private CommandBatch outputQueue = new CommandBatch();
    public Thread enemiesShooting;


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
            this.log("No clients left. Game finalized");
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
     * Indicates how a key press should be administered
     * @param key key pressed
     */
    public void onPress(Key key) {
        // Keys are ignored during Dying (the client is unaware of this)
        if (this.player.hasLost()) {
            return;
        }

        ControllableMode mode = (ControllableMode)this.player.getMode();
        switch (key) {
            case LEFT -> mode.onMoveLeft(this.player);
            case RIGHT -> mode.onMoveRight(this.player);
            case SHOOT -> mode.onShoot(this.player);
        }
    }

    /**
     * Indicates what to do once a fabric has been released
     */
    public void onRelease() {
        if (!this.player.hasLost()) {
            ((ControllableMode)this.player.getMode()).onRelease(this.player);
        }
    }

    /**
     * Indicates whether an object would collide if it were in a given position
     * @param object object whose collision status you want to check
     * @param position hypothetical position
     * @return A `Placement` object that describes collision aspects
     */
    public synchronized Placement testCollisions(GameObject object, Position position) {
        return new Placement(object, position, this.level, this.gameObjects.values(), false);
    }
    public Boolean isValidPosition(Position position){
        return position.getX() >= 0 && position.getX() <= 240 && position.getY() >= 25 && position.getY() <= 175;
    }
    /**
     * Indicates the steps to carry out once a movement action by the player has been detected
     * @param objectId entity identifier that was moved
     * @param position position of the entity to manage
     */
    public synchronized void onMove(Integer objectId, Position position) {
        GameObject object = this.gameObjects.get(objectId);
        if (object == null) {
            // The entity was probably recently deleted
            return;
        }

        // Crash and collision cases are tested
        Placement placement = new Placement(object, position, this.level, this.gameObjects.values());

        Orientation hitOrientation = placement.getHitOrientation();
        if (hitOrientation != null) {
            // The speculative movement of clients is counteracted
            object.getMode().onHit(object, hitOrientation);
        } else {
            object.relocate(position);
        }

        GameObject target = placement.getInteractionTarget();
        if(target != null && target.getDynamics() == Dynamics.RIGID){
            object.delete();
            target.delete();
        }

        if(target != null && target.getDynamics() == Dynamics.INTERACTIVE && object instanceof PlayerShot){
            object.delete();

            if (target instanceof Enemy) {
                player.updateScore(+((Enemy)target).getScore());
                target.delete();
            } else if (target instanceof EnemyShot) {
                target.delete();
            }
        }

        if(target instanceof Player && object instanceof EnemyShot){
            object.delete();
            player.updateLives();

            if(player.getLives() == 0){
                target.delete();

                this.score = 0;
                this.lives = 3;
                this.enemiesShooting = null;

                this.log("You lost");
                this.log("Begin level reset");
                this.player = null;

                // Old objects from the past instance are removed
                for (GameObject obj : this.gameObjects.values()) {
                    this.outputQueue.add(obj.makeDeleteCommand());
                }

                // it cleans
                this.gameObjects.clear();
                this.syncStats();
                this.commit();

                // start of level
                this.player = this.level.setup(this, this.score, this.lives);
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
        this.player = this.level.setup(this, this.score, this.lives);
    }

    /**
     * Add commands to the waiting list to synchronize the player status on the clients according to the status on the server
     */
    private void syncStats() {
        this.outputQueue.add(Command.cmdStats(this.lives, this.score));
    }

    /**
     * Updates the values related to the game state
     */
    private void updateStats() {
        // Score and lives are updated if there was a change
        if (this.score != this.player.getScore() || this.lives != this.player.getLives()) {
            this.score = this.player.getScore();
            this.lives = this.player.getLives();
            this.syncStats();
        }
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

    // Called when deleting an entity
    @Override
    public synchronized void onObjectDeleted(GameObject object) {
        this.outputQueue.add(object.makeDeleteCommand());
        this.gameObjects.remove(object.getId());

        this.commit();
    }

    // Called when changing the mode of an entity
    @Override
    public synchronized void onObjectModeChanged(GameObject object) {
        if (object == this.player) {
            // The player could have died or changed their stats
            this.updateStats();
        }

        this.outputQueue.add(object.makePutCommand());
        this.commit();
    }

    /**
     * Add a message to print in the administrator console
     * @param message message to print in the administrator console
     */
    @Override
    public void log(String message) {
        System.out.println("[GAME-" + this.playerId + "] " + message);
    }

    /**
     * @return player client id
     */
    public Integer getPlayerId() {
        return this.playerId;
    }

    /**
     * Gets the difficulty of the current state of the game
     * @return game difficulty level
     */
    public Integer getDifficulty() {
        return this.difficulty;
    }

    /**
     * Gets the hash map of the active entities in the game
     * @return game entity hashmap
     */
    public HashMap<Integer, GameObject> getGameObjects() {
        return this.gameObjects;
    }

    /**
     * Adds an entity to the game scenario of the match
     */
    public synchronized <T extends GameObject> T spawn(T object) {
        this.gameObjects.put(object.getId(), object);
        this.onObjectModeChanged(object);

        object.addObserver(this);

        // Avoid spam during reset()
        if (this.player != null) {
            this.log("New object " + object);
        }

        return object;
    }

    /**
     * Adds a list of entities to the game stage
     * @param objects array of entities to add
     */
    public synchronized void spawn(GameObject[] objects) {
        // It is not the same as doing it from outside, note that synchronized is preserved
        for (GameObject object : objects) {
            this.spawn(object);
        }
    }

    /**
     * Makes enemies hostile, so they start shooting.
     * @throws Exception exception in case of malfunctioning thread.
     */
    public synchronized void enemiesStartShooting() throws Exception {
        // Lambda for threading.
        Runnable shooting = () -> {
            while(true){
                try{
                    HashSet<Integer> enemyObjectsIDs = new HashSet<>();
                    HashMap<Integer, GameObject> objects = this.gameObjects;

                    // Detects every enemy on the GameObjects HashMap.
                    for(GameObject object : objects.values()){
                        if(object instanceof Octopus || object instanceof Squid || object instanceof Crab){
                            enemyObjectsIDs.add(object.getId());
                        }
                    }

                    // In case there is no enemies generated on the HashMap.
                    if(enemyObjectsIDs.size() > 0){
                        // Selects a random enemy to shot.
                        Integer[] enemyObjectsIDsArray = enemyObjectsIDs.toArray(new Integer[enemyObjectsIDs.size()]);

                        Random rand = new Random();
                        Integer randomPos = 0;

                        randomPos = rand.nextInt(enemyObjectsIDsArray.length);

                        Enemy enemyShooting = (Enemy) objects.get(enemyObjectsIDsArray[randomPos]);

                        // Creates a custom position to spawn the shot.
                        Position customPos = enemyShooting.getPosition();
                        customPos.setX(customPos.getX() + 2);
                        customPos.setY(customPos.getY() + 2);

                        // Spawns the shot.
                        enemyShooting.createShot(customPos);

                        // Delay for the next shot (Difficulty in order of the amount of enemies, the most enemies, the game get harder).
                        if(enemyObjectsIDsArray.length <= 9){
                            sleep(2500);
                        }else if(enemyObjectsIDsArray.length <= 18){
                            sleep(2000);
                        }else{
                            sleep(1500);
                        }
                    }else{
                        sleep(1000);
                    }
                }catch (InterruptedException e){
                    throw new RuntimeException(e);
                }
            }
        };

        // To avoid multi threading.
        if(enemiesShooting == null){
            enemiesShooting = new Thread(shooting);
        }
        enemiesShooting.start();
    }
}
