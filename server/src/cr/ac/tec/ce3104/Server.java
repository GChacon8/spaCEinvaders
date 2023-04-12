package cr.ac.tec.ce3104;

import cr.ac.tec.ce3104.comms.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server {
    // Singleton pattern
    private static Server instance;
    private static final Integer PORT = 8080;

    // Passive socket
    private ServerSocket serverSocket;

    // Active games
    private HashMap<Integer, Game> games = new HashMap<>();

    // Management console
    private AdminWindow adminWindow;

    /**
     * Gets the active instance of the server. If the server has not been initialized before, then initialize it
     * @return reference to a single instance of the server
     */
    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    /**
     * Private constructor since the class is a Singleton
     */
    private Server() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException exception) {
            exception.printStackTrace();

            System.out.println("Fatal error: unable to start server");
            System.exit(-1);
        }
    }

    /**
     * Execute an infinite loop in search of new clients
     */
    public void startUp() {
        try {
            this.adminWindow = new AdminWindow(System.out);
            System.out.println("[SERVER] Listening on 127.0.0.1:" + PORT + "...");

            // Main loop for listening to new clients
            while (true) {
                // Start a new connection with the client
                Client client = new Client(this.serverSocket.accept());
                System.out.println("[SERVER] Accepted connection from client " + client);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Initializes a new game with the given client as the player client
     * @param player client to be registered as the player client of the game to create
     * @return play with the given client as a player
     */
    public Game initPlayer(Client player) {
        if (this.games.size() >= 2) {
            player.sendError("the maximum number of active games has been reached");
            return null;
        }

        Game game = new Game(player);
        this.games.put(game.getPlayerId(), game);

        return game;
    }

    /**
     * @param gameId game player client id
     * @return game whose player has the given id
     */
    public Game getGame(Integer gameId) {
        return this.games.get(gameId);
    }

    /**
     * Delete a game from the server
     * @param gameId game id to be removed
     */
    public void removeGame(Integer gameId) {
        this.games.remove(gameId);
    }

    /**
     * Gets the list of ids that identify the active games
     * @return list of active game ids
     */
    public List<Integer> getGameIds() {
        return new ArrayList<>(this.games.keySet());
    }
}
