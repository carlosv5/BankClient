package es.upm.dit.cnvr.client;


import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import es.upm.dit.cnvr.model.BankClient;
import es.upm.dit.cnvr.model.OperationEnum;
import es.upm.dit.cnvr.model.Transaction;


public class TCPClient {

    public static void connection(Transaction transaction) { //throws Exception{

    	OperationEnum operation = transaction.getOperation();
    	BankClient bc = transaction.getBankClient();
        Socket clientSocket;
        ObjectOutputStream outToServer;
        int nTimes      = 4;
        ArrayList<String> hostnamelist = getIps();  
        Random random = new java.util.Random();
        String server = hostnamelist.get(random.nextInt(hostnamelist.size()));
        String hostname = server.split(":")[0];
        int port = Integer.parseInt(server.split(":")[1]);
        System.out.println("hostname:" +hostname + "; port: "+port);
        
        
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
    private static ArrayList<String> getIps(){
    	ArrayList<String> ips = new ArrayList<String>();
    	try {
			Files.lines(Paths.get("serverips.txt")).forEach(ips::add);
		} catch (IOException e) {
			ips.add("127.0.0.1");
			System.out.println("Ips not found. Default: 127.0.0.1");
			e.printStackTrace();
		}
    	return ips;
    }
}


