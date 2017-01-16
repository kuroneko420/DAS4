/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kuan
 */

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

public class AuctionUserImple extends UnicastRemoteObject implements AuctionUser  {


	private String name;
	private UUID uuid;

	public AuctionUserImple(String name) throws RemoteException {
		super();
		this.name = name;
		this.uuid = UUID.randomUUID();
	}





	public String getName(){
		return name;
	}

	
	public String getId() {
		return uuid.toString();
	}

	public void sendMessage(String message) {
		System.out.println("Message from Server - " + message);
	}
}
