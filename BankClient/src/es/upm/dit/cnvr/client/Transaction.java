package es.upm.dit.cnvr.client;

import java.io.Serializable;

public class Transaction implements Serializable {
	private OperationEnum operation;
	private BankClient bankClient;

	public Transaction(OperationEnum op, BankClient bc) {
		// TODO Auto-generated constructor stub
		this.operation = op;
		this.bankClient = bc;
	}

	public OperationEnum getOperation() {
		return operation;
	}

	public void setOperation(OperationEnum operation) {
		this.operation = operation;
	}

	public BankClient getBankClient() {
		return bankClient;
	}

	public void setBankClient(BankClient bankClient) {
		this.bankClient = bankClient;
	}

	@Override
	public String toString() {
		return "Transaction [operation=" + operation + ", bankClient="
				+ bankClient + "]";
	}


	

}
