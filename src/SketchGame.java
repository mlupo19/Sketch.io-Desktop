import java.net.Socket;
import java.util.ArrayList;

public class SketchGame extends Thread {

    private static int gameID = 0;
    private int thisGameID;

    private final ArrayList<Client> players = new ArrayList<>();

    private Client currentArtist;
    private boolean running = false;
    private String currentWord;

    SketchGame() {
        super("GameThread-" + (++gameID));
        thisGameID = gameID;
        System.out.println("Creating game " + thisGameID);
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            Message m;
            synchronized (players) {
                for (Client c : players) {
                    if (c == null) {
                        players.remove(c);
                        continue;
                    }
                    m = c.get();
                    if (m != null) {
                        for (Client c1 : players) {
                            c1.send(m);
                            System.out.println(c.getClientName() + " (" + c.getClientId() + "): " + m);
                        }
                    }
                }
            }
        }

    }

    public void addPlayer(Socket socket) {
        Client c = new Client(socket);
        players.add(c);
    }

    public int getNumPlayers() {
        return players.size();
    }

    public void close() {
        running = false;
        for (Client c : players) {
            System.out.println("Disconnecting client " + c.getClientId());
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
