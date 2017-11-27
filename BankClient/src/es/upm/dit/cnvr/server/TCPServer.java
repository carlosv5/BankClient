package es.upm.dit.cnvr.server;

import java.net.ServerSocket;
import java.net.Socket;

import es.upm.dit.cnvr.model.ClientDB;


public class TCPServer {

    public static void main(String[] args) throws Exception {

        Socket connectionSocket;
        int id = 0;

        ServerSocket welcomeSocket = new ServerSocket(6789);

        while (true) {
            try {
                connectionSocket = welcomeSocket.accept();
                // TODO: NO CREAR CLIENTDB AQUI, inicializarlo antes y meterlo en la llamada.
                ConnectionDispatcher conHandler = new ConnectionDispatcher(connectionSocket, id, new ClientDB());
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
