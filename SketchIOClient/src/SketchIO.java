import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class SketchIO {

    private static InetAddress serverAddress;
    private static int port;
    private static Socket socket;
    private static ObjectInputStream ois;
    private static ObjectOutputStream oos;
    private static final Object writeLock = new Object();
    private static volatile String message = "";
    private static Thread readThread, writeThread, consoleThread;
    private static boolean status = false;
    private volatile static boolean guessedWord = false;
    private static String name;

    public static void start(InetAddress serverAddress, int port, String name) {
        if (status)
            return;
        status = true;
        System.out.println("Starting client...");
        SketchIO.name= name;
        SketchIO.serverAddress = serverAddress;
        SketchIO.port = port;

        try {
            socket = new Socket(serverAddress, port);
            System.out.println("Connected!");
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();

        }
        readThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Message message = (Message) ois.readObject();
                    processMessage(message);
                } catch (SocketException e) {
                    stop();
                    return;
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    stop();
                    return;
                }
            }
        }, "ReadThread");

        writeThread = new Thread(() -> {
            try {
                oos.writeUTF(name);
                oos.flush();
            } catch (SocketException e) {
                stop();
                return;
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                stop();
                return;
            }
            while (!Thread.interrupted()) {
                synchronized (writeLock) {
                    try {
                        writeLock.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                try {
                    oos.writeObject(new Message(name, null, message, null));
                } catch (SocketException e) {
                    stop();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    stop();
                    return;
                }
            }
        }, "WriteThread");
        consoleThread = Thread.currentThread();
        readThread.start();
        writeThread.start();

        String command = "";
        Scanner scan = new Scanner(System.in);
        while (!(command = scan.nextLine().toLowerCase()).equals("exit") && !Thread.interrupted()) {
            message = command;
            synchronized (writeLock) {
                writeLock.notifyAll();
            }
        }
        scan.close();
        stop();
    }

    private static void processMessage(Message message) {
        if (message.getCommand() != null) {
            switch (message.getCommand()) {
                case "new round":
                    guessedWord = false;
                    if (message.getName() != null) {
                        System.out.println(message.getName() + " is the new artist.\n" + message.getMessage());
                    } else {
                        System.out.println("You have been chosen as the next artist.\nYour word is \"" + message.getMessage() + "\".");
                    }
                    break;
                case "guessed word":
                    guessedWord = true;
                    System.out.println("Correct!");
                    break;
                case "person joined":

            }
        } else {
            System.out.println(message.getName() + ": " + message.getMessage());
        }
    }


    public static void stop() {
        if (!status)
            return;
        status = false;
        System.out.println("Exiting...");
        readThread.interrupt();
        writeThread.interrupt();
        consoleThread.interrupt();

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

}
