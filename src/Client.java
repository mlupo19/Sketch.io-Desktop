import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends Thread {

    private final int id;
    private String name;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private final Socket socket;

    private static final ArrayList<Integer> ids = new ArrayList<>();
    private boolean running = false;

    private Message message = null;

    @Override
    public void run() {
        try {
            this.ois = new ObjectInputStream(socket.getInputStream());
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.name = ois.readUTF();
            oos.writeUTF("Welcome " + name);
            System.out.println("Welcome " + name);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = true;
        while (running) {
            try {
                message = (Message) ois.readObject();
            } catch (EOFException eofe) {
                System.out.println("Client " + id + " disconnected");
                close();
                break;
            } catch (IOException e) {
                System.out.println("Error - Disconnecting client " + id);
                e.printStackTrace();
                close();
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }

    }



    public void close() {
        running = false;
        ids.remove(new Integer(id));
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    Client(Socket socket) {
        this.socket = socket;
        int id = (int) (Math.random() * 100) + 50;
        while(ids.contains(id)) {
            id = (int) (Math.random() * 100) + 50;
        }
        this.id = id;
        setName("ClientThread-" + id);
        start();
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
            e.printStackTrace();
        }
    }

    public int getClientId() {
        return id;
    }

    public String getClientName() {
        return name;
    }
}
