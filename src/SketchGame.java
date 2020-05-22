import java.net.Socket;
import java.util.ArrayList;

public class SketchGame extends Thread {

    private static int gameID = 0;

    private ArrayList<Client> players = new ArrayList<>();
    private int numPlayers = 0;

    private Client currentArtist;

    SketchGame() {
        super("GameThread-" + (++gameID));
    }

    @Override
    public void run() {

    }

    public void addPlayer(Socket socket) {
        Client c = new Client(socket);
        players.add(c);
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public void close() {
        for (Client c : players) {
            c.close();
        }
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
