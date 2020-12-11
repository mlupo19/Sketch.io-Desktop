import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class Client {

    private final int id;
    private String name;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private final Socket socket;
    private int gameId;
    private SketchGame game;
    private final Thread clientThread;

    private static final Map<Integer, Client> ids = new HashMap<>();

    private Message message = null;

    private Runnable runnable;


    public void close() {
        ids.remove(id);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientThread.interrupt();
    }

    Client(Socket socket, SketchGame game) {
        this.game = game;
        this.socket = socket;
        this.gameId = game != null ? game.getID() : -1;
        int id = (int) (Math.random() * 100) + 50;
        while(ids.get(id) != null) {
            id = (int) (Math.random() * 100) + 50;
        }
        this.id = id;
        ids.put(id, this);
        runnable = () -> {
            try {
                this.ois = new ObjectInputStream(socket.getInputStream());
                this.oos = new ObjectOutputStream(socket.getOutputStream());
                this.name = ois.readUTF();
                System.out.println(name + " @ " + socket.getInetAddress().getHostName() + " joined");
                Server.getGameByID(this.gameId).sendMessageToPlayers(new Message(getName(), null, null, "player joined"));

                if (game != null) {
                    game.onPlayerAdded(this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.interrupted()) {
                try {
                    message = (Message) ois.readObject();
                } catch (SocketException e) {
                    break;
                } catch (EOFException eofe) {
                    System.out.println("Client " + this.id + " disconnected");
                    break;
                } catch (IOException e) {
                    System.out.println("Error - Disconnecting client " + this.id);
                    e.printStackTrace();
                    break;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            Server.getGameByID(gameId).removePlayer(this);
            close();
        };
        clientThread = new Thread(runnable, "ClientThread-" + id);
        clientThread.start();
    }

    public Message get() {
        if (message != null) {
            Message out = message;
            message = null;
            return out;
        }
        return null;
    }

    public void send(Message message) {
        try {
            oos.writeObject(message);
            oos.flush();
            oos.reset();
        } catch (IOException e) {
            System.err.println(name);
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCurrentGameId() {
        return gameId;
    }

    public SketchGame getCurrentGame() {
        return game;
    }

    public String toString() {
        return name + " (" + id + ")";
    }

    public boolean isAlive() {
        return clientThread.isAlive();
    }

    public boolean equals(Object o) {
        if (!(o instanceof Client))
            return false;
        return id == ((Client) o).id;
    }

    public static Client getClientByID(int id) {
        return ids.get(id);
    }

    public Socket getSocket() {
        return socket;
    }
}
