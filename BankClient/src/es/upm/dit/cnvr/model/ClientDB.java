package es.upm.dit.cnvr.model;

import java.io.Serializable;

public class ClientDB implements Serializable {
	
	private java.util.HashMap <Integer, BankClient> clientDB;

	public ClientDB() {
		clientDB = new java.util.HashMap <Integer, BankClient>();
	}
	
	public ClientDB (ClientDB clientDB) {
		this.clientDB = clientDB.getClientDB();
	}

	public java.util.HashMap <Integer, BankClient> getClientDB() {
		return this.clientDB;
	}
	
	public ServiceStatus createClient(BankClient client) {
		ServiceStatus stat = ServiceStatus.INFORMATION_MISSED;
		if (clientDB.containsKey(client.getAccount())) {
			//it already exists
			stat = ServiceStatus.EXISTING_CLIENT; 
		}else {
			//create
			stat = ServiceStatus.OK;
		}
		return stat;
	}
	
	public ServiceStatus readAccount(BankClient client) {
		ServiceStatus stat = ServiceStatus.INFORMATION_MISSED;
		if (clientDB.containsKey(client.getAccount())) {
			//read
			stat = ServiceStatus.OK;
		}else {
			//client not in database
			stat = ServiceStatus.INFORMATION_INVALID;
		}
		return stat;
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public BankClient read(String clientName) {
		BankClient client = null;
		if (clientDB.containsKey(clientName)) {
			client = clientDB.get(clientName);
		}else {
			client = null;
		}
		return client;
	}
	
	public BankClient read(int clientAccount) {
		BankClient client = null;
		if (clientDB.containsKey(clientAccount)) {
			client = clientDB.get(clientAccount);
		}else {
			client = null;
		}
		return client;
	}

	//update?
	
	// TODO: Hay que meter que devuelvan services-status
	public boolean update (int account, double balance) {
		if (clientDB.containsKey(account)) {
			BankClient client = clientDB.get(account);
			client.setBalance(balance);
			clientDB.put(client.getAccount(), client);
			return true;
		} else {
			return false;
		}	
	}
	
	// TODO: Hay que meter que devuelvan services-status
	public boolean deleteClient(Integer accountNumber) {
		if (clientDB.containsKey(accountNumber)) {
			clientDB.remove(accountNumber);
			return true;	
		} else {
			return false;
		}	
	}

	public boolean createBank(ClientDB clientDB) {
		this.clientDB = clientDB.getClientDB();
		return true;
	}
	
	public String toString() {
		String aux = new String();

		for (java.util.HashMap.Entry <Integer, BankClient>  entry : clientDB.entrySet()) {
			aux = aux + entry.getValue().toString() + "\n";
		}
		return aux;
	}
}



