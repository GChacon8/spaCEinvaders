package cr.ac.tec.ce3104;

import cr.ac.tec.ce3104.gameobjects.*;
import cr.ac.tec.ce3104.physics.HorizontalDirection;
import cr.ac.tec.ce3104.physics.Position;

import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.InputStreamReader;

import java.awt.Font;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import static java.lang.Thread.sleep;

// Management console
public class AdminWindow {

    // Original and piped stdout
    private final PrintStream realStdout;
    private final BufferedReader stdoutSink;

    // Graphic elements
    private JFrame frame;
    private JTextArea consoleOutput;
    private JTextField inputLine;
    private Thread sinkThread;

    /**
     * Initialize a new instance of the game manager console
     * @param realStdout real standard output stream
     * @throws IOException exceptions associated with IO operations on streams
     */
    public AdminWindow(PrintStream realStdout) throws IOException {
        PipedOutputStream stdoutPipe = new PipedOutputStream();
        this.stdoutSink = new BufferedReader(new InputStreamReader(new PipedInputStream(stdoutPipe)));
        this.realStdout = realStdout;

        // Stdout is replaced
        PrintStream fakeStdout = new PrintStream(stdoutPipe, true);
        System.setOut(fakeStdout);
        System.setErr(fakeStdout);

        // Window starts
        SwingUtilities.invokeLater(this::start);
    }

