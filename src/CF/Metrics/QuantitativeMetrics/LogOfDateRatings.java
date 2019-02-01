package CF.Metrics.QuantitativeMetrics;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.Preference;

import CF.Metrics.AbstractMetric;
import CF.Metrics.Constant;
import CF.DataModel.DataModelMC;
import Output.OutPutFile;

public class LogOfDateRatings extends AbstractMetric {

	protected Map<Long, Integer> hashLogDtdDatesItem;
	protected Map<Long, Integer> hashLogDtdDatesUser;
	protected long timeConvertion;

	public LogOfDateRatings()
	{
		super();
		this.timeConvertion = 1;
	}
	
	public LogOfDateRatings(DataModelMC model) throws TasteException {
		super(model);
		preProcessing(true, true, true);
		this.timeConvertion = 1;
	}

	public LogOfDateRatings(DataModelMC model, OutPutFile outPutIO) throws TasteException {
		super(model, outPutIO);
		this.timeConvertion = 1;
		preProcessing(true, true, true);
	}

	public LogOfDateRatings(DataModelMC model, OutPutFile outPutIO, long timeConvertion) throws TasteException {
		super(model, outPutIO);
		this.timeConvertion = timeConvertion;
		preProcessing(true, true, true);
	}

	public LogOfDateRatings(DataModelMC model, OutPutFile outPutIO, boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		super(model, outPutIO);
		this.timeConvertion = 1;
		preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);
	}

	public LogOfDateRatings(DataModelMC model, OutPutFile outPutIO, boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser, long timeConvertion) throws TasteException {
		super(model, outPutIO);
		this.timeConvertion = timeConvertion;
		preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);
	}

	public synchronized Double itemValue(long itemID) {
		Double metricValue = 0.0;
		try {
			if(hashLogDtdDatesItem.containsKey(itemID)) {
				int qtd = hashLogDtdDatesItem.get(itemID);
				metricValue = qtd != 0 ? Math.log10(qtd) / Math.log10(2) : 0;
			}
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	public Double userValue(long userID) {
		Double metricValue = 0.0;
		try {
			//if(hashLogDtdDatesUser.containsKey(userID)) {
				int qtd = hashLogDtdDatesUser.get(userID);
				metricValue = qtd != 0 ? Math.log10(qtd) / Math.log10(2) : 0;
			//}
		} catch (Exception e) {
			e.printStackTrace();
			metricValue = null;
		}
		return metricValue;
	}

	public Double itemUserValue(long itemID, long userID) {
		Double metricValue = null;
		//TODO implementar
		return metricValue;
	}

	@Override
	public void preProcessing(boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		hashLogDtdDatesItem = new HashMap<Long, Integer>();
		hashLogDtdDatesUser = new HashMap<Long, Integer>();
		
		HashSet<String> dates;
		
		// Processamento de item
		if (preProcessingItem || preProcessingItemUser) {
			LongPrimitiveIterator arrayItemIterator = getModel().getItemIDs();
			for (; arrayItemIterator.hasNext();) {
				Long itemId = arrayItemIterator.next();
				dates = new HashSet<String>();
				for(Preference pref : getModel().getPreferencesForItem(itemId)) {
					try {
						String date = convertTime(getModel().getPreferenceTime(pref.getUserID(), pref.getItemID()) * timeConvertion);
						dates.add(date);
					} catch(Exception e) {
						//
					}
				}
				hashLogDtdDatesItem.put(itemId, dates.size());
			}
		}

		// Processamento de usuario
		if (preProcessingUser || preProcessingItemUser) {
			LongPrimitiveIterator arrayUserIterator = getModel().getUserIDs();
			for (; arrayUserIterator.hasNext();) {
				Long userId = arrayUserIterator.next();
				dates = new HashSet<String>();
				for(Preference pref : getModel().getPreferencesFromUser(userId)) {
					try {
						String date = convertTime(getModel().getPreferenceTime(pref.getUserID(), pref.getItemID()));
						dates.add(date);
					} catch(Exception e) {
						//
					}
				}
				hashLogDtdDatesUser.put(userId, dates.size());
			}
		}

		// Processamento de item-usuario
		if (preProcessingItemUser) {
			//TODO processar Item-User
		}
	}
	
	private String convertTime(long time){
	    Date date = new Date(time);
	    Format format = new SimpleDateFormat("yyyy-MM-dd");
	    return format.format(date);
	}

	public Long getNumMetric() {
		return getNumMetric(Constant.MetricEnum.LOG_OF_DATE_RATINGS);
	}
	
	public String getMetricNameID() {
		return "LogOfDateRatings";
	}

}
