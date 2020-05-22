import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends Thread {

    private int id;
    private String name;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket socket;

    private static final ArrayList<Integer> ids = new ArrayList<>();

    @Override
    public void run() {
        try {
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.name = dis.readUTF();
            int id = 0;
            while(!ids.contains(id)) {
                id = (int) (Math.random() * 100) + 50;
            }
            this.id = id;

            dos.writeUTF("Welcome Client " + id);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setName("ClientThread-" + id);
    }

    public void close() {
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
        start();
    }

    public String get() {
        try {
            return dis.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void send(String message) {
        try {
            dos.writeUTF(message);
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
