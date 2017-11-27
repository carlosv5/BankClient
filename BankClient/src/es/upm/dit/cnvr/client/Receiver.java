package es.upm.dit.cnvr.client;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;

import es.upm.dit.cnvr.model.BankClient;
import es.upm.dit.cnvr.model.Transaction;
import es.upm.dit.cnvr.model.ServiceStatus;

public class Receiver extends Thread{

    private Socket clientSocket;
    private boolean finished = false;
    private boolean error    = false;
    private int nValue       = 0;
    private Transaction transaction;
    
    public Receiver (Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void finish (boolean finished, boolean error, int nValue) {
        this.finished = finished;
        this.nValue   = nValue;
        this.error    = error;
    }

    public void run() {

        ObjectInputStream inFromServer;
        int nReceiver    = 0;

        while(!error && !(finished && nValue  <= nReceiver)) {
            try {
                // 1. Create the Stream for the input
                inFromServer =new ObjectInputStream(clientSocket.getInputStream());
                String message;
                // 2. Read from the connection
                transaction = (Transaction) inFromServer.readObject();
                BankClient bc = transaction.getBankClient();
                ServiceStatus status = transaction.getStatus();
                System.out.println("<< Connection: Account recieved.");
                System.out.println("<< The status of the operation is: " + status);
                if (bc == null) break;
                
		        switch (transaction.getOperation().toString()) {
	            case "CREATE_CLIENT":
	            	if(status == ServiceStatus.OK)
	                    System.out.println("<< Created client: " + transaction.getBankClient().getClientName());
	                break;
	            case "READ_CLIENT":	
	            	if(status == ServiceStatus.OK)
	                    System.out.println("<< Read client: " + transaction.getBankClient().getClientName());
	                break;

	            case "UPDATE_CLIENT":
	            	if(status == ServiceStatus.OK)
	                    System.out.println("<< Updated client: " + transaction.getBankClient().getClientName());
	                break;

	            case "DELETE_CLIENT":
	            	if(status == ServiceStatus.OK)
	                    System.out.println("<< Deleted client: " + transaction.getBankClient().getClientName());
	                break;

	            default:
                    System.out.println("<< There is an error state, consult with the admin");
	   }
                System.out.println("<< ID: " + transaction.getBankClient().getAccount());
                System.out.println("<< Balance " + transaction.getBankClient().getBalance());
                nReceiver ++;
            } catch (Exception e) {
                break;
            }
        }
        try {
            System.out.println("Socket closed.");
            clientSocket.close();
        } catch (Exception e) { // IOException
            System.out.println("Unexpected exception " + e);
        }
    }
}
