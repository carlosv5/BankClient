package es.upm.dit.cnvr.server;

import java.util.Random;

import es.upm.dit.cnvr.model.BankClient;
import es.upm.dit.cnvr.model.Transaction;

import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Handler extends Thread{

    // The id of the server
    private int id;
    // The order of the message received from the client
    private int sequence;
    private Transaction transaction;
    private Socket connection;

    /** Constructor
     * @param id Identifier of the associated connection dispatcher
     * @param sequence Sequence of the received message from the client
     * @param bc The message content
     * @param connection The connection for sending the processed information
     *                   to the client
     */
    public Handler(int id, int sequence, Transaction transaction, Socket connection) {
        this.id         = id;
        this.sequence   = sequence;
        this.transaction    = transaction;
        this.connection = connection;
    }

    public void run() {
        Random random = new Random();
        int bound = 10;
        ObjectOutputStream outToClient;


        // Simulate processing time for handling the message
        try {
            System.out.println(">>> Connection: " + id + " Sequence: " + sequence);
            Thread.sleep(random.nextInt(bound) * 1000);

            outToClient = new ObjectOutputStream(connection.getOutputStream());
            outToClient.writeObject(transaction);
            outToClient.flush();

            //counter.Add()
            System.out.println("<< Connection: Cuenta bancaria recibida de " + transaction.getBankClient().getClientName());
        } catch (InterruptedException e) {

        } catch (java.io.IOException e) {
            System.out.println("!!! Exception. Socket " + id + " Connection closed");
        } catch (Exception e) {
            System.out.println("!!! Unexpected exception");
        }
        //try {
        //    connection.close();
        //} catch (Exception e) {
        //    return;
        //}

    }

}
