package es.upm.dit.cnvr;

import java.io.Serializable;

public class BankClient implements Serializable {
	private int account;
	private String clientName;
	private double balance = 0;
	
	
	public BankClient(int account, String clientName, double balance) {
		this.account = account;
		this.clientName = clientName;
		this.balance = balance;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public int getAccount() {
		return account;
	}

	public void setAccount(int account) {
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
