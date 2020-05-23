import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Server {

    private static ServerSocket serverSocket;

    private static ArrayList<SketchGame> games = new ArrayList<>();

    private static boolean isRunning = false;

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
        while (isRunning()) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("New client: " + socket.getInetAddress());
                boolean gameFound = false;
                for (SketchGame sg : games) {
                    if (sg.getNumPlayers() < 6) {
                        sg.addPlayer(socket);
                        gameFound = true;
                    }
                }
                if (!gameFound)  {
                    SketchGame newGame = new SketchGame();
                    newGame.start();
                    newGame.addPlayer(socket);
                    games.add(newGame);
                }
            } catch(SocketException ignored) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Closing...");
        for (SketchGame sg: games) {
            sg.close();
        }
    }

    private static boolean isRunning() {
        return isRunning;
    }


    public static void stop() {
        isRunning = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<SketchGame> getGames() {
        return games;
    }
}
