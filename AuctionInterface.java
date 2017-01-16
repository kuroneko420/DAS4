/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kuan
 */
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuctionInterface extends Remote {

    public boolean placeBid(AuctionUser bidder, long auctionId,
            double bid) throws RemoteException;

    public String getDescription(long auctionId)
            throws RemoteException;

    public long createAuction(String itemTitle,
            String itemDescription,
            double itemMinimumValue,
            String closingTimeString,
            AuctionUser owner)
            throws RemoteException;

    public String getList()
            throws RemoteException;

    public boolean saveData()
            throws RemoteException;

    public boolean restoreData()
            throws RemoteException;

    public void ignore(int localParam) throws RemoteException;
}
