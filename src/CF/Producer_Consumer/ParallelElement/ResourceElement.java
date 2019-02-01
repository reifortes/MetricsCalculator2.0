package CF.Producer_Consumer.ParallelElement;

import java.util.LinkedList;

import CF.Metrics.Metric;
import Output.OutPut;

public class ResourceElement {

	private LinkedList<RegisterElement> registers;
	protected boolean finished;
	private Metric metric;
	OutPut outPut;

	public ResourceElement() {
		this.registers = new LinkedList<RegisterElement>();
		this.finished = false;
	}

	public Metric getMetric() {
		return metric;
	}

	public void setMetric(Metric metric) {
		this.metric = metric;
	}

	public synchronized void putRegister(RegisterElement register) {
		this.registers.addLast(register);
		wakeup();
	}

	protected void wakeup() {
		this.notify();
	}

	public synchronized RegisterElement getRegister() throws Exception {
		if (!this.registers.isEmpty()) {
			return this.registers.removeFirst();
		} else {
			if (finished == false) {
				suspend();
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

}
