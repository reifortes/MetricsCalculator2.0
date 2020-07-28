package CF.Metrics.QualitativeMetrics;

import java.util.Iterator;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import CF.Metrics.AbstractMetric;
import CF.Metrics.Constant;
import CF.DataModel.DataModelMC;
import Output.OutPutFile;

public class PqMean extends AbstractMetric {

	//TODO: use these as parameters
	double p = 1.0;
	double q = 3.0;

	public PqMean() {
		super();
	}

	public PqMean(DataModelMC model, double p, double q) throws TasteException {
		super(model);
		preProcessing(true, true, true);

		if (p > 1)
			System.err.println("p value must be less than or equal to one");
		if (q <= 1)
			System.err.println("q value must be greater than one");

		this.p = p;
		this.q = q;
	}

	public PqMean(DataModelMC model, double p, double q, OutPutFile outPutIO) throws TasteException {
		super(model, outPutIO);
		preProcessing(true, true, true);

		if (p > 1)
			System.err.println("p value must be less than or equal to one");
		if (q <= 1)
			System.err.println("q value must be greater than one");

		this.p = p;
		this.q = q;
	}

	public PqMean(DataModelMC model, double p, double q, OutPutFile outPutIO, boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		super(model, outPutIO);
		preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);

		if (p > 1)
			System.err.println("p value must be less than or equal to one");
		if (q <= 1)
			System.err.println("q value must be greater than one");

		this.p = p;
		this.q = q;
	}

	public Double calcPqMean(PreferenceArray ratingArray) {
		try {
			Iterator<Preference> iteratorRatingItem = ratingArray.iterator();
			double sumP = 0;
			double sumQ = 0;
			double n = (double) ratingArray.length();

			for (; iteratorRatingItem.hasNext();) {
				double currentRatingItem = iteratorRatingItem.next().getValue();
				sumP += (double) Math.pow((double) currentRatingItem, (double) p);
				sumQ += (double) Math.pow((double) currentRatingItem, (double) q);
			}

			// TODO Verificar sinal...
			// return -1*((Math.pow(((double)sumP/n),(1/p)))*
			// ((Math.pow(((double)sumQ/n),-(1/q)))));
			return ((Math.pow(((double) sumP / n), (1 / p))) * ((Math.pow(((double) sumQ / n), -(1 / q)))));

		} catch (Exception e) {
			return null;
		}
	}

	public Double calcPqMean(PreferenceArray itemRaingArray, PreferenceArray userRaingArray) {
		try {
			Iterator<Preference> iteratorRatingItem = itemRaingArray.iterator();
			Iterator<Preference> iteratorRatingUser = userRaingArray.iterator();

			double sumP = 0;
			double sumQ = 0;
			double n = (double) itemRaingArray.length() + (double) userRaingArray.length();

			for (; iteratorRatingItem.hasNext();) {
				double currentRatingItem = iteratorRatingItem.next().getValue();
				sumP += (double) Math.pow((double) currentRatingItem, (double) p);
				sumQ += (double) Math.pow((double) currentRatingItem, (double) q);
			}

			for (; iteratorRatingUser.hasNext();) {
				double currentRatingUser = iteratorRatingUser.next().getValue();
				sumP += (double) Math.pow((double) currentRatingUser, (double) p);
				sumQ += (double) Math.pow((double) currentRatingUser, (double) q);
			}

			// TODO Verificar sinal...
			// return -1*((Math.pow(((double)sumP/n),(1/p)))*
			// ((Math.pow(((double)sumQ/n),-(1/q)))));
			return ((Math.pow(((double) sumP / n), (1 / p))) * ((Math.pow(((double) sumQ / n), -(1 / q)))));

		} catch (Exception e) {
			return null;
		}
	}

	public Double itemValue(long itemID) {
		Double metricValue = 0.0;
		try {
			PreferenceArray ratingItemArray = getModel().getPreferencesForItem(itemID);
			metricValue = calcPqMean(ratingItemArray);
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	public Double userValue(long userID) {
		Double metricValue = 0.0;
		try {
			PreferenceArray ratingUserArray = getModel().getPreferencesFromUser(userID); // USE THIS ONE, Future me
			metricValue = calcPqMean(ratingUserArray);
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	public Double itemUserValue(long itemID, long userID) {
		Double metricValue = 0.0;
		try {
			PreferenceArray itemRaingArray = getModel().getPreferencesForItem(itemID);
			PreferenceArray userRaingArray = getModel().getPreferencesFromUser(userID);
			metricValue = calcPqMean(itemRaingArray, userRaingArray);
		} catch (Exception e) {
			metricValue = null;
		}
		return metricValue;
	}

	public Long getNumMetric() {
		return getNumMetric(Constant.MetricEnum.PQ_MEAN);
	}

	@Override
	public String getMetricNameID() {
		return "cf_PqMean";
	}

}
