import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
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

    private static JFrame frame;
    private static JPanel panel;
    private static JTextPane chat;
    private static JTextField chatBox;
    private static JLabel wordLabel;

    public static void start(InetAddress serverAddress, int port, String name) {
        if (status)
            return;
        buildGUI();
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

    private static void printToChat(String message) {
        printToChat(message, Color.BLACK);
    }

    private static void printToChat(String message, Color color) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
        synchronized (chat) {
            try {
                chat.getStyledDocument().insertString(chat.getDocument().getLength(), message + "\n", aset);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private static void processMessage(Message message) {
        if (message.getCommand() != null) {
            switch (message.getCommand()) {
                case "new round":
                    guessedWord = false;
                    setWordLabel(message.getMessage());
                    if (message.getName() != null) {
                        printToChat(message.getName() + " is the new artist.");
                    } else {
                        printToChat("You have been chosen as the next artist.\nYour word is \"" + message.getMessage() + "\".");
                    }
                    break;
                case "guessed word":
                    guessedWord = true;
                    printToChat("Correct!", Color.GREEN);
                    break;
                case "player joined":
                    printToChat(message.getName() + " has joined the game.", Color.BLUE);
                    break;
            }
        } else {
            printToChat(message.getName() + ": " + message.getMessage());
        }
    }

    private static void setWordLabel(String newWord) {
        wordLabel.setText(newWord);
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

    private static void buildGUI() {
        frame = new JFrame("Sketch.IO");
        panel = new JPanel();
        panel.setBackground(Color.WHITE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screen.width * 2 / 3, screen.height * 2 / 3);
        frame.add(panel);
        chat = new JTextPane();
        chat.setEditable(false);
        chatBox = new JTextField();
        chatBox.setPreferredSize(new Dimension(frame.getWidth() / 3, frame.getHeight() / 3 - 10));
        chatBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    message = chatBox.getText();
                    synchronized (writeLock) {
                        writeLock.notifyAll();
                    }
                    chatBox.setText("");
                }
            }
        });

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(chat);
        scrollPane.setPreferredSize(new Dimension(frame.getWidth() / 3, frame.getHeight() * 2 / 3 - 10));
        chatPanel.add(scrollPane, BorderLayout.PAGE_START);
        chatPanel.add(chatBox, BorderLayout.PAGE_END);
        panel.setLayout(new BorderLayout());
        panel.add(chatPanel, BorderLayout.LINE_END);

        wordLabel = new JLabel("_");
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.TRACKING, 0.3);

        wordLabel.setFont(wordLabel.getFont().deriveFont(24.0f).deriveFont(attributes));
        wordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(wordLabel, BorderLayout.PAGE_START);

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                stop();
            }
        });
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
