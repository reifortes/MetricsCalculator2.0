package CF.Producer_Consumer.Preprocessing;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import Output.OutPut;

public class ResourcePreprocessing {

	private LinkedList<RegisterPreprocessing> registers;
	protected boolean finished;
	OutPut outPut;
	protected Map<Long, Integer> hashCommomItem;
	protected Map<Long, Integer> hashCommomUser;

	public ResourcePreprocessing() {
		this.registers = new LinkedList<RegisterPreprocessing>();
		this.finished = false;
		this.hashCommomItem = Collections.synchronizedMap(new HashMap<Long, Integer>());
		this.hashCommomUser = Collections.synchronizedMap(new HashMap<Long, Integer>());
	}

	public synchronized void putRegister(RegisterPreprocessing register) {
		this.registers.addLast(register);
		wakeup();
	}

	protected void wakeup() {
		this.notify();
	}

	public synchronized RegisterPreprocessing getRegister() throws Exception {
		if (!this.registers.isEmpty()) {
			return this.registers.removeFirst();
		} else {
			if (finished == false) {
				suspend();
				// System.out.println("false");
			}
			return null;
		}
	}

	protected synchronized void suspend() throws Exception {
		wait();
	}

	public int getNumOfRegisters() {
		return this.registers.size();
	}

	public synchronized void setFinished() {
		this.finished = true;
		this.notifyAll();
	}

	public boolean isFinished() {
		return this.finished;
	}

	public OutPut getOutPut() {
		return outPut;
	}

	public void setOutPut(OutPut outPut) {
		this.outPut = outPut;
	}

	public boolean getFinished() {
		return finished;
	}

	public LinkedList<RegisterPreprocessing> getRegisters() {
		return registers;
	}

	public void setRegisters(LinkedList<RegisterPreprocessing> registers) {
		this.registers = registers;
	}

	public Map<Long, Integer> getHashCommomItem() {
		return hashCommomItem;
	}

	public synchronized void setHashCommomItem(Map<Long, Integer> hashCommomItem) {
		this.hashCommomItem = hashCommomItem;
	}

	public Map<Long, Integer> getHashCommomUser() {
		return hashCommomUser;
	}

	public synchronized void setHashCommomUser(Map<Long, Integer> hashCommomUser) {
		this.hashCommomUser = hashCommomUser;
	}

}
