package es.upm.dit.cnvr.server;

import java.net.ServerSocket;
import java.net.Socket;

import es.upm.dit.cnvr.model.ClientDB;


public class TCPServer {

    public static void main(String[] args) throws Exception {

        Socket connectionSocket;
        int id = 0;
        ClientDB db = new ClientDB();

        ServerSocket welcomeSocket = new ServerSocket(6789);

        while (true) {
            try {
                connectionSocket = welcomeSocket.accept();
                ConnectionDispatcher conHandler = new ConnectionDispatcher(connectionSocket, id, db);
                System.out.println("Get a socket connection");
                conHandler.start();
                id++;
            } catch (Exception e) {
                System.out.println("Closed the socket");
            }
        }
        //welcomeSocket.close();
    }

}
