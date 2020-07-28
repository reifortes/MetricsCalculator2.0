package CF.Metrics.QuantitativeMetrics;

import java.util.Map;

import org.apache.mahout.cf.taste.common.TasteException;

import CF.Metrics.AbstractMetric;
import CF.Metrics.Constant;
import CF.Producer_Consumer.Preprocessing.ConsumerPreprocessing;
import CF.Producer_Consumer.Preprocessing.ResourcePreprocessing;
import CF.DataModel.DataModelMC;
import Output.OutPutFile;

public class ProportionOfCommomRatings extends AbstractMetric {

	protected double RpItem;
	protected double RpUser;
	protected double RpItemUser;
	protected Map<Long, Integer> hashCommomItem;
	protected Map<Long, Integer> hashCommomUser;
	protected OutPutFile outPutPreProcessing;
	ResourcePreprocessing resourcePreProcessing;
	protected int numConsumidores;

	public ProportionOfCommomRatings() {
		super();
	}

	@Override
	public void setResourcePreprocessing(ResourcePreprocessing rp) {
		this.resourcePreProcessing = rp;
		}
	
	public ProportionOfCommomRatings(DataModelMC model) throws TasteException {
		super(model);
		this.hashCommomItem = null;
		this.hashCommomUser = null;
		// preProcessing(true, true, true);
	}

	public ProportionOfCommomRatings(DataModelMC model, OutPutFile outPutIO) throws TasteException {
		super(model, outPutIO);
		this.hashCommomItem = null;
		this.hashCommomUser = null;
		// preProcessing(true, true, true);
	}

	public ProportionOfCommomRatings(DataModelMC model, OutPutFile outPutIO, boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		super(model, outPutIO);
		this.hashCommomItem = null;
		this.hashCommomUser = null;
		// preProcessing(preProcessingItem, preProcessingUser, preProcessingItemUser);
	}

	public ProportionOfCommomRatings(DataModelMC model, OutPutFile outPutIO, OutPutFile outPutPreProcessing, ResourcePreprocessing resourcePreProcessing, int numConsumidores) throws TasteException {
		super(model, outPutIO);
		this.hashCommomItem = null;
		this.hashCommomUser = null;
		this.outPutPreProcessing = outPutPreProcessing;
		this.resourcePreProcessing = resourcePreProcessing;
		this.numConsumidores = numConsumidores;
		preProcessing(true, true, false);
	}

	public void preProcessing(boolean preProcessingItem, boolean preProcessingUser, boolean preProcessingItemUser) throws TasteException {
		setRpItem((double)getModel().getNumItems());
		setRpUser((double)getModel().getNumUsers());
		setRpItemUser(getRpItem() + getRpUser());
		doPreProcessing();
	}
	
	public void doPreProcessing() {
		// Criando consumidores
		if (numConsumidores == 0) autoNumConsumidores();
		ConsumerPreprocessing[] consumidoresPreProcessing = new ConsumerPreprocessing[numConsumidores];
		for (int i = 0; i < consumidoresPreProcessing.length; i++) {
			consumidoresPreProcessing[i] = new ConsumerPreprocessing(model, resourcePreProcessing, outPutPreProcessing, getNumMetric());
			consumidoresPreProcessing[i].start();
		}
		
		//TODO Criar os recursos aqui

		// Aguardando preprocessamento
		try {
			resourcePreProcessing.setFinished();
			for (int i = 0; i < consumidoresPreProcessing.length; i++)
				consumidoresPreProcessing[i].join();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Referenciando resultado do preprocessamento
		this.hashCommomItem = resourcePreProcessing.getHashCommomItem();
		this.hashCommomUser = resourcePreProcessing.getHashCommomUser();
	}

	public Double itemValue(long itemID) {
		Double metricValue = 0.0;
		try {
			metricValue = ((double) hashCommomItem.get(itemID) / RpItem);
		} catch (Exception e) {
			System.err.println(e.toString());
			metricValue = null;
		}
		return metricValue;
	}

	public Double userValue(long userID) {
		Double metricValue = 0.0;
		try {
			metricValue = ((double) hashCommomUser.get(userID) / RpUser);
		} catch (Exception e) {
			System.err.println(e.toString());
			metricValue = null;
		}
		return metricValue;
	}

	public Double itemUserValue(long itemID, long userID) {
		Double metricValue = 0.0;
		try {
			metricValue = ((double) (hashCommomItem.get(itemID) + hashCommomUser.get(userID)) / (RpItem + RpItemUser));
		} catch (Exception e) {
			System.err.println(e.toString() + hashCommomItem.get(itemID));
			metricValue = null;
		}
		return metricValue;
	}

	public Long getNumMetric() {
		return getNumMetric(Constant.MetricEnum.PROPORTION_OF_COMMOM_RATINGS);
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

	@Override
	public String getMetricNameID() {
		return "cf_PCR";
	}
	
	@Override
	public boolean isPreprocessed()
	{
		return true;
	}
	
	public void autoNumConsumidores()
	{
		this.numConsumidores = Runtime.getRuntime().availableProcessors();
	}
}
