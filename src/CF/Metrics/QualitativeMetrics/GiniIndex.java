package CF.Metrics.QualitativeMetrics;

import java.util.Vector;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import CF.Metrics.AbstractMetric;
import CF.Metrics.Constant;
import CF.DataModel.DataModelMC;
import Output.OutPutFile;

public class GiniIndex extends AbstractMetric {

	public GiniIndex() {
		super();
	}

	public GiniIndex(DataModelMC model) throws TasteException {
		super(model);
		preProcessing(true, true, true);
	}

	public GiniIndex(DataModelMC model, OutPutFile outPutIO) throws TasteException {
		super(model, outPutIO);
		preProcessing(true, true, true);
	}

	public GiniIndex(DataModelMC model, OutPutFile outPutIO, boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		super(model, outPutIO);
		preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);
	}

	public Double calcGini(PreferenceArray ratingArray) {
		try {
			double sum = 0;
			ratingArray.sortByValue();
			double sumOfRatings = 0;
			for (int i = 0; i < ratingArray.length(); i++) {
				sumOfRatings += (float) ratingArray.getValue(i);
			}
			for (int i = 0; i < ratingArray.length(); i++) {
				sum += ((double) ratingArray.getValue(i) / (double) sumOfRatings) * ((double) (ratingArray.length() - (i + 1) + 0.5) / (double) ratingArray.length());
			}
			return 1 - (2 * sum);
		} catch (Exception e) {
			return null;
		}
	}

	public Double calcGini(PreferenceArray itemRatingArray, PreferenceArray userRatingArray) {
		try {
			itemRatingArray.sortByValue();
			userRatingArray.sortByValue();

			double sumOfRatings = 0;
			double sum = 0;
			int k = 1;
			int n = itemRatingArray.length() + userRatingArray.length();
			Vector<Float> allRatings = new Vector<Float>();

			for (int i = 0; i < itemRatingArray.length(); i++) {
				sumOfRatings += (double) itemRatingArray.getValue(i);
			}
			for (int i = 0; i < userRatingArray.length(); i++) {
				sumOfRatings += (double) userRatingArray.getValue(i);
			}

			int itemArrayPosition = 0;
			int userArrayPosition = 0;
			for (int i = 0; i < itemRatingArray.length() + userRatingArray.length(); i++) {
				if (itemRatingArray.getValue(itemArrayPosition) <= userRatingArray.getValue(userArrayPosition)) {
					allRatings.add(itemRatingArray.getValue(itemArrayPosition));
					sum += ((double) itemRatingArray.getValue(itemArrayPosition) / sumOfRatings) * ((double) (n - k + 0.5) / (double) (n));
					k++;
					if (itemArrayPosition < itemRatingArray.length() - 1) {
						itemArrayPosition++;
					}
				} else {
					allRatings.add(userRatingArray.getValue(userArrayPosition));
					sum += ((double) userRatingArray.getValue(userArrayPosition) / sumOfRatings) * ((double) (n - k + 0.5) / (double) (n));
					k++;
					if (userArrayPosition < userRatingArray.length() - 1) {
						userArrayPosition++;
					}
				}
			}
			return 1 - (2 * sum);
		} catch (Exception e) {
			return null;
		}
	}

	public synchronized Double itemValue(long itemID) {
		Double metricValue = 0.0;
		try {
			PreferenceArray ratingItemArray = getModel().getPreferencesForItem(itemID);
			metricValue = calcGini(ratingItemArray);
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	public Double userValue(long userID) {
		Double metricValue = 0.0;
		try {
			PreferenceArray ratingUserArray = getModel().getPreferencesFromUser(userID);
			metricValue = calcGini(ratingUserArray);
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	public Double itemUserValue(long itemID, long userID) {
		Double metricValue = 0.0;
		try {
			PreferenceArray itemRatingArray = getModel().getPreferencesForItem(itemID);
			PreferenceArray userRatingArray = getModel().getPreferencesFromUser(userID);
			metricValue = calcGini(itemRatingArray, userRatingArray);
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	public Long getNumMetric() {
		return getNumMetric(Constant.MetricEnum.GINI_INDEX);
	}

	@Override
	public String getMetricNameID() {
		return "cf_Gini";
	}

	
}
