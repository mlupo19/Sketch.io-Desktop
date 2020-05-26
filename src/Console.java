import java.util.Scanner;

public class Console implements Runnable {
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        String command;
        boolean running = true;
        while (running) {
            command = scanner.nextLine().toLowerCase();
            if (command.equals("exit"))
                running = false;
            if (command.equals("threads"))
                Thread.currentThread().getThreadGroup().list();
            if (command.equals("clients")) {
                for (SketchGame sg : Server.getGames()) {
                    System.out.println(sg.getName());
                    System.out.println(sg.getPlayers() + "\n");
                }
            }
        }
        System.out.println("exiting...");
        Server.stop();
        scanner.close();
    }
}
