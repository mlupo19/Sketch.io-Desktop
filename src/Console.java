import java.util.Scanner;

public class Console implements Runnable {
    @Override
    public void run() {

        try (Scanner scanner = new Scanner(System.in)) {
            String[] command;
            boolean running = true;
            while (running) {
                command = scanner.nextLine().toLowerCase().split(" ");
                if (command.length == 0)
                    continue;

                if (command[0].equals("exit"))
                    running = false;
                if (command[0].equals("threads"))
                    Thread.currentThread().getThreadGroup().list();
                if (command[0].equals("clients")) {
                    int total = 0;
                    for (SketchGame sg : Server.getGames()) {
                        System.out.println("SketchGame-" + sg.getID());
                        System.out.println(sg.getPlayers() + "\n");
                        total += sg.getNumPlayers();
                    }
                    System.out.println("Total number of players: " + total);
                }
                if (command.length >= 3 && command[0].equals("close") && command[1].equals("game"))
                    try {
                        Server.removeGame(Integer.parseInt(command[2])).close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                if (command.length >= 2 && command[0].equals("kick")) {
                    for (int i = 1; i < command.length; i++) {
                        try {
                            Client c = Client.getClientByID(Integer.parseInt(command[i]));
                            Server.getGameByID(c.getCurrentGameId()).removePlayer(c);
                            c.close();
                        } catch (NumberFormatException ignored){}
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Server.stop();
        }
    }
}
