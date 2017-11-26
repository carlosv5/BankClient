package es.upm.dit.cnvr.client;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;

import es.upm.dit.cnvr.client.BankClient;

public class ConnectionDispatcher extends Thread {

	private Transaction transaction;
    private Socket connection;
    private ObjectInputStream inFromClient;
    private DataOutputStream outToClient;
    private int id;

    /**
     * Constructor
     * @param id The identifier of the connection
     * @param connection The connection handle for sending a message to the client
     */
    public ConnectionDispatcher(Socket connection, int id) {
        this.connection = connection;
        this.id         = id;
    }

    public void run () {

        Handler handler;
        int sequence = 0;

        try {
            System.out.println("ConnHandler " + id + ": socket waiting messages.");
            inFromClient = new ObjectInputStream(connection.getInputStream());

            while (true) {
                transaction = (Transaction) inFromClient.readObject();
                BankClient bc = transaction.getBankClient();
                if (bc == null) break;

                handler = new Handler(id, sequence, bc, connection);
                handler.start();
                sequence ++;
            }
        }
        catch (NullPointerException e) {
            System.out.println("Exception. Connection " + id + " Connection closed");
        }
        catch (Exception e) {
            System.out.println(e);
        }

        try {
            //id ++;
            System.out.println("Comnection " + id + ": connection closed");
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
