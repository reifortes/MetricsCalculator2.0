package CF.Metrics.QualitativeMetrics;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import CF.Metrics.AbstractMetric;
import CF.Metrics.Constant;
import CF.DataModel.DataModelMC;
import Output.OutPutFile;

public class PearsonCorrelation extends AbstractMetric {

	public PearsonCorrelation() {
		super();
	}

	public PearsonCorrelation(DataModelMC model) throws TasteException {
		super(model);
		preProcessing(true, true, true);
	}

	public PearsonCorrelation(DataModelMC model, OutPutFile outPutIO) throws TasteException {
		super(model, outPutIO);
		preProcessing(true, true, true);
	}

	public PearsonCorrelation(DataModelMC model, OutPutFile outPutIO, boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		super(model, outPutIO);
		preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);
	}

	public Double pearsonCalc(PreferenceArray ratingArray) {
		try {
			// Calculando o Desvio padrao sem conhecer a media
			double firstSum = 0;
			double secondSum = 0;
			double n = (double) (ratingArray.length());
			double standardDeviation = 0;

			for (int i = 0; i < ratingArray.length(); i++) {
				firstSum += Math.pow(ratingArray.getValue(i), 2);
				secondSum += ratingArray.getValue(i);
			}

			standardDeviation = Math.sqrt((firstSum - (Math.pow(secondSum, 2)) / n) / (n - 1));
			return standardDeviation / ((double) secondSum / n);
/*			double n = (double) (ratingArray.length());
			double average = 0;
			double standardDeviation = 0;
			Iterator<Preference> preferenceIterator;

			// Calculando a media
			preferenceIterator = ratingArray.iterator();
			for (; preferenceIterator.hasNext();) {
				average += preferenceIterator.next().getValue();
			}
			average = average / n;

			// Calculando o desvio padrao
			preferenceIterator = ratingArray.iterator();
			for (; preferenceIterator.hasNext();) {
				standardDeviation += Math.pow(preferenceIterator.next().getValue() - average, 2);
			}
			standardDeviation = Math.sqrt(standardDeviation / (n - 1));

			return standardDeviation / average;*/
		} catch (Exception e) {
			return null;
		}
	}

	public Double pearsonCalc(PreferenceArray itemRatingArray, PreferenceArray userRatingArray) {
		try {
			// Calculando o Desvio padrao sem conhecer a media
			double firstSum = 0;
			double secondSum = 0;
			double n = (double) (itemRatingArray.length() + userRatingArray.length());
			double standardDeviation = 0;

			for (int i = 0; i < itemRatingArray.length(); i++) {
				firstSum += Math.pow(itemRatingArray.getValue(i), 2);
				secondSum += itemRatingArray.getValue(i);
			}
			for (int i = 0; i < userRatingArray.length(); i++) {
				firstSum += Math.pow(userRatingArray.getValue(i), 2);
				secondSum += userRatingArray.getValue(i);
			}

			standardDeviation = Math.sqrt((firstSum - (Math.pow(secondSum, 2)) / n) / (n - 1));
			return standardDeviation / ((double) secondSum / n);

			// Calculando a media primeiro e depois o desvio
			/*
			 * double n = (double) (itemRatingArray.length() + userRatingArray .length()); double average = 0; double standardDeviation = 0; Iterator<Preference> preferenceIterator;
			 * 
			 * // Calculando a media preferenceIterator = itemRatingArray.iterator(); for (; preferenceIterator.hasNext();) { average += preferenceIterator.next().getValue(); } preferenceIterator =
			 * userRatingArray.iterator(); for (; preferenceIterator.hasNext();) { average += preferenceIterator.next().getValue(); } average = average / n;
			 * 
			 * // Calculando o desvio padrao preferenceIterator = itemRatingArray.iterator(); for (; preferenceIterator.hasNext();) { standardDeviation += Math.pow(preferenceIterator.next()
			 * .getValue() - average, 2); } preferenceIterator = userRatingArray.iterator(); for (; preferenceIterator.hasNext();) { standardDeviation += Math.pow(preferenceIterator.next() .getValue()
			 * - average, 2); } standardDeviation = Math.sqrt(standardDeviation / (n - 1));
			 * 
			 * return standardDeviation / average;
			 */

		} catch (Exception e) {
			return null;
		}
	}

	public Double itemValue(long itemID) {
		Double metricValue = 0.0;
		try {
			PreferenceArray preferenceItemsArray = getModel().getPreferencesForItem(itemID);
			metricValue = pearsonCalc(preferenceItemsArray);
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	public Double userValue(long userID) {
		Double metricValue = 0.0;
		try {
			PreferenceArray preferenceUsersArray = getModel().getPreferencesFromUser(userID);
			metricValue = pearsonCalc(preferenceUsersArray);
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	public Double itemUserValue(long itemID, long userID) {
		Double metricValue = 0.0;
		try {
			PreferenceArray preferenceItemsArray = getModel().getPreferencesForItem(itemID);
			PreferenceArray preferenceUsersArray = getModel().getPreferencesFromUser(userID);
			metricValue = pearsonCalc(preferenceItemsArray, preferenceUsersArray);
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	public Long getNumMetric() { return getNumMetric(Constant.MetricEnum.PEARSON_CORRELATION); }

	@Override
	public String getMetricNameID() {
		return "PearsonCorrelation";
	}

}
