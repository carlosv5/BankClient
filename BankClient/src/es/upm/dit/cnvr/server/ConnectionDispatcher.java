package es.upm.dit.cnvr.server;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
        		try {
        			String hostname = InetAddress.getLocalHost().getHostName();
                    transaction.setHostname(hostname);
        		} catch (UnknownHostException e1) {
        			// TODO Auto-generated catch block
        			e1.printStackTrace();
        		}
                BankClient bc = transaction.getBankClient();                
                if (transaction.getOperation().equals(OperationEnum.CREATE_CLIENT)){
                	bc.setAccount(generateId(5));
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
                
                if (transaction.getOperation().equals(OperationEnum.SHOW_ALL)){
                	
            		transaction.setClientdb(db);;
            		transaction.setStatus(ServiceStatus.OK);

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
            System.out.println("Connection " + id + ": connection closed");
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("FIN CONNECTION DISPATCHER");
    }
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
