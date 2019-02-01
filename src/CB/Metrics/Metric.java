package CB.Metrics;


import com.ecyrd.speed4j.StopWatch;

import CB.Resource;
import Output.OutPut;

public interface Metric {
	public void setResource(Resource r);
	public void setParameter(String id, String value);
	public void itemValues(int itemID, OutPut o);
	public void itemValues(int itemID, OutPut o, StopWatch timeWatch);
	public String getMetricNameID();
	public boolean isNormalized();
	public void normalize();
}
