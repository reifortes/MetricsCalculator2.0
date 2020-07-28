package CF.Metrics.QuantitativeMetrics;

import org.apache.mahout.cf.taste.common.TasteException;

import CF.Metrics.Constant;
import CF.DataModel.DataModelMC;
import Output.OutPutFile;

public class PRDateRatings extends LogOfDateRatings {

	protected double RpItem;
	protected double RpUser;
	protected double RpItemUser;

	public PRDateRatings()
	{
		super();
	}
	
	public PRDateRatings(DataModelMC model) throws TasteException {
		super(model);
		preProcessing(true, true, true);
		this.timeConvertion = 1;
	}

	public PRDateRatings(DataModelMC model, OutPutFile outPutIO) throws TasteException {
		super(model, outPutIO);
		this.timeConvertion = 1;
		preProcessing(true, true, true);
	}

	public PRDateRatings(DataModelMC model, OutPutFile outPutIO, long timeConvertion) throws TasteException {
		super(model, outPutIO);
		this.timeConvertion = timeConvertion;
		preProcessing(true, true, true);
	}

	public PRDateRatings(DataModelMC model, OutPutFile outPutIO, boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		super(model, outPutIO);
		this.timeConvertion = 1;
		preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);
	}

	public PRDateRatings(DataModelMC model, OutPutFile outPutIO, boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser, long timeConvertion) throws TasteException {
		super(model, outPutIO);
		this.timeConvertion = timeConvertion;
		preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);
	}

	public synchronized Double itemValue(long itemID) {
		Double metricValue = 0.0;
		try {
			metricValue = hashLogDtdDatesItem.get(itemID) / RpItem;
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	public Double userValue(long userID) {
		Double metricValue = 0.0;
		try {
			metricValue = hashLogDtdDatesUser.get(userID) / RpUser;
		} catch (Exception e) {
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
		super.preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);
		
		// Processamento de item
		if (preProcessingItem || preProcessingItemUser) {
			RpItem = Double.MIN_VALUE;
			for(int qtd : hashLogDtdDatesItem.values()) {
				if(qtd > RpItem) RpItem = qtd;
			}
		}

		// Processamento de usuario
		if (preProcessingUser || preProcessingItemUser) {
			RpUser = Double.MIN_VALUE;
			for(int qtd : hashLogDtdDatesUser.values()) {
				if(qtd > RpUser) RpUser = qtd;
			}
		}

		// Processamento de item-usuario
		if (preProcessingItemUser) {
			//TODO processar Item-User
		}
	}

	public Long getNumMetric() {
		return getNumMetric(Constant.MetricEnum.PR_OF_DATE_RATINGS);
	}
	
	public String getMetricNameID() {
		return "cf_PRDateRatings";
	}

}
