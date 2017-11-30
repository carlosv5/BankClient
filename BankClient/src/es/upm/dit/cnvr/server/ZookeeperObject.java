package es.upm.dit.cnvr.server;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import es.upm.dit.cnvr.model.OperationEnum;


public class ZookeeperObject implements Watcher{

	private static ZooKeeper zk = null;
	private static String rootMembers = "/members";
	private static String rootBarrier = "/b1";
	private static String rootBarrierOperation = "/boperation";
	private static String aMember = "/member-";
	private static int nMembers  = 0;
	private static int nBarriers = 0;
	private static List<String> listMembers  = null;
	private String leader = "";


	private static Integer mutex        = -1;
	private static Integer mutexBarrier = -2;
	private static Integer mutexMember  = -3;
	private static Integer mutexOperate  = -4;

	private static final int SESSION_TIMEOUT = 5000;
	private String myId = null;
	
	private String rootOperation = "/operation";
	private static Operate operate;

	/**
	 * Constructor
	 *
	 */
	public ZookeeperObject() { 

	}

	public static Integer getMutexOperate() {
		return mutexOperate;
	}

	public static Integer getMutexBarrier() {
		return mutexBarrier;
	}

	public void configure() {	
		System.out.println("START CONFIGURE");
		// This is static. A list of zookeeper can be provided for decide where to connect
		String[] hosts = {"138.4.31.99:2181", "138.4.31.98:2182"};

		// Select a random zookeeper server
		Random rand = new Random();
		int i = rand.nextInt(hosts.length);

		// Create the session
		// Create a session and wait until it is created.
		// When is created, the watcher is notified
		try {
			if (getZk() == null) {
				setZk(new ZooKeeper(hosts[i], SESSION_TIMEOUT, this));
				// We initialize the mutex Integer just after creating ZK.
				try {
					// Wait for creating the session. Use the object lock
					synchronized(mutex) {
						mutex.wait();
					}
					//zk.exists("/", false);
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			System.out.println("Exception in constructor");
		}

		// Add the process to the members in zookeeper

		if (getZk() != null) {
			// Create a folder for members and include this process/server
			try {
				// Create the /members znode
				// Create a folder, if it is not created
				Stat s = getZk().exists(rootMembers, false);
				if (s == null) {
					// Created the znode, if it is not created.
					getZk().create(rootMembers, new byte[0],
							Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				myId = getZk().create(rootMembers + aMember, new byte[0], Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL_SEQUENTIAL);
				myId = myId.replace(rootMembers + "/", "");
				System.out.println("Created znode nember id:" + myId);
				// Create a znode for registering as member and get my id
				//myId = zk.create(rootMembers + aMember, new byte[0],
				//		Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
				//myId = myId.replace(rootMembers + "/", "");
				// false. Debe esperar a arrancar el barrir
				listMembers = getZk().getChildren(rootMembers,  memberWatcher, s);
				printListMembers(listMembers);
				//System.out.println("Created znode nember id:"+ myId );


			} catch (KeeperException e) {
				System.out.println("The session with Zookeeper failes. Closing");
				return;
			} catch (InterruptedException e) {
				System.out.println("InterruptedException raised");
			}

			// Create the /b1 znode
			if (getZk() != null) {
				try {
					Stat s = getZk().exists(rootBarrier, false);
					if (s == null) {
						getZk().create(rootBarrier, new byte[0], Ids.OPEN_ACL_UNSAFE,
								CreateMode.PERSISTENT);
					}

//					getZk().create(rootBarrier, new byte[0],
//							Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
					//myId = myId.replace(rootMembers + "/", "");
					// false. Debe esperar a arrancar el barrir
					getZk().getChildren(rootBarrier,  barrierWatcher, s);

				} catch (KeeperException e) {
					System.out
					.println("Keeper exception when instantiating queue: "
							+ e.toString());
				} catch (InterruptedException e) {
					System.out.println("Interrupted exception");
				}
			}
			// Create the /b1 znode
			if (getZk() != null) {
				try {
					Stat s = getZk().exists(rootBarrierOperation, false);
					if (s == null) {
						getZk().create(rootBarrierOperation, new byte[0], Ids.OPEN_ACL_UNSAFE,
								CreateMode.PERSISTENT);
					}

//					getZk().create(rootBarrier, new byte[0],
//							Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
					//myId = myId.replace(rootMembers + "/", "");
					// false. Debe esperar a arrancar el barrir
					getZk().getChildren(rootBarrierOperation,  barrierWatcher, s);

				} catch (KeeperException e) {
					System.out
					.println("Keeper exception when instantiating queue: "
							+ e.toString());
				} catch (InterruptedException e) {
					System.out.println("Interrupted exception");
				}
			}
			if (getZk() != null) {
				// Create a folder for members and include this process/server
				try {
					// Create the /members znode
					// Create a folder, if it is not created
					Stat s = getZk().exists(rootOperation, false);
					if (s == null) {
						// Created the znode, if it is not created.
						getZk().create(rootOperation, new byte[0],
								Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					}
				} catch (KeeperException e) {
					System.out.println("The session with Zookeeper failes. Closing");
					return;
				} catch (InterruptedException e) {
					System.out.println("InterruptedException raised");
				}
			}
			try {
				getZk().getChildren(rootOperation,  false, null);
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			operate = new Operate(getZk());
			try {
				getZk().getChildren(rootOperation,  counterOperation, null);
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Create threads
		ProcessMember pm = new ProcessMember(getZk(), memberWatcher, mutexMember, myId, mutexBarrier);
		pm.start();
		ProcessBarrier bm = new ProcessBarrier(getZk(), barrierWatcher, mutexBarrier);
		bm.start();
		ProcessOperation cm = new ProcessOperation(getZk(), counterOperation, mutexOperate, mutexBarrier, operate);
		cm.start();
		
		synchronized (mutexMember) {
			mutexMember.notify();
		}
	}
	
	public Operate getOperate() {
		return operate;
	}

	public void setOperate(Operate operate) {
		this.operate = operate;
	}

	public void printListMembers(List<String> list) {
		System.out.println("Remaining # members:" + list.size());
		System.out.print("The active members are: ");
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
				System.out.print(string + ", ");
		}
		System.out.println();
		System.out.println("---------------------------------------------------");
	}
	// Assigned to members
	@Override
	public void process(WatchedEvent event) {
		Stat s = null;

		System.out.println("------------------Watcher PROCESS ------------------");
		System.out.println("Member: " + event.getType() + ", " + event.getPath());
		try {
			if (event.getPath() == null) {			
				//if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
				System.out.println("SyncConnected");
				synchronized (mutex) {
					mutex.notifyAll();
				}

			}
			System.out.println("-----------------------------------------------");
		} catch (Exception e) {
			System.out.println("Unexpected Exception process");
		}
	}	

	public static ZooKeeper getZk() {
		return zk;
	}

	public static void setZk(ZooKeeper zk) {
		ZookeeperObject.zk = zk;
	}

	Watcher memberWatcher = new Watcher() {
		public void process(WatchedEvent event) { 

			Stat s = null;

			System.out.println("------------------Watcher MEMBER ------------------");
			System.out.println("Member: " + event.getType() + ", " + event.getPath());
			try {
				if (event.getPath() == null) {			
					//if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
					System.out.println("SyncConnected");
					synchronized (mutex) {
						mutex.notifyAll();
					}
				}
				else if (event.getPath().equals(rootMembers)) {
					listMembers = getZk().getChildren(rootMembers, false, s);
					synchronized (mutexMember) {
						nMembers ++;
						System.out.println("MW Members: " + nMembers);
						mutexMember.notifyAll();
					}
				}
				else if (event.getPath().equals(rootBarrier)) {
					//listBarriers = zk.getChildren(rootBarrier, this.barrierWatcher, s); 
					synchronized (mutexBarrier) {
						nBarriers ++;
						System.out.println("Unexpeted to handle this watcher. MW NBarriers: " + nBarriers);
						mutexBarrier.notifyAll();
					}
				}
				System.out.println("-----------------------------------------------");
			} catch (Exception e) {
				System.out.println("Unexpected Exception process");
			}
		}
	};

	Watcher barrierWatcher = new Watcher() {
		public void process(WatchedEvent event) {
			Stat s = null;

			System.out.println("------------------Watcher BARRIER ------------------");
			System.out.println("Barrier: " + event.getType() + ", " + event.getPath());
			try {
				if (event.getPath() == null) {			
					//if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
					System.out.println("SyncConnected");
					synchronized (mutex) {
						mutex.notifyAll();
					}
				}
				else if (event.getPath().equals(rootMembers)) {
					//listMembers = zk.getChildren(rootMembers, process, s);
					//&synchronized (mutexMember) {
					synchronized (mutexMember) {
						nMembers ++;
						System.out.println("Unexpeted to handle this watcher. BW Members: " + nMembers);
						mutexMember.notifyAll();
					}
				}
				else if (event.getPath().equals(rootBarrier)) {
					//listBarriers = zk.getChildren(rootBarrier, this.barrierWatcher, s); 
					synchronized (mutexBarrier) {
						nBarriers ++;
						System.out.println("BW: NBarriers: " + nBarriers);
						mutexBarrier.notifyAll();
						System.out.println("He hecho notify con el mutexBarrier: " + System.identityHashCode(mutexBarrier));
					}
				}
				System.out.println("-----------------------------------------------");
			} catch (Exception e) {
				System.out.println("Unexpected Exception process");
			}
		}
	};
	
	Watcher counterOperation = new Watcher() {
		public void process(WatchedEvent event) { 

			Stat s = null;

			System.out.println("------------------Watcher COUNTER ------------------");
			System.out.println("Counter: " + event.getType() + ", " + event.getPath());
			try {
				if (event.getPath() == null) {			
					//if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
					System.out.println("SyncConnected");
					synchronized (mutex) {
						mutex.notifyAll();
					}
				}
				else if (event.getPath().equals(rootMembers)) {
					listMembers = getZk().getChildren(rootMembers, false, s);
					synchronized (mutexMember) {
						nMembers ++;
						System.out.println("MW Members: " + nMembers);
						mutexMember.notifyAll();
					}
				}
				else if (event.getPath().equals(rootOperation)) {
					listMembers = getZk().getChildren(rootOperation, false, s);
					synchronized (mutexOperate) {
						mutexOperate.notifyAll();
					}
				}
				else if (event.getPath().equals(rootBarrier)) {
					//listBarriers = zk.getChildren(rootBarrier, this.barrierWatcher, s); 
					synchronized (mutexBarrier) {
						nBarriers ++;
						System.out.println("Unexpeted to handle this watcher. MW NBarriers: " + nBarriers);
						mutexBarrier.notifyAll();
					}
				}
				System.out.println("-----------------------------------------------");
			} catch (Exception e) {
				System.out.println("Unexpected Exception process");
			}
		}
	};
}