    /**
     * Launches the game manager window and configures it
     */
    private void start() {
        // Graphic elements are initialized
        this.frame = new JFrame("SpaCEInvaders. server");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.getContentPane().setLayout(new BoxLayout(this.frame.getContentPane(), BoxLayout.PAGE_AXIS));

        Font font = new Font("monospaced", Font.PLAIN, 16);

        this.consoleOutput = new JTextArea(22, 100);
        this.consoleOutput.setFont(font);
        this.consoleOutput.setEditable(false);
        this.consoleOutput.setBackground(Color.BLACK);
        this.consoleOutput.setForeground(Color.WHITE);
        this.frame.getContentPane().add(new JScrollPane(this.consoleOutput));

        ((DefaultCaret)this.consoleOutput.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        this.frame.getContentPane().add(Box.createHorizontalGlue());

        this.inputLine = new JTextField();
        this.inputLine.setFont(font);
        this.inputLine.addActionListener(event -> this.onCommand());
        this.frame.getContentPane().add(inputLine);

        this.frame.pack();
        this.frame.setVisible(true);

        // Starts the thread that reads the console contents
        this.sinkThread = new Thread(this::readSink);
        this.sinkThread.start();
    }

    /**
     * Indicates how the commands enabled for the game administrator user should be handled
     */
    private void onCommand() {
        String line = this.inputLine.getText().trim();
        this.inputLine.setText("");

        if (line.isEmpty()) {
            return;
        }

        String[] command = line.split(" +");
        System.out.println("> " + line);

        // The command line is parsed
        try {
            switch (command[0]) {
                case "help" -> {
                    System.out.println("=== Available commands ===");
                    System.out.println("help: Show command help");
                    System.out.println("clear: Clears the command line");
                    System.out.println("game-list: Show running games");
                    System.out.println("object-list <game>: Show all entities in a running game");
                    System.out.println("add-enemy <game> <x> <y> [squid|crab|octopus]: Add a enemy");
                    System.out.println("add-enemy-line <game> <y> [squid|crab|octopus]: Add a enemy line");
                    System.out.println("add-saucer <game> <score>: Add a flying saucer");
                }
                case "clear" -> this.consoleOutput.setText("");
                case "game-list" -> {
                    Boolean atLeastOne = false;
                    for (Integer id : Server.getInstance().getGameIds()) {
                        atLeastOne = true;

                        Game game = Server.getInstance().getGame(id);
                        System.out.println("Game " + id + " started by client " + game.getPlayerId());
                    }

                    if (!atLeastOne) {
                        System.out.println("No games are running");
                    }
                }
                case "object-list" -> {
                    Game game = expectGame(command, 1);
                    for (GameObject object : game.getGameObjects().values()) {
                        System.out.println(object);
                    }
                    System.out.println("Total: " + game.getGameObjects().size());
                }
                case "add-enemy" -> {
                    Game game = expectGame(command, 1);
                    Integer posX = expectInteger(command, 2);
                    Integer posY = expectInteger(command, 3);

                    EnemyType type = switch (expectArgument(command, 4)) {
                        case "squid" -> EnemyType.SQUID;
                        case "crab" -> EnemyType.CRAB;
                        case "octopus" -> EnemyType.OCTOPUS;
                        default -> throw new Exception();
                    };

                    Position position = new Position(posX, posY);
                    if(game.isValidPosition(position)) {
                        Enemy enemy = game.spawn(new EnemyFactory().createEnemy(type, -6, position, false, game));
                        System.out.println("Created enemy " + enemy);
                    }
                    else{
                        System.out.println("Can't generate object out of valid bounds (x=[0,240],y=[25,175])");
                    }

                    // Invokes function to enable enemies shooting ability.
                    game.enemiesStartShooting();
                }
                case "add-enemy-line" -> {
                    Game game = expectGame(command, 1);

                    Integer posX = 74;
                    Integer posY = expectInteger(command, 2);

                    EnemyType type = switch (expectArgument(command, 3)) {
                        case "squid" -> EnemyType.SQUID;
                        case "crab" -> EnemyType.CRAB;
                        case "octopus" -> EnemyType.OCTOPUS;
                        default -> throw new Exception();
                    };
                    Position position = new Position(posX, posY);
                    if(game.isValidPosition(position)) {
                        Integer i;
                        for (i = 0; i <= 8; i++) {
                            Enemy enemy = game.spawn(new EnemyFactory().createEnemy(type, -6, position, true, game));
                            System.out.println("Created enemy " + enemy);
                            posX += 20;
                            position.setX(posX);
                        }
                    }
                    else{
                        System.out.println("Can't generate object out of valid bounds (y=[25,175])");
                    }

                    // Invokes function to enable enemies shooting ability.
                    game.enemiesStartShooting();
                }
                case "add-saucer" -> {
                    Game game = expectGame(command, 1);
                    Integer score = expectInteger(command, 2);

                    Saucer saucer;
                    Random random = new Random();
                    int randomNumber = random.nextInt(2);
                    if (randomNumber == 0) {
                        saucer = new Saucer(0, HorizontalDirection.LEFT, new Position(256, 50), score, game);
                        game.spawn(saucer);
                    } else{
                        saucer = new Saucer(0, HorizontalDirection.RIGHT, new Position(0, 50), score, game);
                        game.spawn(saucer);
                    }
                    System.out.println("Created flying saucer " + saucer);
                }
                default -> System.err.println("Error: unknown command '" + command[0] + "'. Type 'help' for more information.");
            }
        } catch (Exception exception) {
            System.err.println("Error: bad usage. Type 'help' for more information." + exception.getMessage());
        }
    }

    /**
     * Gets the game reference of a command entered the admin console
     * @param command array of strings that make up the command read from the administration console
     * @param index position of the argument to extract in the command line
     * @return game reference
     * @throws Exception error that arises when there is an invalid entry by the administrator
     */
    private static Game expectGame(String[] command, Integer index) throws Exception {
        Integer id = expectInteger(command, index);
        Game game = Server.getInstance().getGame(id);
        if (game == null) {
            System.err.println("Error: no game has ID " + id);
            throw new Exception();
        }
        return game;
    }

    /**
     * Extract an integer value from a command entered from the administration console
     * @param command array of strings that make up the command read from the administration console
     * @param index position of the argument to extract in the command line
     * @return Integer given in command
     * @throws Exception error that arises when there is an invalid entry by the administrator
     */
    private static Integer expectInteger(String[] command, Integer index) throws Exception {
        try {
            return Integer.parseInt(expectArgument(command, index));
        } catch (NumberFormatException exception) {
            throw new Exception();
        }
    }

    /**
     * Extract an integer value from a command entered from the administration console
     * @param command array of strings that make up the command read from the administration console
     * @param index position of the argument to extract in the command line
     * @return extracted string value
     * @throws Exception error that arises when there is an invalid entry by the administrator
     */
    private static String expectArgument(String[] command, Integer index) throws Exception {
        if (index >= command.length) {
            throw new Exception();
        }
        return command[index];
    }

    /**
     * Attempts to read information from the standard output stream
     */
    private void readSink() {
        try {
            try {
                while (true) {
                    String line;
                    try {
                        line = this.stdoutSink.readLine();
                    } catch (IOException exception) {
                        // This happens when a client's thread ends
                        continue;
                    }

                    // This should never happen
                    if (line == null) {
                        break;
                    }

                    // It shows both in real stdout and console
                    this.realStdout.println(line);
                    SwingUtilities.invokeLater(() -> this.consoleOutput.append(line + "\n"));
                }
            } finally {
                // Restore stdout/stderr in case of a problem
                System.setOut(this.realStdout);
                System.setErr(this.realStdout);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}