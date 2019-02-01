package CF.Metrics;

import java.io.IOException;
import java.util.Iterator;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.Preference;

import CF.DataModel.DataModelMC;
import CF.Producer_Consumer.Preprocessing.ResourcePreprocessing;
import Output.OutPut;
import Output.OutPutFile;

public abstract class AbstractMetric implements Metric {
	protected DataModelMC model;
	

	protected Constant.MetricEnum thisMetric;
	OutPutFile outPutIO = null;
	
	public AbstractMetric() {
		super();
	}

	public AbstractMetric(DataModelMC model) {
		super();
		setModel(model);
	}

	public AbstractMetric(DataModelMC model, OutPutFile outPutIO) throws TasteException {
		super();
		setModel(model);
		this.outPutIO = outPutIO;
	}

	public void preProcessing(boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		// Do nothing.
	}

	public abstract Double itemValue(long itemID);

	public synchronized Double itemValue(long itemID, OutPut outPut) throws TasteException, IOException {

		Double metricValue = itemValue(itemID);
		outPut.setValue(itemID, null, metricValue); //Long flushOccurrence = outPut.setValue(itemID, null, metricValue);
		return metricValue;
	}

	public void itemValuePartial(OutPut outPut, Iterator<Preference> preferences) {
		try {
			while (preferences.hasNext()) {
				itemValue(preferences.next().getItemID(), outPut);
			}
		} catch (Exception e) {
			System.err.println("Error: " + e);
		}
	}

	public abstract Double userValue(long itemID);

	public synchronized Double userValue(long userID, OutPut outPut) throws TasteException, IOException {

		Double metricValue = userValue(userID);
		outPut.setValue(null, userID, metricValue); //Long flushOccurrence = outPut.setValue(null, userID, metricValue);
		return metricValue;
	}

	public void userValuePartial(OutPut outPut, Iterator<Preference> preferences) throws TasteException {
		try {
			while (preferences.hasNext()) {
				userValue(preferences.next().getUserID(), outPut);
			}
		} catch (Exception e) {
			System.err.println("Error: " + e);
		}
	}

	public abstract Double itemUserValue(long itemID, long userID);

	public Double itemUserValue(long itemID, long userID, OutPut outPut) throws TasteException, IOException {
		Double metricValue = itemUserValue(itemID, userID);
		outPut.setValue(itemID, userID, metricValue); //Long flushOccurrence = outPut.setValue(itemID, userID, metricValue);

		return metricValue;
	}

	public void itemUserValuePartial(OutPut outPut, Iterator<Preference> preferences) throws TasteException, IOException {
		Preference currentPreference;
		while (preferences.hasNext()) {
			currentPreference = (Preference) preferences.next();
			itemUserValue(currentPreference.getItemID(), currentPreference.getUserID(), outPut);
		}
	}

	public void setModel(DataModelMC model) { this.model = model; }

	public DataModelMC getModel() { return model; }

	public abstract Long getNumMetric();

	public static Long getNumMetric(Constant.MetricEnum metricEnum) {
		switch (metricEnum) {
			case GINI_INDEX:
				return new Long(1);
			case PEARSON_CORRELATION:
				return new Long(2);
			case PQ_MEAN:
				return new Long(3);
			case NORMALIZED_PROPORTION_OF_COMMOM_RATINGS:
				return new Long(4);
			case NORMALIZED_PROPORTION_OF_RATINGS:
				return new Long(5);
			case PROPORTION_OF_COMMOM_RATINGS:
				return new Long(6);
			case PROPORTION_OF_RATINGS:
				return new Long(7);
			case STANDARD_DEVIATION:
				return new Long(8);
			case LOG_OF_QTD_RATINGS:
				return new Long(9);
			case LOG_OF_DATE_RATINGS:
				return new Long(10);
			case PR_OF_DATE_RATINGS:
				return new Long(11);
			default:
				return null;
		}
	}

public String getName()
{
	return "Missing metric name";
}

public String getMetricNameId()
{
	return "Unknown";
}

public boolean isPreprocessed() 
{
	return false;	
}

public void setResourcePreprocessing(ResourcePreprocessing rp) {
	// do nothing, this should be overrided by preprocessed metrics
	}

}
