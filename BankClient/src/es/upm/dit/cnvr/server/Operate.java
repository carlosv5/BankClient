package es.upm.dit.cnvr.server;

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

public class Operate{

	private int nCounters;
	private ZooKeeper zk;
	
	private String rootOperate = "/operation";
	private  int personalCounter = 0;
	private  int zkCounter = 0;
	private List<String> listCounter = null;

	public Operate(ZooKeeper zk) {
		this.zk = zk;
		
		configure();
		}
	
	public byte[] createByte(OperationEnum operation){
		byte[] data = operation.toString().getBytes(Charset.forName("UTF-8"));
		return data;
	}
	public String readData(byte[] data){
		String string = new String(data, StandardCharsets.UTF_8);
		return string;
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
	
	public void operation(OperationEnum operation){
		String name="";
		try {
			name = new String(InetAddress.getLocalHost().getCanonicalHostName().toString());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		//TO CREATE THE OPERATION
		byte[] data = createByte(operation);
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
		System.out.println("Los datos guardados son: " + readData(data));
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
