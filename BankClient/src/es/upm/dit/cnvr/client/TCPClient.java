package es.upm.dit.cnvr.client;


import java.io.*;
import java.net.*;
import java.util.Random;

import es.upm.dit.cnvr.model.BankClient;
import es.upm.dit.cnvr.model.Transaction;


public class TCPClient {

    public static void connection(Transaction transaction) { //throws Exception{

    	OperationEnum operation = transaction.getOperation();
    	BankClient bc = transaction.getBankClient();
        Socket clientSocket;
        ObjectOutputStream outToServer;
        int nTimes      = 4;
        String hostname = "127.0.0.1";
        int port        = 6789;
        Random random = new java.util.Random();
        int bound       = 10;

        try {
            //1. creating a socket to connect to the server
            clientSocket = new Socket(hostname, port);
            //2. Create a stream for output information to the connection
            outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // 3. Create a thread for handling the inputs from the connection
        Receiver receiver = new Receiver(clientSocket);
        receiver.start();

        try {
        	if(ClientApp.debug){
            	System.out.println("Debug operation");
                System.out.println("Transaction: the information fo the account to send is ");
            	System.out.println("Operation: " + operation);
            	System.out.println("Account ID: " + bc.getAccount());
            	System.out.println("Client Name: " + bc.getClientName());
            	System.out.println("Balance: " + bc.getBalance());        	
            	}
             // 4. Write information to the connection
              outToServer.writeObject(transaction);
              outToServer.flush();

                Thread.sleep(random.nextInt(bound) * 1000);
        } catch (Exception e) { // IOException
            receiver.finish(false, true, 0);
            e.printStackTrace();
        }

        receiver.finish(true, false, nTimes);

    }
}


