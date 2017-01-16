
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ThreadLocalRandom;
/*
 * The sole purpose of this Java Class is to stress test the Auction Server
 * Start up the Auction Server and run this file for the multi threaded stress test.
 */
/**
 *
 * @author kuanjiaqing
 */
public class MultiThreadStressServerTester {


    static void threadMessage(String message) {
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    }
    private static AuctionInterface am;

    private static class CreateAuctionLoop implements Runnable {

        public void run() {
            Thread thread = Thread.currentThread();
            AuctionUser me;
            try {

                String name = thread.getName();

                me = new AuctionUserImple(name);

                am = (AuctionInterface) Naming.lookup("rmi://localhost/AuctionManagerService");

            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            try {
                for (int i = 0; i < 200; i++) {//How many auctions per thread
                    long auctionId = am.createAuction("testAuction - " + Thread.currentThread().getName(), "testDescription", 100,
                            "2016-12-28 04:00", me);

                    threadMessage("Created an auction " + auctionId);
                }
            } catch (RemoteException ex) {
                Logger.getLogger(MultiThreadStressServerTester.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

     private static class BidAuctionLoop implements Runnable {

        public void run() {
            Thread thread = Thread.currentThread();
            AuctionUser me;
            try {

                String name = thread.getName();

                me = new AuctionUserImple(name);

                am = (AuctionInterface) Naming.lookup("rmi://localhost/AuctionManagerService");

            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            try {
                for (int i = 0; i < 200; i++) {//How many bids per thread
                double randomNumber = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
       am.placeBid(me, i, randomNumber); //Place a bid on auction i with a randomNumber for the bid price

                    threadMessage("Placed a bid on auction " + i + " for " + "randomNumber dollars.");
                }
            } catch (RemoteException ex) {
                Logger.getLogger(MultiThreadStressServerTester.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void main(String args[]) throws InterruptedException {

        threadMessage("Starting Stress Test thread");

        long startTime = System.currentTimeMillis();

        Thread[] threads = new Thread[4];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new CreateAuctionLoop());
            threads[i].start();
        }

        

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

       for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new BidAuctionLoop());
            threads[i].start();
        }
       
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        
        System.out.println("The system took " + (System.currentTimeMillis() - startTime) + "ms to complete this action. \n");
        System.exit(0);
    }
}
