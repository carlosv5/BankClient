package es.upm.dit.cnvr.server;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Random;

import org.apache.zookeeper.ZooKeeper;

import es.upm.dit.cnvr.model.BankClient;
import es.upm.dit.cnvr.model.ClientDB;
import es.upm.dit.cnvr.model.OperationEnum;
import es.upm.dit.cnvr.model.ServiceStatus;
import es.upm.dit.cnvr.model.Transaction;
import es.upm.dit.cnvr.model.ServiceStatus;

public class ConnectionDispatcher extends Thread {

	private Transaction transaction;
    private Socket connection;
    private ObjectInputStream inFromClient;
    private DataOutputStream outToClient;
    private ClientDB db;
    private int id;
    private ZookeeperObject zkobject;
	private Operate operate;
	private ZooKeeper zk = ZookeeperObject.getZk();
	private static String rootBarrierOperation = "/boperation";

    /**
     * Constructor
     * @param id The identifier of the connection
     * @param connection The connection handle for sending a message to the client
     * @param db The database of clients
     * @param zkobject The object that administer zookeeper
     * @param operate The object that create the znodes of the operations
     */
    public ConnectionDispatcher(Socket connection, int id, ClientDB db, ZookeeperObject zkobject, Operate operate) {
        this.connection = connection;
        this.id         = id;
        this.db			= db;
        this.zkobject	= zkobject;
        this.operate	= operate;
    }

    public void run () {

        Handler handler;
        int sequence = 0;
        ServiceStatus status = null;

        try {
            System.out.println("ConnHandler " + id + ": socket waiting messages.");
            inFromClient = new ObjectInputStream(connection.getInputStream());

            while (true) {
            	if (zk.getChildren(rootBarrierOperation, false).size() > 0){

            		synchronized(ZookeeperObject.getMutexOperate()){
    					System.out.println("He hecho wait con el mutexOperate (en ConnectionDispatcher.java): " + System.identityHashCode(ZookeeperObject.getMutexOperate()));
    					ZookeeperObject.getMutexOperate().wait();
            			
            		}
            	}

                transaction = (Transaction) inFromClient.readObject();
                BankClient bc = transaction.getBankClient();                
                //TODO: Deberiamos meter comprobaciones para que si la operacion no la realiza bien que no cree los nodos de operaciones
                if (transaction.getOperation().equals(OperationEnum.CREATE_CLIENT)){
                	bc.setAccount(generateId(2));
                	status = db.createClient(bc);
                	transaction.setBankClient(bc);
                	transaction.setStatus(status);
                	if (status.equals(ServiceStatus.OK)){
                		operate.operation(transaction);
                    	operate.setPersonalCounter(operate.getPersonalCounter()+1); 	
                	}
                }
                
                if (transaction.getOperation().equals(OperationEnum.DELETE_CLIENT)){
                	status = db.deleteClient(bc.getAccount(),bc.getClientName());
                	transaction.setStatus(status);
                	if (status.equals(ServiceStatus.OK)){
                		operate.operation(transaction);
                    	operate.setPersonalCounter(operate.getPersonalCounter()+1); 	
                	}  	
                }
                
                //TODO: Quitar en ProcessOperation, no es necesario
                if (transaction.getOperation().equals(OperationEnum.READ_CLIENT)){
                	if (bc.getAccount() != "0") {
                		bc = db.readAccount(bc.getAccount());
                		transaction.setBankClient(bc);
                		transaction.setStatus(ServiceStatus.OK);
                	}
                	status = ServiceStatus.OK;
                }
                
                if (transaction.getOperation().equals(OperationEnum.UPDATE_CLIENT)){
                	status = db.update(bc.getAccount(), bc.getBalance());
                	transaction.setStatus(status);
                	if (status.equals(ServiceStatus.OK)){
                		operate.operation(transaction);
                    	operate.setPersonalCounter(operate.getPersonalCounter()+1);
                	}
                }
                
                if(es.upm.dit.cnvr.client.ClientApp.debug){
                    	System.out.println("Debug operation");
                    	System.out.println("Connection Dispatcher");
                    	System.out.println("Operation: " + transaction.getOperation());
                    	System.out.println("Account ID: " + transaction.getBankClient().getAccount());
                    	System.out.println("Client Name: " + transaction.getBankClient().getClientName());
                    	System.out.println("Balance: " + transaction.getBankClient().getBalance());

                    }
                
                handler = new Handler(id, sequence, transaction, connection);
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
    //TODO Change in the method put of HashMap -> <String, BankClient>
    private static String generateId(int bloqDigits){
    	Random random = new Random();
    	StringBuilder accountId = new StringBuilder();
    	accountId.append("EB32-");
    	for(int i=0;i<bloqDigits-1;i++){
	    	for(int j=0; j<4;j++){
	    		accountId.append(Integer.toHexString(random.nextInt(16)));
	    	}
	    if(i!= bloqDigits -2)
	    	accountId.append("-");
    	}
    	return accountId.toString().toUpperCase();
    }  

}
