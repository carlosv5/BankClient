package es.upm.dit.cnvr.server;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ProcessBarrier  extends Thread {

	private List<String> listBarriersP = null;
	private int npBarriers = 0;
	private String rootBarrier = "/boperation";
	private ZooKeeper zk; 
	private Watcher barrierWatcherP;
	private Integer mutex;




	public ProcessBarrier(ZooKeeper zk, Watcher barrierWatcherP, Integer mutex) {
		this.zk = zk;
		this.barrierWatcherP = barrierWatcherP;
		this.mutex = mutex;	
	}

	@Override
	public void run() {
		Stat s = null;
		while (true) {
			try {
				synchronized (mutex) {
					mutex.wait();
				}
				System.out.println("Recargando el watcher del barrier cuando ha saltado");
				listBarriersP = zk.getChildren(rootBarrier, barrierWatcherP, s); 
			} catch (Exception e) {
				System.out.println("Unexpected Exception process barrier");
				break;
			}	
		}
	}
}