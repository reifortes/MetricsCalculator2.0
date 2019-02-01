package CF.Metrics.QuantitativeMetrics;

import org.apache.mahout.cf.taste.common.TasteException;

import CF.DataModel.DataModelMC;
import CF.Metrics.Constant;
import CF.Producer_Consumer.Preprocessing.ResourcePreprocessing;
import Output.OutPutFile;

public class NormalizedProportionOfCommomRatings extends ProportionOfCommomRatings {

	public NormalizedProportionOfCommomRatings() {
		super();
	}

	public NormalizedProportionOfCommomRatings(DataModelMC model, boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		super(model);
		//preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);
	}

	public NormalizedProportionOfCommomRatings(DataModelMC model) throws TasteException {
		super(model);
		//preProcessing(true, true, true);
	}

	public NormalizedProportionOfCommomRatings(DataModelMC model, OutPutFile outPutIO) throws TasteException {
		super(model, outPutIO);
		//preProcessing(true, true, true);
	}

	public NormalizedProportionOfCommomRatings(DataModelMC model, OutPutFile outPutIO, ProportionOfCommomRatings PCR) throws TasteException {
		super(model, outPutIO);
		this.hashCommomItem = PCR.hashCommomItem;
		this.hashCommomUser = PCR.hashCommomUser;
		preProcessing(true, true, true);
	}
	
	public NormalizedProportionOfCommomRatings(DataModelMC model, OutPutFile outPutIO, OutPutFile outPutPreProcessing, ResourcePreprocessing resourcePreProcessing, int numConsumidores) throws TasteException {
		super(model, outPutIO, outPutPreProcessing, resourcePreProcessing, numConsumidores);
		preProcessing(true, true, true);
	}

	/*
	@Override
	public void preProcessing(boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		int maxCommomItem = 0;
		int maxCommomUser = 0;

		// Processamento de item
		if (preProcessingItem || preProcessingItemUser) {
			LongPrimitiveIterator arrayItemIterator = getModel().getItemIDs();
			for (; arrayItemIterator.hasNext();) {
				int qtdCommomItems = hashCommomItem.get(arrayItemIterator.next());
				if (qtdCommomItems > maxCommomItem) {
					maxCommomItem = qtdCommomItems;
				}
			}
			setRpItem(maxCommomItem);
		}

		// Processamento de usuario
		if (preProcessingUser || preProcessingItemUser) {
			LongPrimitiveIterator arrayUserIterator = getModel().getUserIDs();
			for (; arrayUserIterator.hasNext();) {
				int qtdCommomUsers = hashCommomItem.get(arrayUserIterator.next());
				if (qtdCommomUsers > maxCommomUser) {
					maxCommomUser = qtdCommomUsers;
				}
			}
			setRpUser(maxCommomUser);
		}

		// Processamento de item-usuario
		if (preProcessingItemUser) {
			setRpItemUser(getRpItem() + getRpUser());
		}
	}
	*/

	@Override
	public Long getNumMetric() {
		return getNumMetric(Constant.MetricEnum.NORMALIZED_PROPORTION_OF_COMMOM_RATINGS);
	}
	
	@Override
	public boolean isPreprocessed()
	{
		return true;
	}
	
	@Override
	public String getMetricNameID() {
		return "NormalizedProportionOfCommonRatings";
	}
}
