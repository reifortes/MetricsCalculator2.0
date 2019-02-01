package CF.Producer_Consumer.ParallelElement;

import CF.DataModel.DataModelMC;
import Output.OutPutFile;

import com.ecyrd.speed4j.StopWatch;

import CF.Metrics.Metric;
import Measure.CPUTimer;

public class ConsumerElement extends Thread {

	private ResourceElement resource;
	DataModelMC model;
	Metric metric;
	StopWatch stItem;
	StopWatch stUser;
	StopWatch stItemUser;
	OutPutFile outFile;
	CPUTimer timer;

	public ConsumerElement(ResourceElement re, OutPutFile outFile) {
		timer = new CPUTimer("consumerElement thread");
		timer.start();
		this.resource = re;
		this.outFile = outFile;
		this.stItem = new StopWatch();
		this.stUser = new StopWatch();
		this.stItemUser = new StopWatch();
	}

	public synchronized void run() {

		RegisterElement register = null;
		try {
			while ((resource.isFinished() == false) || (resource.getNumOfRegisters() != 0)) {
				if ((register = resource.getRegister()) != null) {
					switch (register.getVersion()) {
						case ITEM_VALUE:
							stItem.start();
							register.getMetric().itemValuePartial(register.getOutPut(), register.getIterador());
							stItem.stop();
							if(outFile != null) outFile.setValue(getNumMetric(register.getMetric()), null, 1.0); // stItem.getTimeNanos()
							break;

						case USER_VALUE:
							stUser.start();
							register.getMetric().userValuePartial(register.getOutPut(), register.getIterador());
							stUser.stop();
							if(outFile != null) outFile.setValue(getNumMetric(register.getMetric()), null, 2.0); //, stUser.getTimeNanos()
							break;

						case ITEM_USER_VALUE:
							stItemUser.start();
							register.getMetric().itemUserValuePartial(register.getOutPut(), register.getIterador());
							stItemUser.stop();
							if(outFile != null) outFile.setValue(getNumMetric(register.getMetric()), null, 3.0); //stItemUser.getTimeNanos()
							break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		timer.stop();
		timer.print();
	}

	private Long getNumMetric(Metric metric) {
		// TODO  Aqui se caracterizava cada metrica por um valor. Refazer
		return (Long) null;
	}
}