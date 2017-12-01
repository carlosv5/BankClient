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
			System.out.println(size);
			System.out.println(list.size());
			List<String> boperationList = null;
			boperationList = zk.getChildren(root+"leave", true);
			if (list.size() < size && boperationList.size()==0) {
				synchronized (mutexBarrier) {
					System.out.println("While antes del wait barrier.java");
					System.out.println("He hecho wait con el mutexBarrier: " + System.identityHashCode(mutexBarrier));
					mutexBarrier.wait();
					System.out.println("While tras el wait barrier.java");
				}
			} else {
				System.out.println("ESTADO T: " + boperationList.size());
				boperationList = zk.getChildren(root+"leave", true);
				if (boperationList.size()==0) {
					System.out.println("*********************** SE ESTA CREANDO EL NODO LEAVE **************");
					zk.create(root+"leave/nodo", new byte[0], Ids.OPEN_ACL_UNSAFE,
							CreateMode.EPHEMERAL);
				}
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
		System.out.println("He borrado el nodoB: " + nodoB);
		List<String> boperationList = zk.getChildren(root+"leave", true);
		System.out.println("Leave");
		System.out.println("La lista de "+ root+"leave"+" es: " + boperationList);
		System.out.println("El size de boperationleave es: " + boperationList.size());
		while (true) {
			List<String> list = zk.getChildren(root, true);
			
			if (list.size() > 0) {
				synchronized (mutexBarrier) {
					mutexBarrier.wait();
				}
			} else {
				if (boperationList.size()>0) {
					System.out.println("DELETING AUXILIAR NODE");
					zk.delete(root+"leave/nodo", 0);
				}
				return true;
			}
		}
	}
}
