/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kuan
 */
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuctionClient {

    private static final String commandList = "help - show commands\n"
            + "new - Create a new auction item\n"
            + "listings - List all the auctions\n"
            + "bid - Place a bid for an item\n"
            + "details - Get full description of an auction\n"
            + "serverLoad - Get server load information\n"
            + "quit - Quit";
    private static Timer timer;
    private static int counter = 0;
    private static AuctionInterface am;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err
                    .println("Missing one argument, please enter your name as the first argument.");
            System.exit(0);
        }

        AuctionUser me;

        try {

            String name = args[0];

            me = new AuctionUserImple(name);

            am = (AuctionInterface) Naming.lookup("rmi://localhost/AuctionManagerService");

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Scanner scanner = new Scanner(System.in);

        System.out
                .println("\nWelcome to the DAS4 Auctioning System Client\n");
        System.out.println("Type help to list the avaliable commands.\n");
        System.out.println("This program was developed by Kuan Jia Qing (2228126k) for DAS4 2016");

        try {
            System.out.println("\n You are " + me.getName() + " with ID " + me.getId());
        } catch (RemoteException ex) {
            Logger.getLogger(AuctionClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        checkConnection();

        while (true) {
            System.out.print(">> ");

            String command = null;

            if (!scanner.hasNextLine()) {
                scanner.close();
                return;
            }

            command = scanner.nextLine();
            try {
                if (command.startsWith("new")) {
                    try {
                        System.out
                                .print("Enter the title of the auction item\n>>> ");
                        String title = scanner.nextLine();
                        System.out
                                .print("Enter the description of the auction item\n>>> ");
                        String description = scanner.nextLine();

                        System.out
                                .print("Enter the starting bid of the auction item\n>>> ");
                        String minValueString = scanner.nextLine();
                        Scanner valueScanner = new Scanner(minValueString);
                        double minValue = valueScanner.nextDouble();
                        valueScanner.close();

                        System.out
                                .print("Enter the closing time of the auction in the format YYYY-MM-DD HH:mm \n Example: (2016-11-19 21:39)\n>>> ");
                        String closingTimeString = scanner.nextLine();
                        long auctionId = am.createAuction(title, description, minValue,
                                closingTimeString, me);
                        if (auctionId == -1) {
                            System.out.println("Auction was not created!");
                        } else {
                            System.out.println("Auction created with ID = " + auctionId);
                        }
                    } catch (InputMismatchException e) {
                        System.err.println("Wrong input!");
                    }
                } else if (command.startsWith("help")) {
                    System.out.println(commandList);
                } else if (command.startsWith("listings")) {
                    String auctionList = am.getList();
                    System.out.println(auctionList);
                } else if (command.startsWith("bid")) {
                    try {
                        System.out
                                .print("Please enter the Auction ID.\n>>> ");
                        String idString = scanner.nextLine();
                        Scanner idScanner = new Scanner(idString);
                        long id = idScanner.nextLong();
                        idScanner.close();

                        System.out
                                .print("Please enter the total amount which you wish to bid\n>>> ");
                        String minValueString = scanner.nextLine();
                        Scanner valueScanner = new Scanner(minValueString);
                        double amount = valueScanner.nextDouble();
                        valueScanner.close();

                        am.placeBid(me, id, amount);
                    } catch (InputMismatchException e) {
                        System.err.println("Error. Bad input.");
                    }
                } else if (command.startsWith("details")) {
                    try {
                        System.out
                                .print("Please insert the id of the auction.\n>>> ");
                        String idString = scanner.nextLine();
                        Scanner idScanner = new Scanner(idString);
                        long id = idScanner.nextLong();
                        idScanner.close();

                        String auctionDetails = am.getDescription(id);
                        if (auctionDetails == null) {
                            System.out.println("Error. No such auction exists");
                        } else {
                            System.out.println(auctionDetails);
                        }
                    } catch (InputMismatchException e) {
                        System.err.println("Error. Bad input");
                    }
                } else if (command.startsWith("quit")) {
                    System.out.println("Goodbye");
                    scanner.close();
                    System.exit(0);
                } else if (command.startsWith("serverload")) {
                    try {
                        am.ignore(42);	// one call to prime system
                        long startTime = System.currentTimeMillis();
                        for (int i = 0; i < 10000; i++) {
                            am.ignore(42);
                        }
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        System.out.format("10000 calls in %d ms - %d.%03d ms/call\n",
                                elapsedTime, elapsedTime / 10000, (elapsedTime % 10000) / 10);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                } else {
                    System.err
                            .println("Wrong command! Type help for list of avaliable commands.");
                }
            } catch (Exception e) {
                System.err.println("Unknown Exception while performing operation");
                e.printStackTrace();

            }
        }

    }

    public static void checkConnection() {
        timer = new Timer();
        timer.schedule(new ping(), 5 * 1000);

    }

    static class ping extends TimerTask {

        public void run() {
            try {
                am = (AuctionInterface) Naming.lookup("rmi://localhost/AuctionManagerService");
                if (counter > 0) {
                    System.out.println("You have successfully reconnected after " + counter + " tries. Type a command now.\n>>>");
                    counter = 0;
                }
                timer.schedule(new ping(), 5 * 1000);
            } catch (Exception e) {
                System.out.println("Ping failed! Server is down. Attemping to reconnect every 5 seconds.");
                counter++;
                timer.schedule(new ping(), 5 * 1000);
            }
        }
    }

}
