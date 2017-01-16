/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kuan
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionImple extends UnicastRemoteObject implements AuctionInterface {

    private Long nextAuctionID; // the next auction id to be assigned
    private ConcurrentHashMap<Long, Auction> auctionHM; // Map holding auctions with their ids

    protected AuctionImple() throws RemoteException {
        super();
        auctionHM = new ConcurrentHashMap<Long, Auction>();
        nextAuctionID = 0L;
    }

    @Override
    public long createAuction(String itemTitle, String itemDescription,
            double itemMinimumValue, String closingTimeString,
            AuctionUser owner) throws RemoteException {
        long id;
        synchronized (nextAuctionID) {
            id = nextAuctionID;
            nextAuctionID++;
        }
        System.out.println("Creating auction no. " + id);

        Date closingTime = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            closingTime = sdf.parse(closingTimeString);
        } catch (ParseException e) {
            owner.sendMessage("Closing date entered wrongly. Please follow the format.");
            return -1;
        }

        if (closingTime == null) {
            return -1;
        }

        Auction auction = new Auction(itemTitle, itemDescription,
                itemMinimumValue, closingTime, id, owner);
        System.out.println("Auction created: " + auction.getSmallDetails());

        auctionHM.put(id, auction);
        saveData();

        return id;
    }

    @Override
    public String getDescription(long auctionId) throws RemoteException {
        Auction a = auctionHM.get(auctionId);
        if (a == null) {
            return null;
        }
        return a.getFullDescription();
    }

    @Override
    public String getList() throws RemoteException {

        String s = "ID | Title | Price | Closing Time | Status\n";
        s += "-------------------------------------\n";

        Calendar cal = new GregorianCalendar();
        for (Entry<Long, Auction> e : auctionHM.entrySet()) {
            Long key = e.getKey();
            Auction a = e.getValue();

//REMOVE OLD AUCTIONS
            cal.setTime(a.getClosingDateTime());
            cal.add(GregorianCalendar.MINUTE, 2);
            if ((new GregorianCalendar().after(cal))) {
                System.out.println("Removing auction " + key);
                auctionHM.remove(key);
                continue;
            }

            s += a.getSmallDetails() + "\n";
        }
        saveData();
        return s;
    }

    @Override
    public boolean placeBid(AuctionUser bidder, long auctionId, double bid) throws RemoteException {
        Auction a = auctionHM.get(auctionId);
        if (a == null) {
            bidder.sendMessage("Wrong auction ID!");
            return false;
        }
        a.bid(bidder, bid);
        saveData();
        return true;
    }

    @Override
    public synchronized boolean saveData() throws RemoteException {

        try {
            ObjectOutputStream objectOut = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("state.bin")));

            objectOut.writeObject(auctionHM);
            objectOut.writeObject(nextAuctionID);
            objectOut.close();
            System.out.println("State saved to state.bin");

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
//	@Override
//        public void ping(AuctionUser user) throws RemoteException{
//        user.sendMessage("OK");
//        }
//        

    @Override
    public boolean restoreData() throws RemoteException {

        try {

            ObjectInputStream objectIn = new ObjectInputStream(new BufferedInputStream(new FileInputStream("state.bin")));
            try {
                auctionHM = (ConcurrentHashMap<Long, Auction>) objectIn.readObject();

            } catch (Throwable e) {
                e.printStackTrace();
            }
            nextAuctionID = (Long) objectIn.readObject();
        } catch (FileNotFoundException e) {

            System.out.println("It seems that the save file doesn't exist. Assuming fresh start of system.");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        for (Auction a : auctionHM.values()) {
            a.setUpClosing();
        }

        System.out.println("State restored from state.bin");

        return true;
    }

    public void ignore(int localParam) throws RemoteException {
    }

}
