import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientMain {

    public static void main(String[] args) throws UnknownHostException {
        String name = JOptionPane.showInputDialog("Enter your name: ");
        SketchIO.start(InetAddress.getByName("127.0.0.1"), 8080, name);
    }

}
