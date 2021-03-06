package es.upm.dit.cnvr.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class Barrier implements Watcher {

	ZooKeeper zk = null;

	private int size;
	private String name;
	String root;
	int nWatchers;
	String nodoB = "";
	Integer mutexBarrier;


	/**
	 * Barrier constructor
	 *
	 * @param address
	 * @param root
	 * @param size
	 */

	public Barrier(ZooKeeper zk, String root, int size, Integer mutexBarrier) {
		this.zk = zk;
		this.root = root;
		this.size = size;
		this.mutexBarrier = mutexBarrier;

		// Create barrier node
		if (zk != null) {
			try {
				Stat s = zk.exists(root, false);
				if (s == null) {
					zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
							CreateMode.PERSISTENT);
				}
				
			} catch (KeeperException e) {
				System.out
				.println("Keeper exception when instantiating queue: "
						+ e.toString());
			} catch (InterruptedException e) {
				System.out.println("Interrupted exception");
			}
		}

		// My node name
		try {
			//hostname
			name = new String(InetAddress.getLocalHost().getCanonicalHostName().toString());
		} catch (UnknownHostException e) {
			System.out.println(e.toString());
		}
	}


	public void process(WatchedEvent event) {
		nWatchers++;
		System.out.println(">>> Process: " + event.toString() + ", " + nWatchers);
		System.out.println("Process: " + event.getType());
		synchronized (mutexBarrier) {
			mutexBarrier.notify();
		}
	}


	/**
	 * Join barrier
	 * @param listBarriers 
	 *
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 */

	boolean enter() throws KeeperException, InterruptedException{
		System.out.println("Start enter barrier");
		nodoB = zk.create(root + "/" + name, new byte[0], Ids.OPEN_ACL_UNSAFE,
				CreateMode.EPHEMERAL_SEQUENTIAL);
		
		zk.exists(root + "/" + name, false);
		System.out.println("He creado el nodoB: " + nodoB);
		while (true) {
			List<String> list = zk.getChildren(root, true);
			if (list.size() < size && zk.exists("/boperationleave", false)== null) {
				synchronized (mutexBarrier) {
					mutexBarrier.wait(1000);
				}
			} else {
				System.out.println("Enter");
				return true;
			}
		}
	}

	/**
	 * Wait until all reach barrier
	 *
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 */

	boolean leave() throws KeeperException, InterruptedException{
		System.out.println("Start leave barrier");
		zk.delete(nodoB, 0);
		while (true) {
			List<String> list = zk.getChildren(root, true);
			Stat stat = zk.exists("/boperationleave", false);
			if (list.size() > 0) {
				synchronized (mutexBarrier) {
					if(stat == null){
						try{
						zk.create("/boperationleave", new byte[0],
								Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
						} catch (Exception e1){
							System.out.println("Node boperationleave already created");

						}
					}
					mutexBarrier.wait(1000);
				}
			} else {
				System.out.println("Leave");
				if(stat != null){
					try{
					zk.delete("/boperationleave",0);
					} catch (Exception e1){
						System.out.println("Node boperationleave already created");

					}
				}
				return true;
			}
		}
	}
}
