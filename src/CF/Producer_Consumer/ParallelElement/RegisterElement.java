package CF.Producer_Consumer.ParallelElement;

import java.util.Iterator;

import org.apache.mahout.cf.taste.model.Preference;

import CF.Metrics.Constant;
import CF.Metrics.Metric;
import Output.OutPut;


public class RegisterElement {
	
	Iterator<Preference> iterador;
	private Constant.VersionElementMetricEnum version;
	Metric metric;
	OutPut outPut;

	public RegisterElement() {
		//do nothing
	}

	public Iterator<Preference> getIterador() {
		return iterador;
	}

	public void setIterador(Iterator<Preference> iterador) {
		this.iterador = iterador;
	}

	public RegisterElement(Constant.VersionElementMetricEnum version, Metric metric, OutPut outPut) {
		this.version = version;
		this.metric = metric;
		this.outPut = outPut;
	}

	public OutPut getOutPut() {
		return outPut;
	}

	public void setOutPut(OutPut outPut) {
		this.outPut = outPut;
	}

	public Metric getMetric() {
		return metric;
	}

	public void setMetric(Metric metric) {
		this.metric = metric;
	}

	public Constant.VersionElementMetricEnum getVersion() {
		return version;
	}

	public void setVersion(Constant.VersionElementMetricEnum version) {
		this.version = version;
	}			
}
