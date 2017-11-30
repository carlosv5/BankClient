package es.upm.dit.cnvr.model;

import java.io.Serializable;

public class BankClient implements Serializable {
	private String account;
	private String clientName;
	private double balance = 0;
	
	
	public BankClient(String account, String clientName, double balance) {
		this.account = account;
		this.clientName = clientName;
		this.balance = balance;
	}

	public static void main(String[] args) {
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String name) {
		this.clientName = name;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	@Override
	public String toString() {
		return "BankClient [account=" + account + ", clientName=" + clientName
				+ ", balance=" + balance + "]";
	}
	

}
