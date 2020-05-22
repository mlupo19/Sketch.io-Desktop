import org.lwjgl.Sys;

import java.util.Scanner;

public class Console implements Runnable {
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        String command = "";
        boolean running = true;
        while (running) {
            command = scanner.nextLine();
            if (command.equals("exit"))
                running = false;
        }
        System.out.println("exiting...");
        Server.stop();
        scanner.close();
    }
}
