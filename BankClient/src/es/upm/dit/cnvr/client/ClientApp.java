package es.upm.dit.cnvr.client;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import es.upm.dit.cnvr.model.BankClient;
import es.upm.dit.cnvr.model.OperationEnum;
import es.upm.dit.cnvr.model.Transaction;

public class ClientApp {
	//Set true or false debug 
	public static boolean debug = false;
    
	public ClientApp() {
	}

	
	public static void main(String[] args) { //throws Exception{
		System.out.println("|****************************************************|");
		System.out.println("|Welcome to the administration system of Eclipse Bank|");
		System.out.println("|*************************************************///|");
		System.out.println("|           ||||||||            ||||||||         ////|");
		System.out.println("|           ||                  ||    |||       /////|");
		System.out.println("|           ||                  ||    |||      //////|");
		System.out.println("|           ||||||||            ||||||||      ///////|");
		System.out.println("|           ||                  ||    |||    ////////|");
		System.out.println("|           ||                  ||    |||   /////////|");
		System.out.println("|           ||||||||            ||||||||   //////////|");
		System.out.println("|////////////////////////////////////////////////////|");

		showOptions();		
	}
	
	public BankClient getAccount(int accountId){
		return null;
		
	}
	
	public static void showOptions(){
		System.out.println("|       These are the operations you can do:         |");
		System.out.println("|1. Create a client                                  |");
		System.out.println("|   - Usage: CREATE [Name and lastnames] [Balance]   |");
		System.out.println("|2. Read a client account                            |");
		System.out.println("|   - Usage: READ [Account ID]                       |");
		System.out.println("|3. Update balance of a client account               |");
		System.out.println("|   - Usage: UPDATE [Account ID] [New Balance]       |");
		System.out.println("|4. Delete a client account                          |");
		//ClientName for security purposes
		System.out.println("|   - Usage: DELETE [Account ID] [Name and lastnames]|");
		System.out.println("|5. Show these options again                         |");
		System.out.println("|   - Usage: OPTIONS                                 |");
		System.out.println("|----------------------------------------------------|");
		System.out.println("|Enter an operation                                  |");
		while (true){
			Scanner keyboard = new Scanner(System.in);
		    String input = keyboard.nextLine();
		    String[] input_parts = input.split(" ");
		    boolean result = false;
        	//Variables for method
        	OperationEnum op;
        	String accountId = "0";
        	String clientName = "";
        	Double balance = 0.0;
		        switch (input_parts[0].toUpperCase()) {
		            case "CREATE":
					try {
						//Check options
						if (input_parts[1] == null || input_parts[2] == null || input_parts[3] == null
								|| input_parts[4] == null || input_parts.length > 5) {
							System.out.println("¡¡¡¡You are not using this operation properly!!!!\n\n");
							break;
						} 
					} catch (Exception e) {
						System.out.println("¡¡¡¡You are not using this operation properly!!!!\n\n");
						break;
					}
					//Variables for method
		            	op = OperationEnum.CREATE_CLIENT;
		            	clientName = input_parts[1]+" "+input_parts[2]+" "+input_parts[3];
		            	balance = Double.parseDouble(input_parts[4]);
		            	//Send
		            	result = operateAndSend(op,accountId,clientName,balance);		       
		                break;
		            case "READ":	
					try {
						//Check options
						if (input_parts[1] == null || input_parts.length > 2) {
							System.out.println("¡¡¡¡You are not using this operation properly!!!!\n\n");
							break;
						} 
					} catch (Exception e) {
						System.out.println("¡¡¡¡You are not using this operation properly!!!!\n\n");
						break;
					}
					//Variables for method
		            	op = OperationEnum.READ_CLIENT;
		            	accountId = input_parts[1];
		            	//Send
		            	result = operateAndSend(op,accountId,clientName,balance);		       
		            	break;
		            case "UPDATE":
					try {
						//Check options
						if (input_parts[1] == null || input_parts[2] == null || input_parts.length > 3) {
							System.out.println("¡¡¡¡You are not using this operation properly!!!!\n\n");
							break;
						} 
					} catch (Exception e) {
						System.out.println("¡¡¡¡You are not using this operation properly!!!!\n\n");
						break;
					}
					//Variables for method
		            	op = OperationEnum.UPDATE_CLIENT;
		            	accountId = input_parts[1];
		            	balance = Double.parseDouble(input_parts[2]);
		            	//Send
		            	result = operateAndSend(op,accountId,clientName,balance);		       
		            	break;
		            case "DELETE":
					try {
						//Check options
						if (input_parts[1] == null || input_parts[2] == null || input_parts[3] == null
								|| input_parts[4] == null || input_parts.length > 5) {
							System.out.println("¡¡¡¡You are not using this operation properly!!!!\n\n");
							break;
						} 
					} catch (Exception e) {
						System.out.println("¡¡¡¡You are not using this operation properly!!!!\n\n");
						break;
					}
					//Variables for method
		            	op = OperationEnum.DELETE_CLIENT;
		            	accountId = input_parts[1];
		                clientName = input_parts[2]+" "+input_parts[3]+" "+input_parts[4];
		            	//Send
		            	result = operateAndSend(op,accountId,clientName,balance);
		                break;
		            case "OPTIONS":
		                showOptions();
		                break;
		            default:
		        		System.out.println("This is not an available operation.");
		            	showOptions();
		   }
		        try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

		}
	}
	
	
	public static boolean operateAndSend(OperationEnum operation, String accountId,String clientName, double quantity){
        if(debug){
        	System.out.println("Debug operation");
        	System.out.println("Operation: " + operation);
        	System.out.println("Account ID: " + accountId);
        	System.out.println("Client Name: " + clientName);
        	System.out.println("Balance: " + quantity);
        }
        BankClient bc = new BankClient(accountId, clientName, quantity); 
        Transaction transaction = new Transaction(operation,bc);
        TCPClient.connection(transaction);
		return true;
		
	}

	
}
