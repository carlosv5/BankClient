package es.upm.dit.cnvr.server;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;


public class ProcessMember extends Thread{

	private List<String> listMembersP = null;
	private int npMembers = 0;
	private String rootMember = "/members";
	private ZooKeeper zk; 
	private Watcher memberWatcherP;
	private Integer mutex;
	private String leader = "";
	private String myId = null;
	private String rootBarrier = "/b1";
	private Integer mutexBarrier;

	public ProcessMember(ZooKeeper zk, Watcher memberWatcherP, Integer mutex, String myId, Integer mutexBarrier) {
		this.zk = zk;
		this.memberWatcherP = memberWatcherP;
		this.mutex = mutex;
		this.mutexBarrier = mutexBarrier;
		this.myId = myId;
	}
	private void leaderElection(List<String> list) {
		Barrier b = new Barrier(zk, rootBarrier, list.size(), mutexBarrier);
		try {
			boolean flag = b.enter();
			System.out.println("Flag es: " + flag);
			System.out.println(
					"Entered barrier. There are " + list.size() + " members. They are going to wait in the barrier");
			if (!flag)
				System.out.println("Error when entering the barrier");
		} catch (KeeperException e) {

		} catch (InterruptedException e) {

		}

		// Election
		Collections.sort(list);
		leader = list.get(0);
		if (leader.equals(myId)) {
			System.out.println("****You are the leader****");
		} else {
			System.out.println("The process " + leader + " is the leader");
		}

		try {
			b.leave();
		} catch (KeeperException e) {
			System.out.println(e.toString());
		} catch (InterruptedException e) {
			System.out.println(e.toString());
		}
		System.out.println("Left barrier");

	}

	public void printListMembers(List<String> list) {
		System.out.println("----------------------EL EUROMILLONES----------------------------");
		leaderElection(list);
		System.out.println("Remaining # members:" + list.size());
		System.out.print("The active members are: ");
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			if (leader.equals(string)) {
				System.out.print("(L) " + string + ", ");
			} else {
				System.out.print(string + ", ");
			}
		}
		System.out.println();
		System.out.println("---------------------------------------------------");

	}
	@Override
	public void run() {
		Stat s = null;
		while (true) {
			try {
				synchronized (mutex) {
					mutex.wait();
				}
				listMembersP = zk.getChildren(rootMember, memberWatcherP, s); 
				printListMembers(listMembersP);
				npMembers ++;
				//System.out.println("Process Member. NMembers: " + npMembers);
			} catch (Exception e) {
				System.out.println("Unexpected Exception process member");
				break;
			}
		}		
	}
}