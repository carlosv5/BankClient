package es.upm.dit.cnvr.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientDB implements Serializable {
	
	private java.util.HashMap <String, BankClient> clientDB;
	
	// Singleton
	
	private static ClientDB instance = null;

	private ClientDB() {
		clientDB = new java.util.HashMap <String, BankClient>();
	}
	
	public static ClientDB getInstance() {
		if (instance == null) {
			instance = new ClientDB();
		}
		return instance;
	}
	
	public ClientDB (ClientDB clientDB) {
		this.clientDB = clientDB.getClientDB();
	}

	public HashMap<String, BankClient> getClientDB() {
		return this.clientDB;
	}
	
	public ServiceStatus createClient(BankClient client) {
		ServiceStatus stat = ServiceStatus.INFORMATION_MISSED;
		if (clientDB.containsKey(client.getAccount()) && client.getBalance() >= 0) {
			// It already exists: We inform about it
			stat = ServiceStatus.EXISTING_CLIENT; 
		}else {
			// It doesn't exists: We create it
			clientDB.put(client.getAccount(), client);
			stat = ServiceStatus.OK;
		}
		//System.out.println("DB: " + clientDB.toString());
		printDb();
		return stat;
	}

	public BankClient readAccount(String clientAccount) {
		BankClient client = null;
		if (clientDB.containsKey(clientAccount)) 
			client = clientDB.get(clientAccount);
		//System.out.println("DB: " + clientDB.toString());
		printDb();
		return client;
	}
	
	public ServiceStatus update (String accountId, double balance) {
		ServiceStatus stat = ServiceStatus.INFORMATION_MISSED;
		if (balance >= 0 &&  clientDB.containsKey(accountId)){
				BankClient client = clientDB.get(accountId);
				client.setBalance(balance);
				clientDB.put(client.getAccount(), client);
				stat = ServiceStatus.OK;
			} else {
				stat = ServiceStatus.INFORMATION_INVALID;
			} 
		//System.out.println("DB: " + clientDB.toString());
		printDb();
		return stat;
	}
	
	public ServiceStatus deleteClient(String accountId, String clientName) {
		ServiceStatus stat = ServiceStatus.INFORMATION_MISSED;
		if (clientDB.containsKey(accountId) && (clientDB.get(accountId).getClientName().equals(clientName))) {
			clientDB.remove(accountId);
			stat = ServiceStatus.OK;	
		} else {
			stat = ServiceStatus.INFORMATION_INVALID;
		}
		//System.out.println("DB: " + clientDB.toString());
		printDb();
		return stat;
	}

	public boolean createBank(ClientDB clientDB) {
		this.clientDB = clientDB.getClientDB();
		return true;
	}
	public String toString() {
		String aux = new String();

		for (java.util.HashMap.Entry <String, BankClient>  entry : clientDB.entrySet()) {
			aux = aux + entry.getValue().toString() + "\n";
		}
		return aux;
	}
	
	public void printDb(){
		java.util.HashMap <String, BankClient> db = clientDB;
		System.out.println("******************************************");
		for (java.util.HashMap.Entry <String, BankClient>  entry : db.entrySet()) {
			System.out.print("Account ID: " + entry.getValue().getAccount());
        	System.out.print(" | Client Name: " + entry.getValue().getClientName());
        	System.out.println(" | Balance: " + entry.getValue().getBalance());
		}
		System.out.println("******************************************");
	}
}



