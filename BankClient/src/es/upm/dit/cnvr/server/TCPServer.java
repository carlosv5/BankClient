package es.upm.dit.cnvr.server;

import java.net.ServerSocket;
import java.net.Socket;

import es.upm.dit.cnvr.model.ClientDB;
import es.upm.dit.cnvr.server.Zookeeper;


public class TCPServer {

    public static void main(String[] args) throws Exception {

        Socket connectionSocket;
        int id = 0;
        ClientDB db = ClientDB.getInstance();
        Zookeeper zk = new Zookeeper();
        zk.configure();
        Operate operate = zk.getOperate();

        ServerSocket welcomeSocket = new ServerSocket(6789);
        

        while (true) {
            try {
                connectionSocket = welcomeSocket.accept();
                ConnectionDispatcher conHandler = new ConnectionDispatcher(connectionSocket, id, db, zk, operate);
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
