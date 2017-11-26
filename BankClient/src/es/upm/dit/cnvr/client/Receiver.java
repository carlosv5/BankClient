package es.upm.dit.cnvr.client;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;

import es.upm.dit.cnvr.model.BankClient;

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
                if (bc == null) break;
                System.out.println("<< Connection: Account recieved " + bc.getClientName());
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
