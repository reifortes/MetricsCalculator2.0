package CF.Metrics.QuantitativeMetrics;

import org.apache.mahout.cf.taste.common.TasteException;

import CF.Metrics.AbstractMetric;
import CF.Metrics.Constant;
import CF.DataModel.DataModelMC;
import Output.OutPutFile;

public class LogOfQtdRatings extends AbstractMetric {

	public LogOfQtdRatings()
	{
		super();

	}
	
	public LogOfQtdRatings(DataModelMC model) throws TasteException {
		super(model);
		preProcessing(true, true, true);
	}

	public LogOfQtdRatings(DataModelMC model, OutPutFile outPutIO) throws TasteException {
		super(model, outPutIO);
		preProcessing(true, true, true);
	}

	public LogOfQtdRatings(DataModelMC model, OutPutFile outPutIO, boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		super(model, outPutIO);
		preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);
	}

	public synchronized Double itemValue(long itemID) {
		Double metricValue = 0.0;
		try {
			double qtd = getModel().getNumUsersWithPreferenceFor(itemID);
			metricValue = qtd != 0 ? Math.log10(qtd) / Math.log10(2) : 0;
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	public Double userValue(long userID) {
		Double metricValue = 0.0;
		try {
			double qtd = getModel().getNumItemsWithPreferenceBy(userID);
			metricValue = qtd != 0 ? Math.log10(qtd) / Math.log10(2) : 0;
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	public Double itemUserValue(long itemID, long userID) {
		Double metricValue = 0.0;
		try {
			double qtd = getModel().getNumUsersWithPreferenceFor(itemID) + (double) getModel().getNumItemsWithPreferenceBy(userID);
			metricValue = qtd != 0 ? Math.log10(qtd) / Math.log10(2) : 0;
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	@Override
	public void preProcessing(boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		//
	}

	public Long getNumMetric() {
		return getNumMetric(Constant.MetricEnum.LOG_OF_QTD_RATINGS);
	}
	
	public String getMetricNameID() {
		return "LogOfAmountRatings";
	}

}
