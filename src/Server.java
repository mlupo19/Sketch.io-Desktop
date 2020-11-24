import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {

    private static ServerSocket serverSocket;

    private static final Map<Integer, SketchGame> games = new HashMap<>();

    private static volatile boolean isRunning = false;

    public static void main(String[] args) {
        Server.start();
    }

    private static void start() {
        System.out.println("Starting server...");
        // set up console interface
        Thread consoleThread = new Thread(new Console(), "ConsoleThread");
        consoleThread.start();

        // allow clients to join
        try {
            serverSocket = new ServerSocket(8080);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Cannot launch server!");
            return;
        }
        System.out.println("Waiting for clients...");
        isRunning = true;
        while (isRunning) {
            try {
                Socket socket = serverSocket.accept();
                boolean gameFound = false;
                for (SketchGame sg : games.values()) {
                    if (sg.getNumPlayers() < 6) {
                        sg.addPlayer(socket);
                        gameFound = true;
                    }
                }
                if (!gameFound)  {
                    SketchGame newGame = new SketchGame();
                    newGame.start();
                    newGame.addPlayer(socket);
                    games.put(newGame.getID(), newGame);
                }
            } catch(SocketException ignored) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Shutting down...");
        for (SketchGame sg: games.values()) {
            sg.close();
        }
    }

    public static SketchGame getGameByID(int id) {
        return games.get(id);
    }

    public static void stop() {
        isRunning = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<SketchGame> getGames() {
        return new ArrayList<>(games.values());
    }
}
