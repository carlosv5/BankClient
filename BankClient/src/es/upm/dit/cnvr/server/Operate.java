package es.upm.dit.cnvr.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import es.upm.dit.cnvr.model.OperationEnum;
import es.upm.dit.cnvr.model.Transaction;

// This class only create the znodes, it doesn't do any operation actually.

//TODO: Plantear con bloqueo para que no se cree una operacion si ya se esta haciendo una

public class Operate{

	private ZooKeeper zk;
	
	private String rootOperate = "/operation";
	private  int personalCounter = 0;
	private  int zkCounter = 0;
	
	public Operate(ZooKeeper zk) {
		this.zk = zk;
		
		configure();
		}
	
	public byte[] createByte(Transaction transaction){
		byte[] data = transaction.toString().getBytes(Charset.forName("UTF-8"));
		return data;
	}
	public Transaction readData(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
	    if(es.upm.dit.cnvr.client.ClientApp.debug){
	    	System.out.println("Debug operation (Operate.java): Se esta creando el objeto transaction:" + ((Transaction) is.readObject()).toString());
	    }
	    return (Transaction) is.readObject();
		
//		String string = new String(data, StandardCharsets.UTF_8);
//		return string;
	}

	public int getPersonalCounter() {
		return personalCounter;
	}

	public void setPersonalCounter(int personalCounter) {
		this.personalCounter = personalCounter;
	}

	public int getZkCounter() {
		return zkCounter;
	}

	public void setZkCounter(int zkCounter) {
		this.zkCounter = zkCounter;
	}
	
	public void operation(Transaction transaction) throws ClassNotFoundException, IOException{
		String name="";
		try {
			name = new String(InetAddress.getLocalHost().getCanonicalHostName().toString());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		//TO CREATE THE OPERATION
		byte[] data = createByte(transaction);
		String node = "";
		try {
			node = zk.create(rootOperate + "/" + name, data, Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT_SEQUENTIAL);
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//TO READ NODE OPERATION
		System.out.println("Los datos guardados son: " + readData(data).toString());
		System.out.println("Created znode counter id:" + node);
		try {
			zk.getChildren(rootOperate, false, null);
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void configure(){
		System.out.println("-------------Entra en cuenta");
		if (zk != null) {
			// Create a folder for operations and include this process/server
			try {
				// Create the /operation znode
				// Create a folder, if it is not created
				Stat s = zk.exists(rootOperate, false);
				if (s == null) {
					// Created the znode, if it is not created.
					zk.create(rootOperate, new byte[0],
							Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}


			} catch (KeeperException e) {
				System.out.println("The session with Zookeeper failes. Closing");
				return;
			} catch (InterruptedException e) {
				System.out.println("InterruptedException raised");
			}


		}
	}
}
