package CF.Metrics.QuantitativeMetrics;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;

import CF.Metrics.Constant;
import CF.DataModel.DataModelMC;
import Output.OutPutFile;

public class NormalizedProportionOfRatings extends ProportionOfRatings {

	public NormalizedProportionOfRatings() throws TasteException{
		super();
	}
	
	public NormalizedProportionOfRatings(DataModelMC model) throws TasteException {
		super(model);
		preProcessing(true, true, true);
	}

	public NormalizedProportionOfRatings(DataModelMC model, OutPutFile outPutIO) throws TasteException {
		super(model, outPutIO);
		preProcessing(true, true, true);
	}

	@Override
	public void preProcessing(boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {

		if (preProcessingItem || preProcessingItemUser) {
			LongPrimitiveIterator itemIDs = getModel().getItemIDs();
			int itemMaxRatings = 0;
			for (; itemIDs.hasNext();) {
				long currentItem = itemIDs.next();
				if (getModel().getNumUsersWithPreferenceFor(currentItem) > itemMaxRatings) {
					itemMaxRatings = getModel().getNumUsersWithPreferenceFor(currentItem);
				}
			}
			setRpItem(itemMaxRatings);
		}

		if (preProcessingUser || preProcessingItemUser) {
			LongPrimitiveIterator userIDs = getModel().getUserIDs();
			int userMaxRatings = 0;
			for (; userIDs.hasNext();) {
				long currentUser = userIDs.next();
				if (getModel().getNumItemsWithPreferenceBy(currentUser) > userMaxRatings) {
					userMaxRatings = getModel().getNumItemsWithPreferenceBy(currentUser);
				}
			}
			setRpUser(userMaxRatings);
		}

		if (preProcessingItemUser) {
			setRpItemUser(getRpItem() + getRpUser());
		}
	}

	@Override
	public String getMetricNameID() {
		return "cf_NPR";
	}
	
	@Override
	public Long getNumMetric() { return getNumMetric(Constant.MetricEnum.NORMALIZED_PROPORTION_OF_RATINGS); }
}
