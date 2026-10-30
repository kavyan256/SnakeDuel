import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Host:  java Main
 * Join:  java Main <host-ip>
 *
 * The host runs the game and sends the state text to the other laptop every frame.
 * The other laptop only draws that text and sends back its key presses.
 */
public class Main {
    private static final int PORT = 5555, FRAME_MS = 60;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) host();
        else join(args[0]);
    }

    private static void host() throws Exception {
        Board board = new Board("Snake Duel - You are BLUE");
        try (ServerSocket server = new ServerSocket(PORT)) {
            board.show("Waiting for player 2...|Tell them to run: java Main " + localIp() + "|");
            try (Socket socket = server.accept()) {
                socket.setTcpNoDelay(true);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = reader(socket);

                Game game = new Game();
                board.onKey = key -> game.handle(0, key);
                Thread keys = new Thread(() -> readKeys(in, game));
                keys.setDaemon(true);
                keys.start();

                while (!out.checkError()) { // becomes true when player 2 leaves
                    game.tick();
                    String state = game.state();
                    out.println(state);
                    board.show(state);
                    Thread.sleep(FRAME_MS);
                }
            }
        }
        board.show("|Player 2 disconnected|");
    }

    private static void join(String hostIp) throws Exception {
        Board board = new Board("Snake Duel - You are ORANGE");
        try (Socket socket = new Socket(hostIp, PORT)) {
            socket.setTcpNoDelay(true);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = reader(socket);

            board.onKey = out::println;
            String state;
            while ((state = in.readLine()) != null) board.show(state);
        }
        board.show("|Host disconnected|");
    }

    private static void readKeys(BufferedReader in, Game game) {
        try {
            String key;
            while ((key = in.readLine()) != null) game.handle(1, key);
        } catch (IOException e) {
            // player 2 left; the game loop notices and stops
        }
    }

    private static BufferedReader reader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /** Asks the OS which address it uses on the network. No data is actually sent. */
    private static String localIp() {
        try (DatagramSocket probe = new DatagramSocket()) {
            probe.connect(InetAddress.getByName("8.8.8.8"), 53);
            return probe.getLocalAddress().getHostAddress();
        } catch (IOException e) {
            return "<host-ip>";
        }
    }
}
