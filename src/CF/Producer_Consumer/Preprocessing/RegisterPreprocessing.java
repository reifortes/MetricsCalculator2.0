package CF.Producer_Consumer.Preprocessing;

import java.util.Iterator;

import org.apache.mahout.cf.taste.model.Preference;

import CF.Metrics.Constant;
import Output.OutPut;

public class RegisterPreprocessing {

	Iterator<Preference> iterador;
	private Constant.VersionElementMetricEnum version;
	OutPut outPut;

	public RegisterPreprocessing() {
		// do nothing
	}

	public RegisterPreprocessing(int lowerLimit, int upperLimit, Constant.VersionElementMetricEnum version, OutPut outPut) {
		this.version = version;
		this.outPut = outPut;
	}

	public Iterator<Preference> getIterador() {
		return iterador;
	}

	public void setIterador(Iterator<Preference> iterador) {
		this.iterador = iterador;
	}

	public OutPut getOutPut() {
		return outPut;
	}

	public void setOutPut(OutPut outPut) {
		this.outPut = outPut;
	}

	public Constant.VersionElementMetricEnum getVersion() {
		return version;
	}

	public void setVersion(Constant.VersionElementMetricEnum version) {
		this.version = version;
	}
}
