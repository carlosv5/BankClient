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
			// It already exists: We inform about it
			stat = ServiceStatus.EXISTING_CLIENT; 
		}else {
			// It doesn't exists: We create it
			clientDB.put(client.getAccount(), client);
			stat = ServiceStatus.OK;
		}
		return stat;
	}
	
	public ServiceStatus readAccount(BankClient client) {
		ServiceStatus stat = ServiceStatus.INFORMATION_MISSED;
		
		if (client != null && client.getAccount() != 0) {
			if (clientDB.containsKey(client.getAccount())) {
				//read
				stat = ServiceStatus.OK;
			} else {
				//client not in database
				stat = ServiceStatus.INFORMATION_INVALID;
			} 
		}
		return stat;
	}
	/* TODO: Este esta mal: No puedes buscar como key el nombre, son enteros. Sugerencia: Convertir a lista
	 * e intentar hacer una búsqueda clasica, porque ni siquiera estamos buscando values, estamos buscando
	 * un parametro (el nombre) de los objetos. 
	 */
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
	
	public ServiceStatus update (int account, double balance) {
		ServiceStatus stat = ServiceStatus.INFORMATION_MISSED;
		if (account != 0 && balance > 0) {
			if (clientDB.containsKey(account)) {
				BankClient client = clientDB.get(account);
				client.setBalance(balance);
				clientDB.put(client.getAccount(), client);
				stat = ServiceStatus.OK;
			} else {
				stat = ServiceStatus.INFORMATION_INVALID;
			} 
		}
		return stat;
	}
	
	public ServiceStatus deleteClient(Integer accountNumber) {
		ServiceStatus stat = ServiceStatus.INFORMATION_MISSED;
		if (clientDB.containsKey(accountNumber)) {
			clientDB.remove(accountNumber);
			stat = ServiceStatus.OK;	
		} else {
			stat = ServiceStatus.INFORMATION_INVALID;
		}
		return stat;
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



