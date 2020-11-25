import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SketchGame {

    private static int gameID = 0;
    private final int thisGameID;

    private final ArrayList<Client> players = new ArrayList<>();

    private int round = 1;
    private volatile boolean inRound = false;
    private final ArrayList<Client> guessedWord = new ArrayList<>();
    private Client currentArtist;
    private String currentWord;
    private Thread helperThread;
    private final Thread mainThread;
    private final Runnable helperRunnable = () -> {
        newRound();
        System.out.println();
    };

    public void start() {
        mainThread.start();
    }

    private void newRound() {
        guessedWord.clear();
        currentArtist = getRandomPlayer();
        guessedWord.add(currentArtist);
        System.out.println("CurrentArtist: " + currentArtist);
        currentWord = "Banana";
        StringBuilder underlines = new StringBuilder();
        for (int i = 0; i < currentWord.length(); i++) {
            underlines.append("_");
        }
        currentArtist.send(new Message(null, null, currentWord, "new round"));
        sendMessageToPlayers(new Message(currentArtist.getName(), null, underlines.toString(), "new round"), currentArtist);
        inRound = true;
    }

    SketchGame() {
        Runnable runnable = () -> {
            try {
                while (!Thread.interrupted()) {
                    Message m;
                    synchronized (players) {
                        for (Client c : players) {
                            if (c == null || !c.isAlive()) {
                                players.remove(c);
                                continue;
                            }
                            m = c.get();
                            if (m != null) {
                                System.out.println(c.getName() + " (" + c.getId() + "): " + m.getMessage());
                                if (inRound) {
                                    if (!guessedWord.contains(c) && m.getMessage().toLowerCase().equals(currentWord.toLowerCase())) {
                                        guessedWord.add(c);
                                        c.send(new Message(null, null, currentWord, "guessed word"));
                                        sendMessageToPlayers(new Message(c.getName(), null, null, "guessed word"), c);
                                        if (guessedWord.containsAll(players)) {
                                            newRound();
                                        }
                                        continue;
                                    }

                                }
                                for (Client c1 : players) {
                                    if (!guessedWord.contains(c1) || c1.equals(currentArtist))
                                        c1.send(m);
                                }
                            }
                        }
                    }
                }
            } finally {
                helperThread.interrupt();
            }
        };
        mainThread = new Thread(runnable, "GameThread-" + (++gameID));
        thisGameID = gameID;
        helperThread = new Thread(helperRunnable, "HelperThread-" + thisGameID);
        System.out.println("Creating game " + thisGameID);
    }

    public void addPlayer(Socket socket) {
        Client c = new Client(socket, thisGameID);
        synchronized (players) {
            players.add(c);
            if (players.size() > 2) {
                helperThread.start();
            }
        }
    }

    public boolean removePlayer(Client c) {
        synchronized (players) {
            return players.remove(c);
        }
    }

    public int getNumPlayers() {
        synchronized (players) {
            return players.size();
        }
    }

    public void close() {
        synchronized (players) {
            for (Client c : players) {
                System.out.println("Disconnecting client " + c.getId());
                c.close();
            }
        }
        mainThread.interrupt();
    }

    public void sendMessageToPlayers(Message m, Client... exceptions) {
        List<Client> exc = Arrays.asList(exceptions);
        synchronized (players) {
            for (Client c : players) {
                if (!exc.contains(c) && c.isAlive())
                    c.send(m);
            }
        }
    }

    public Client getRandomPlayer() {
        synchronized (players) {
            return players.get(new Random().nextInt(players.size()));
        }
    }

    public ArrayList<Client> getPlayers() {
        synchronized (players) {
            return players;
        }
    }

    public int getID() {
        return thisGameID;
    }
}
