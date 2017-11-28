package es.upm.dit.cnvr.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientDB implements Serializable {
	
	private java.util.HashMap <String, BankClient> clientDB;

	public ClientDB() {
		clientDB = new java.util.HashMap <String, BankClient>();
	}
	
	public ClientDB (ClientDB clientDB) {
		this.clientDB = clientDB.getClientDB();
	}

	public HashMap<String, BankClient> getClientDB() {
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
			if(es.upm.dit.cnvr.client.ClientApp.debug){
				System.out.println("DB has: ");
				System.out.println(clientDB.toString());
			}
			stat = ServiceStatus.OK;
		}
		return stat;
	}
	
	public ServiceStatus readAccount(BankClient client) {
		ServiceStatus stat = ServiceStatus.INFORMATION_MISSED;
		
		if (client != null && !client.getAccount().equals("0")) {
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
	/* TODO: -SOLUCIONADO- Este esta mal: No puedes buscar como key el nombre, son enteros. Sugerencia: Convertir a lista
	 * e intentar hacer una busqueda clasica, porque ni siquiera estamos buscando values, estamos buscando
	 * un parametro (el nombre) de los objetos.
	 * 
	 * Act1: Solucionado.
	 * Este metodo no tiene sentido! No podemos buscar una cuenta por el nombre del cliente
	 */

//	public BankClient readByClient(String clientName) {
//		BankClient client = null;
//		for (Map.Entry<String, BankClient> entry : clientDB.entrySet()){
//			if (entry.getValue().getClientName().equals(clientName)) client = entry.getValue();
//		}
//		return client;
//	}

	public BankClient readById(String clientAccount) {
		BankClient client = null;
		if (clientDB.containsKey(clientAccount)) client = clientDB.get(clientAccount);
		return client;
	}

	//update?
	
	public ServiceStatus update (String string, double balance) {
		ServiceStatus stat = ServiceStatus.INFORMATION_MISSED;
		if (string.equals("0") && balance > 0) {
			if (clientDB.containsKey(string)) {
				BankClient client = clientDB.get(string);
				client.setBalance(balance);
				clientDB.put(client.getAccount(), client);
				stat = ServiceStatus.OK;
			} else {
				stat = ServiceStatus.INFORMATION_INVALID;
			} 
		}
		return stat;
	}
	
	public ServiceStatus deleteClient(String string) {
		ServiceStatus stat = ServiceStatus.INFORMATION_MISSED;
		if (clientDB.containsKey(string)) {
			clientDB.remove(string);
			stat = ServiceStatus.OK;	
		} else {
			stat = ServiceStatus.INFORMATION_INVALID;
		}
		return stat;
	}

	// XXX: Diria de cambiar esto por un getBank, porque es lo que realmente hace. No se si seria de utilidad aun asi
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
}



