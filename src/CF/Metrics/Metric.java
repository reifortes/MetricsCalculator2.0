package CF.Metrics;

import java.io.IOException;
import java.util.Iterator;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.Preference;

import Output.OutPut;

public interface Metric {

	public void preProcessing(boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException;

	public Double itemValue(long itemID, OutPut outPut) throws TasteException, IOException;

	public void itemValuePartial(OutPut outPut, Iterator<Preference> preferences);

	public Double userValue(long userID, OutPut outPut) throws TasteException, IOException;

	public void userValuePartial(OutPut outPut, Iterator<Preference> preferences) throws TasteException;

	public Double itemUserValue(long itemID, long userID, OutPut outPut) throws TasteException, IOException;

	public void itemUserValuePartial(OutPut outPut, Iterator<Preference> preferences) throws TasteException, IOException;

	public String getName();
	
	public String getMetricNameID();

	public boolean isPreprocessed();
}
