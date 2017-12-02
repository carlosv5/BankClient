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
	private String myId = null;

	public ProcessMember(ZooKeeper zk, Watcher memberWatcherP, Integer mutex, String myId) {
		this.zk = zk;
		this.memberWatcherP = memberWatcherP;
		this.mutex = mutex;
		this.myId = myId;
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