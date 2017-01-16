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
import java.util.UUID;

public interface AuctionUser extends Remote {
	


	public String getName() throws RemoteException;
	

	public String getId() throws RemoteException;

	public void sendMessage(String information) 
			throws RemoteException;
	
}
