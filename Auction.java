/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kuan
 */
import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Auction implements Serializable {
    
    private AuctionUser owner;
    private AuctionUser clientWithHighestPrice;
    private String winnerClientName;
    private Map<String, AuctionUser> clientsHM;
    
    private String auctionName;
    private String auctionDetails;
    private long id;
    
    private boolean isStatusOpen;
    private double auctionMinValue;
    private Date closingDateTime;
    
    public Auction(String auctionName, String auctionDetails,
            double auctionMinValue, Date closingTime, long id,
            AuctionUser owner) {
        this.auctionName = auctionName;
        this.auctionDetails = auctionDetails;
        this.auctionMinValue = auctionMinValue;
        this.closingDateTime = closingTime;
        this.id = id;
        this.owner = owner;
        
        this.clientsHM = new HashMap<String, AuctionUser>();
        
        setUpClosing();
        
        if (!isStatusOpen) {
            try {
                owner.sendMessage("You entered an old date!");
            } catch (RemoteException ex) {
                Logger.getLogger(Auction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
     public synchronized void bid(AuctionUser client, double bid) throws RemoteException {
        
        if (!isStatusOpen) {
            client.sendMessage("Unable to bid. Auction is closed.");
            return;
        }
        clientsHM.put(client.getId(), client);
        
        if (bid <= auctionMinValue) {
            client.sendMessage("Bid not high enough. You need to beat the minimun price.");
            return;
        }
        
        clientWithHighestPrice = client;
        winnerClientName = client.getName();
        auctionMinValue = bid;
        client.sendMessage("Your bid was accepted.");
        
    }
    
 
    
    public synchronized String getSmallDetails() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String closingTimeString = sdf.format(closingDateTime);
//        return String.format("%-15d%-20s%-20.2f%-20s%-10s", id, auctionName,
//                auctionMinValue, closingTimeString, Boolean.toString(isStatusOpen));
        String open;
        if (isStatusOpen) {
            open = "Open";
        } else {
            open = "Close";
        }
        return id + " | " + auctionName + " | " + auctionMinValue + " | " + closingTimeString + " | " + open;
    }
    
    public synchronized String getFullDescription() {
        String description = "Auction ID: " + id + "\n"
                + "Auction Title: " + auctionName + "\n"
                + "Auction Description: " + auctionDetails + "\n"
                + "Current highest bid: " + auctionMinValue + "\n"
                + "Total bidders: " + clientsHM.size() + "\n";
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        description += String.format("Closing Time : %s\n\n", sdf.format(closingDateTime));
        
        description += "The auction is " + ((isStatusOpen) ? "open" : "closed") + ".\n";
        if (!isStatusOpen) {
            description += getResult();
        }
        
        return description;
        
    }
    
    public synchronized void setUpClosing() {
        if (closingDateTime.before(new Date())) {
            this.isStatusOpen = false;
        } else {
            this.isStatusOpen = true;
            (new Timer(true)).schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Timer started!");
                    try {
                        Auction.this.close();
                    } catch (RemoteException ex) {
                        Logger.getLogger(Auction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
            }, closingDateTime);
            System.out.println("Timer set!");
        }
        
    }
     private synchronized void close() throws RemoteException {
        System.out.println("Closing Auction " + id);
        
        isStatusOpen = false;
        
        if (clientWithHighestPrice != null) {
            
            System.out.println("Auction " + id + " is won by " + winnerClientName + " with highest bid of " + auctionMinValue);
            owner.sendMessage("Your Auction item " + auctionName + " has been successfully sold to " + winnerClientName + " for " + auctionMinValue);
            for (Entry<String, AuctionUser> e : clientsHM.entrySet()) {
                String id = e.getKey();
                AuctionUser client = e.getValue();
                if (client == clientWithHighestPrice) {
                    client.sendMessage("Great! You have won the auction item " + auctionName + " for " + auctionMinValue);
                } else {
                    client.sendMessage("You did not win the auction '" + auctionName + "', it was won by " + winnerClientName + " for " + auctionMinValue);
                }
            }
            
        } else {
            owner.sendMessage("Sorry, nobody bidded for your auction item '" + auctionName + "'");
            System.out.println("Nobody bidded for auction " + id);
            
            for (AuctionUser client : clientsHM.values()) {

//                client.sendMessage(String.format("%s auction closed and nobody met the starting value %.2f!",
//                        auctionName, auctionMinValue));
                client.sendMessage("This auction '" + auctionName + "' was closed and nobody could meet the starting price of " + auctionMinValue + ".");
            }
        }
        
    }
    private String getResult() {
        if (clientWithHighestPrice == null) {
            return "No bids.";
        }
        
        return String.format("Bidder " + winnerClientName + " has purchased this item for " + auctionMinValue);
        
    }
    
      public synchronized Date getClosingDateTime() {
        return closingDateTime;
    }
    
   
    
}
