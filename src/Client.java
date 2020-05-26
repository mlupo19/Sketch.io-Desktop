import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends Thread {

    private final int id;
    private String name;
    private DataInputStream dis;
    private DataOutputStream dos;
    private final Socket socket;

    private static final ArrayList<Integer> ids = new ArrayList<>();
    private boolean running = false;

    private String message = null;

    @Override
    public void run() {
        try {
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.name = dis.readUTF();
            dos.writeUTF("Welcome " + name);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = true;
        while (running) {
            try {
                message = dis.readUTF();
            } catch (EOFException eofe) {
                System.out.println("Client " + id + " disconnected");
                close();
                break;
            } catch (IOException e) {
                System.out.println("Error - Disconnecting client " + id);
                e.printStackTrace();
                close();
                break;
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

    public String get() {
        if (message != null) {
            String out = message;
            message = null;
            return out;
        }
        return null;
    }

    public void send(String message) {
        try {
            dos.writeUTF(message);
            dos.flush();
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
