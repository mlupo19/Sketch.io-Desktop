import java.net.Socket;
import java.util.ArrayList;

public class SketchGame extends Thread {

    private static int gameID = 0;

    private final ArrayList<Client> players = new ArrayList<>();

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
        return players.size();
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

    public ArrayList<Client> getPlayers() {
        return players;
    }
}
