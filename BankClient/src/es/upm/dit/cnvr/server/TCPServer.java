package es.upm.dit.cnvr.server;

import java.net.ServerSocket;
import java.net.Socket;

import es.upm.dit.cnvr.model.ClientDB;
import es.upm.dit.cnvr.server.ZookeeperObject;


public class TCPServer {

    public static void main(String[] args) throws Exception {

        Socket connectionSocket;
        int id = 0;
        ClientDB db = ClientDB.getInstance();
        ZookeeperObject zkobject = new ZookeeperObject();
        zkobject.configure();
        Operate operate = zkobject.getOperate();

        ServerSocket welcomeSocket = new ServerSocket(6788);
        

        while (true) {
            try {
                connectionSocket = welcomeSocket.accept();
                ConnectionDispatcher conHandler = new ConnectionDispatcher(connectionSocket, id, db, zkobject, operate);
                System.out.println("Get a socket connection");
                conHandler.start();
                System.out.println("Reach after start");
                id++;
            } catch (Exception e) {
                System.out.println("Closed the socket");
            }
        }
        //welcomeSocket.close();
    }

}
