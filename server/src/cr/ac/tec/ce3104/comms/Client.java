package cr.ac.tec.ce3104.comms;

import cr.ac.tec.ce3104.Game;
import cr.ac.tec.ce3104.Key;
import cr.ac.tec.ce3104.Server;
import cr.ac.tec.ce3104.physics.Position;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

// Instances of this class manage a particular client
public class Client implements AutoCloseable {

    // Client ID Generator
    private static Integer nextClientId = 0;

    // Client status
    private Game game;
    private Socket socket;
    private Integer id;
    private ClientType type;
    private Key lastKey = null;

    // I/O resources for this client
    private Thread runnerThread;
    private BufferedReader requestReader;
    private PrintWriter commandSender;

    /**
     * Starts a new instance of the class to manage a connection to a client
     * @param socket client connection information
     * @throws IOException error that can occur if there are problems reading the socket stream
     */
    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.id = nextClientId++;

        this.requestReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.commandSender = new PrintWriter(this.socket.getOutputStream(), true);

        this.runnerThread = new Thread(this::run);
        this.runnerThread.start();
    }

    /**
     * Client thread startup routine
     */
    private void run() {
        // `try (this)` closes resources automatically on exit with or without exception
        try (this) {
            try {
                // First a `handshake` is performed as defined by the protocol
                if (this.doHandshake()) {
                    while (this.processNext()) {
                        continue;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                this.sendError(exception);
            }
        } catch (Exception nested) {
            nested.printStackTrace();
        }
    }

    /**
     * Processes the handshake message sent by the client
     * @return status of handshake processing with client
     * @throws IOException error that can occur if there are problems reading the socket stream
     */
    private Boolean doHandshake() throws IOException {
        List<Integer> gameIds = Server.getInstance().getGameIds();

        // First message
        this.sendSingle(Command.cmdStart(this.id, gameIds));
        // Response to init
        Integer gameId = this.receive().expectInt("init");
        if (gameId == null) {
            return false;
        }

        // Subscribe a player based on the id provided by init
        if (gameId == this.id) {
            this.type = ClientType.PLAYER;
            this.game = Server.getInstance().initPlayer(this);
        } else {
            this.type = ClientType.SPECTATOR;
            this.game = Server.getInstance().getGame(gameId);

            if (this.game == null) {
                this.sendError("invalid game ID");
            } else {
                this.game.attachClient(this);
            }
        }

        return this.game != null;
    }

    /**
     * Read a command from the socket stream
     * @return command built from socket stream
     * @throws IOException error that can occur if there are problems reading the socket stream
     */
    private Command receive() throws IOException {
        return new Command(this.requestReader.readLine());
    }

    /**
     * Processes the next pending command obtained from the client (handling user input)
     * @return boolean that indicates the state in which the processing ends
     * @throws IOException error that can occur if there are problems reading the socket stream
     */
    private Boolean processNext() throws IOException {
        // Start client loop
        Command request = this.receive();

        // Manage quit commands for both client types
        String operation = request.expectString("op");
        if (operation.equals("bye")) {
            return false;
        } else if (type == ClientType.SPECTATOR) {
            return true;
        }

        // Dispatch of commands sent by the client
        switch (operation) {
            case "press" -> {
                this.lastKey = Key.parse(request.expectString("key"));
                this.game.onPress(this.lastKey);
            }
            case "release" -> {
                if (this.lastKey == Key.parse(request.expectString("key"))) {
                    this.game.onRelease();
                    this.lastKey = null;
                }
            }
            case "move" -> {
                Position position = new Position(request.expectInt("x"), request.expectInt("y"));
                this.game.onMove(request.expectInt("id"), position);
            }
            default -> {
                this.sendError("invalid operation: " + operation);
                return false;
            }
        }

        return true;
    }

    /**
     * Send a single command to the client
     * @param command command to send to client
     */
    public void sendSingle(Command command) {
        try {
            this.commandSender.println(command);
            this.commandSender.flush();
        } catch (Exception exception) {
            this.sendError(exception);
        }
    }

    /**
     * Send a set of commands to the client
     * @param batch collection of commands to be sent to the client
     */
    public void sendBatch(CommandBatch batch) {
        try {
            this.commandSender.print(batch);
            this.commandSender.flush();
        } catch (Exception exception) {
            this.sendError(exception);
        }
    }

    /**
     * Informs the client that an error has been encountered during execution
     * @param message description of the error found
     */
    public void sendError(String message) {
        try {
            this.sendSingle(Command.cmdError(message));
            this.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            this.socket = null;
        }
    }

    /**
     * Send an exception found to stderr
     * @param exception exception to send to stdout error
     */
    public void sendError(Exception exception) {
        exception.printStackTrace();
        this.sendError(exception.toString());
    }

    /**
     * Gets the id of the client that manages this instance
     * @return id of the client that manages the current instance
     */
    public Integer getClientId() {
        return id;
    }

    /**
     * Provides a string representation of this client
     */
    @Override
    public String toString() {
        String string = "#" + this.id;
        if (this.socket != null) {
            string += " (" + this.socket.getRemoteSocketAddress() + ")";
        }
        return string;
    }

    /**
     * Closes and ends resources, terminating the connection
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        if (this.game == null) {
            System.out.println("[SERVER] Connection finalized with detached client " + this);
        }

        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
        }

        if (this.game != null) {
            this.game.detachClient(this);
            this.game = null;
        }
    }
}
