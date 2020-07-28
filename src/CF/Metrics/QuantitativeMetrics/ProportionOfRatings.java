package CF.Metrics.QuantitativeMetrics;

import org.apache.mahout.cf.taste.common.TasteException;

import CF.Metrics.AbstractMetric;
import CF.Metrics.Constant;
import CF.DataModel.DataModelMC;
import Output.OutPutFile;

public class ProportionOfRatings extends AbstractMetric {

	protected double RpItem;
	protected double RpUser;
	protected double RpItemUser;

	public ProportionOfRatings() throws TasteException {
		super();
	}
	
	public ProportionOfRatings(DataModelMC model) throws TasteException {
		super(model);
		preProcessing(true, true, true);
	}

	public ProportionOfRatings(DataModelMC model, OutPutFile outPutIO) throws TasteException {
		super(model, outPutIO);
		preProcessing(true, true, true);
	}

	public ProportionOfRatings(DataModelMC model, OutPutFile outPutIO, boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		super(model, outPutIO);
		preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);
	}

	public synchronized Double itemValue(long itemID) {
		Double metricValue = 0.0;
		try {
			metricValue = 1 - ((double) getModel().getNumUsersWithPreferenceFor(itemID) / RpItem);
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	public Double userValue(long userID) {
		Double metricValue = 0.0;
		try {
			metricValue = 1 - ((double) getModel().getNumItemsWithPreferenceBy(userID) / RpUser);
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	public Double itemUserValue(long itemID, long userID) {
		Double metricValue = 0.0;
		try {
			metricValue = 1 - (((double) getModel().getNumUsersWithPreferenceFor(itemID) + (double) getModel().getNumItemsWithPreferenceBy(userID)) / RpItemUser);
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	@Override
	public void preProcessing(boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		if (preProcessingItem || preProcessingItemUser) {
			setRpItem((double) getModel().getNumUsers());
		}
		if (preProcessingUser || preProcessingItemUser) {
			setRpUser((double) getModel().getNumItems());
		}
		if (preProcessingItemUser) {
			setRpItemUser(getRpItem() + getRpUser());
		}
	}

	public Long getNumMetric() {
		return getNumMetric(Constant.MetricEnum.PROPORTION_OF_RATINGS);
	}

	public double getRpItem() {
		return RpItem;
	}

	public void setRpItem(double rpItem) {
		RpItem = rpItem;
	}

	public double getRpUser() {
		return RpUser;
	}

	public void setRpUser(double rpUser) {
		RpUser = rpUser;
	}

	public double getRpItemUser() {
		return RpItemUser;
	}

	public void setRpItemUser(double rpItemUser) {
		RpItemUser = rpItemUser;
	}
	
	public String getMetricNameID() {
		return "cf_PR";
	}
	
}
