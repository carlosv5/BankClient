package es.upm.dit.cnvr.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import es.upm.dit.cnvr.model.BankClient;
import es.upm.dit.cnvr.model.ClientDB;
import es.upm.dit.cnvr.model.OperationEnum;
import es.upm.dit.cnvr.model.Transaction;
import es.upm.dit.cnvr.server.ZookeeperObject;


public class TCPServerTest {

    public static void main(String[] args) throws Exception {

        Socket connectionSocket;
        int id = 0;
        ClientDB db = ClientDB.getInstance();
        ZookeeperObject zkobject = new ZookeeperObject();
        zkobject.configure();
        Operate operate = zkobject.getOperate();
        int loop = 100;
        Transaction transaction;
        
        for (int i=0; i<=loop; i++) {
            try {
            	Thread.sleep(750);
            	transaction = new Transaction(OperationEnum.CREATE_CLIENT, new BankClient("0", "User de prueba", i));
                ConnectionDispatcherTest conHandler = new ConnectionDispatcherTest(id, db, zkobject, operate, transaction);
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
