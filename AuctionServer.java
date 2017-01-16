/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kuan
 */


import java.rmi.Naming;	//Import naming classes to bind to rmiregistry
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;


public class AuctionServer {
	static int port = 1099;

   public AuctionServer() {

     try {

			AuctionInterface am = new AuctionImple();


			Naming.rebind("rmi://localhost/AuctionManagerService", am);
                        
			am.restoreData();
			System.out.println("\nWelcome to the DAS4 Auction Server!\n");
                        System.out.println("Loading and saving of states are handled automatically."
                                + "\nKuan Jia Qing (2228126k)\n");

     } 
     catch (Exception e) {
       System.out.println("Server Error: " + e);
     }
   }

   public static void main(String args[]) {
	if (args.length == 1)
		port = Integer.parseInt(args[0]);
	
	new AuctionServer();
   }
}
