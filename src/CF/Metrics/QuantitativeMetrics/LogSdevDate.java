package CF.Metrics.QuantitativeMetrics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.Preference;

import CF.DataModel.DataModelMC;
import CF.Metrics.AbstractMetric;
import CF.Metrics.Constant;
import Output.OutPutFile;

public class LogSdevDate extends AbstractMetric {

	protected long timeConvertion;
	
	public LogSdevDate()
	{
		super();
		this.timeConvertion = 1;
	}
	
	public LogSdevDate(DataModelMC model) throws TasteException {
		super(model);
		preProcessing(true, true, true);
		this.timeConvertion = 1;
	}

	public LogSdevDate(DataModelMC model, OutPutFile outPutIO) throws TasteException {
		super(model, outPutIO);
		this.timeConvertion = 1;
		preProcessing(true, true, true);
	}

	public LogSdevDate(DataModelMC model, OutPutFile outPutIO, long timeConvertion) throws TasteException {
		super(model, outPutIO);
		this.timeConvertion = timeConvertion;
		preProcessing(true, true, true);
	}

	public LogSdevDate(DataModelMC model, OutPutFile outPutIO, boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		super(model, outPutIO);
		this.timeConvertion = 1;
		preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);
	}

	public LogSdevDate(DataModelMC model, OutPutFile outPutIO, boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser, long timeConvertion) throws TasteException {
		super(model, outPutIO);
		this.timeConvertion = timeConvertion;
		preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);
	}	
	
	

	@Override
	public String getMetricNameID() {
		return "LogSdevDate";
	}
	
	public Double desvioPadrao(ArrayList<Long> list)
	{
		Iterator<Long> it = list.iterator();
		long sumi = 0;
		while(it.hasNext())
		{
			sumi += it.next();
		}
		double media = (double)sumi / list.size();
	
		double sumd = 0.0;
		it = list.iterator();
		long item;
		while(it.hasNext())
		{
			item = it.next();
			sumd += (media - item)* (media - item) ;
		}
		
		double variancia = sumd / list.size();
		
		return Math.sqrt(variancia);
		
	}

	@Override
	public Double itemValue(long itemID) 
	{
		ArrayList<Long> dates = new ArrayList();
		try 
		{
			for(Preference pref : getModel().getPreferencesForItem(itemID)) 
			{
				Long date = getModel().getPreferenceTime(pref.getUserID(), pref.getItemID());
				dates.add(date);
			}
			return Math.log10(desvioPadrao(dates));
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
			for(Preference pref : getModel().getPreferencesFromUser(userID)) 
			{
				Long date = getModel().getPreferenceTime(pref.getUserID(), pref.getItemID());
				dates.add(date);
			}
			return Math.log10(desvioPadrao(dates));
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
		try 
		{
			for(Preference pref : getModel().getPreferencesForItem(itemID)) 
			{
				Long date = getModel().getPreferenceTime(pref.getUserID(), pref.getItemID());
				dates.add(date);
			}
			for(Preference pref : getModel().getPreferencesFromUser(userID)) 
			{
				Long date = getModel().getPreferenceTime(pref.getUserID(), pref.getItemID());
				dates.add(date);
			}
			return Math.log10(desvioPadrao(dates));
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
	public Long getNumMetric() { return getNumMetric(Constant.MetricEnum.LOGSDEVDATE); }
	

}
