package es.upm.dit.cnvr.server;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;

import es.upm.dit.cnvr.client.OperationEnum;
import es.upm.dit.cnvr.model.BankClient;
import es.upm.dit.cnvr.model.ClientDB;
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

    /**
     * Constructor
     * @param id The identifier of the connection
     * @param connection The connection handle for sending a message to the client
     */
    public ConnectionDispatcher(Socket connection, int id, ClientDB db) {
        this.connection = connection;
        this.id         = id;
        this.db			= db;
    }

    public void run () {

        Handler handler;
        int sequence = 0;
        ServiceStatus status = null;

        try {
            System.out.println("ConnHandler " + id + ": socket waiting messages.");
            inFromClient = new ObjectInputStream(connection.getInputStream());

            while (true) {
                transaction = (Transaction) inFromClient.readObject();
                BankClient bc = transaction.getBankClient();
                if (transaction.getOperation().equals(OperationEnum.CREATE_CLIENT)){
                	status = db.createClient(bc);
                	transaction.setStatus(status);
                }
                
                if (transaction.getOperation().equals(OperationEnum.DELETE_CLIENT)){
                	status = db.deleteClient(bc.getAccount());
                	transaction.setStatus(status);
                	// TODO: Implementar en ClientDB el ServiceStatus

                }
                if (transaction.getOperation().equals(OperationEnum.READ_CLIENT)){
                	if (bc.getClientName() != null) {
                		bc = db.read(bc.getClientName());
                		transaction.setBankClient(bc);
                		transaction.setStatus(ServiceStatus.OK);
                	}
                	else if (bc.getAccount() != 0) {
                		bc = db.read(bc.getAccount());
                		transaction.setBankClient(bc);
                		transaction.setStatus(ServiceStatus.OK);
                	}
                	status = ServiceStatus.OK;
                }
                if (transaction.getOperation().equals(OperationEnum.UPDATE_BANK)){
                	//TODO: Ni puta de que hay que hacer aquí
                }
                if (transaction.getOperation().equals(OperationEnum.UPDATE_CLIENT)){
                	status = db.update(bc.getAccount(), bc.getBalance());
                	transaction.setStatus(status);
                }
                
                //TODO: Actualizar handler para que acepte transactions (quizá en vez de cuentas)
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
