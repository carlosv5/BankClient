package es.upm.dit.cnvr.server;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import es.upm.dit.cnvr.model.ClientDB;
import es.upm.dit.cnvr.model.OperationEnum;
import es.upm.dit.cnvr.model.ServiceStatus;

public class ProcessOperation extends Thread{

	private List<String> listCounterP = null;
	private String rootOperation = "/operation";
	private String rootMember = "/members";
	private ZooKeeper zk; 
	private Watcher counterWatcherP;
	private Integer mutex;
	private Operate operate;
	private Integer mutexBarrier;
	private static String rootBarrier = "/boperation";
	private ClientDB db = ClientDB.getInstance();

	public ProcessOperation(ZooKeeper zk, Watcher counterWatcherP, Integer mutex, Integer mutexBarrier, Operate operate) {
		this.zk = zk;
		this.counterWatcherP = counterWatcherP;
		this.mutex = mutex;	
		this.operate = operate;
		this.mutexBarrier = mutexBarrier;
	}

	public byte[] createByte(OperationEnum operation){
		byte[] data = operation.toString().getBytes(Charset.forName("UTF-8"));
		return data;
	}
	public String readData(byte[] data){
		String string = new String(data, StandardCharsets.UTF_8);
		return string;
	}
	//TODO: Esto todavia no se ha mirado.
	@Override
	public void run() {
		Stat s = null;
		while (true) {
			try {
				synchronized (mutex) {
					mutex.wait();
				}
			    List<String> listCounter = zk.getChildren(rootOperation,  false, null);
				int size = listCounter.size();
			    List<String> listMembers = zk.getChildren(rootMember,  false, null);
				Barrier b = new Barrier(zk, rootBarrier, listMembers.size(), mutexBarrier);
				System.out.println("La lista del contador es " + size);
				//WATCHER Habr√≠a que hacerlo en el watcher
				b.enter();
				if (operate.getPersonalCounter() < size){
					System.out.println("DESACTUALIZADO");
					///XXX: Voy a cambiar de posiciÛn b.enter y b.leave de dentro del if afuera porque va a ser necesario que la operacion primera la haga en otro lado, y entonces estara al dia y no entrara a la barrera bloqueando a los demas
					int numOp = size-operate.getPersonalCounter();
					System.out.println("Tiene que hacer " + numOp + " cuentas");
					try {
						listCounter = zk.getChildren(rootOperation, false);
					} catch (KeeperException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					Collections.sort(listCounter);
					for(int i=operate.getPersonalCounter(); i<size;i++){
						byte[] op = new byte[0];
						try {
							op = zk.getData(rootOperation+"/" +listCounter.get(i),false, null);
							//TODO: Cambiar esto por las mierdas verdaderas (Operaciones verdaderas)
							
							if (readData(op).equals(OperationEnum.CREATE_CLIENT)){
			                	bc.setAccount(generateId(2));
			                	operate.setPersonalCounter(operate.getPersonalCounter()+1); 	
			                }
			                
			                if (readData(op).equals(OperationEnum.DELETE_CLIENT)){
			                	status = db.deleteClient(bc.getAccount(),bc.getClientName());
			                	operate.setPersonalCounter(operate.getPersonalCounter()+1); 	
			                }
			                if (readData(op).equals(OperationEnum.READ_CLIENT)){
			                	if (bc.getAccount() != "0") {
			                		bc = db.readAccount(bc.getAccount());
			                    	operate.setPersonalCounter(operate.getPersonalCounter()+1); 	
			                	}
			                	status = ServiceStatus.OK;
			                }
			                if (readData(op).equals(OperationEnum.UPDATE_BANK)){
			                }
			                if (readData(op).equals(OperationEnum.UPDATE_CLIENT)){
			                	status = db.update(bc.getAccount(), bc.getBalance());
			                	transaction.setStatus(status);
			                	operate.setPersonalCounter(operate.getPersonalCounter()+1);
			                }
							
							
							
							
							
							
//							if(readData(op).equals(OperationEnum.ADD.toString())){
//								System.out.println("La cuenta es antes: " + operate.getZkCounter());
//								operate.setZkCounter(operate.getZkCounter()+1);
//								System.out.println("La cuenta es despues: " + operate.getZkCounter());
//							}
							//TODO: Y esto
//							if(readData(op).equals(OperationEnum.REMOVE.toString())){
//								operate.setZkCounter(operate.getZkCounter()-1);
//							}
						} catch (KeeperException | InterruptedException e) {
							e.printStackTrace();
						}
						operate.setPersonalCounter(operate.getPersonalCounter()+1);
						System.out.println("La operacion que tiene que realizar es: " + readData(op));
						System.out.println("El valor del contador es zkCounter: " + operate.getZkCounter());
					}
					try {
						listCounter = zk.getChildren(rootOperation,  true, null);
					} catch (KeeperException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				listCounterP = zk.getChildren(rootOperation, counterWatcherP, s);
				
				System.out.println("Left counterbarrier");
			}
				b.leave();
		}  catch (Exception e) {
			System.out.println("Unexpected Exception process member");
			break;
		}
		}
	}
}