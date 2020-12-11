import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientMain {

    public static void main(String[] args) throws UnknownHostException {
        String name = JOptionPane.showInputDialog("Enter your name: ");
        SketchIO.start(InetAddress.getByName("24.189.158.0"), 6969, name);
    }

}
