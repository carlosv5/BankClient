package es.upm.dit.cnvr.server;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import es.upm.dit.cnvr.model.BankClient;
import es.upm.dit.cnvr.model.ClientDB;
import es.upm.dit.cnvr.model.OperationEnum;
import es.upm.dit.cnvr.model.Transaction;

public class ProcessOperation extends Thread{

	private String rootOperation = "/operation";
	private String rootMember = "/members";
	private ZooKeeper zk; 
	private Watcher operationWatcherP;
	private Integer mutex;
	private Operate operate;
	private Integer mutexBarrier;
	private static String rootBarrier = "/boperation";
	private ClientDB db = ClientDB.getInstance();
	private Transaction transaction;
	private BankClient bc;

	public ProcessOperation(ZooKeeper zk, Watcher operationWatcherP, Integer mutex, Integer mutexBarrier, Operate operate) {
		this.zk = zk;
		this.operationWatcherP = operationWatcherP;
		this.mutex = mutex;	
		this.operate = operate;
		this.mutexBarrier = mutexBarrier;
	}

	public Transaction readData(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
	    return (Transaction) is.readObject();
		
//		String string = new String(data, StandardCharsets.UTF_8);
//		return string;
	}
	@Override
	public void run() {
		Stat s = null;
		
		while (true) {
			try {
				synchronized (mutex) {
					mutex.wait();
				}
				Stat stat = null;
				try {
					stat = zk.exists("/boperationleave", false);
					if(stat != null){
						zk.delete("/boperationleave", 0);
					}
				} catch (KeeperException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			    List<String> listOperation = zk.getChildren(rootOperation,  false, null);
				int size = listOperation.size();
			    List<String> listMembers = zk.getChildren(rootMember,  false, null);
				Barrier b = new Barrier(zk, rootBarrier, listMembers.size(), mutexBarrier);
				//WATCHER Habría que hacerlo en el watcher
				zk.getChildren(rootOperation, operationWatcherP, s);
				b.enter();
				if (operate.getPersonalCounter() < size){
					System.out.println("DESACTUALIZADO");
					int numOp = size-operate.getPersonalCounter();
					try {
						listOperation = zk.getChildren(rootOperation, false);
					} catch (KeeperException | InterruptedException e) {
						e.printStackTrace();
					} 
					Collections.sort(listOperation);
					for(int i=operate.getPersonalCounter(); i<size;i++){
						byte[] op = new byte[0];
						try {
							op = zk.getData(rootOperation+"/" +listOperation.get(i),false, null);
							transaction = readData(op);
							bc = transaction.getBankClient();
							
							if (readData(op).getOperation().equals(OperationEnum.CREATE_CLIENT)){
			                	db.createClient(bc);
			                	operate.setPersonalCounter(operate.getPersonalCounter()+1); 	
			                }
			                
			                if (readData(op).getOperation().equals(OperationEnum.DELETE_CLIENT)){
			                	db.deleteClient(bc.getAccount(),bc.getClientName());
			                	operate.setPersonalCounter(operate.getPersonalCounter()+1); 	
			                }
			                
			                if (readData(op).getOperation().equals(OperationEnum.UPDATE_CLIENT)){
			                	db.update(bc.getAccount(), bc.getBalance());
			                	operate.setPersonalCounter(operate.getPersonalCounter()+1);
			                }
							
						} catch (KeeperException | InterruptedException e) {
							e.printStackTrace();
						}
						//operate.setPersonalCounter(operate.getPersonalCounter()+1);
						System.out.println("The operation to do is: " + readData(op).getOperation().toString());
					}
					try {
						listOperation = zk.getChildren(rootOperation,  true, null);
					} catch (KeeperException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				
				System.out.println("Left counterbarrier");
			}			
				b.leave();
//				synchronized(ZookeeperObject.getMutexOperate()){
//					ZookeeperObject.getMutexOperate().notify();
//				}
				zk.getChildren(rootOperation, operationWatcherP, s);
		}  catch (Exception e) {
			System.out.println("Unexpected Exception process member"+e.toString());
			break;
		}
		}
	}
}