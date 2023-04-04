package cr.ac.tec.ce3104;

import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.InputStreamReader;

import java.awt.Font;
import java.awt.Color;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

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
                    System.out.println("list-games: Show running games");
                }
                case "clear" -> this.consoleOutput.setText("");
                case "list-games" -> {
                    System.out.println("No games are running");
                }
                default -> System.err.println("Error: unknown command '" + command[0] + "'. Type 'help' for more information.");
            }
        } catch (Exception exception) {
            System.err.println("Error: bad usage. Type 'help' for more information.");
        }
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