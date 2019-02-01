package CF.Metrics.QuantitativeMetrics;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import CF.DataModel.DataModelMC;
import CF.Metrics.AbstractMetric;
import CF.Metrics.Constant;
import Output.OutPutFile;

public class RatingsMean extends AbstractMetric {

	
	public RatingsMean()
	{
		super();
	}
	
	public RatingsMean(DataModelMC model) throws TasteException {
		super(model);
		preProcessing(true, true, true);
	}

	public RatingsMean(DataModelMC model, OutPutFile outPutIO) throws TasteException {
		super(model, outPutIO);
		preProcessing(true, true, true);
	}

	public RatingsMean(DataModelMC model, OutPutFile outPutIO, long timeConvertion) throws TasteException {
		super(model, outPutIO);
		preProcessing(true, true, true);
	}

	public RatingsMean(DataModelMC model, OutPutFile outPutIO, boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		super(model, outPutIO);
		preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);
	}

	public RatingsMean(DataModelMC model, OutPutFile outPutIO, boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser, long timeConvertion) throws TasteException {
		super(model, outPutIO);
		preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);
	}	
	
	

	@Override
	public String getMetricNameID() {
		return "LogSdevDate";
	}
	
	public Double media(PreferenceArray preferenceArray)
	{
		Iterator<Preference> it = preferenceArray.iterator();
		double sumd = 0;
		Preference p;
		while(it.hasNext())
		{
			p = it.next();
			sumd += p.getValue();
		}
		double media = sumd / preferenceArray.length();
		return media;
	}

	@Override
	public Double itemValue(long itemID) 
	{
		try 
		{
			return media(getModel().getPreferencesForItem(itemID));
		} 
		catch (NoSuchItemException e)
		{
			System.err.println("Warning: some item not found");
		}
		catch (TasteException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Double userValue(long userID) 
	{
		ArrayList<Long> dates = new ArrayList();
		try 
		{
			return media(getModel().getPreferencesFromUser(userID));
		} 
		catch (NoSuchItemException e)
		{
			System.err.println("Warning: some item not found");
		}
		catch (TasteException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Double itemUserValue(long itemID, long userID) 
	{
		ArrayList<Long> dates = new ArrayList();	
		return ( itemValue(itemID) + userValue(userID) ) / 2.0;
	}

	@Override
	public Long getNumMetric() { return getNumMetric(Constant.MetricEnum.LOGSDEVDATE); }
	

}