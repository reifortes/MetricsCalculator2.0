package CB.Metrics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.lucene.index.IndexReader;

import CB.Resource;
import CB.Metrics.Metric;
import Output.OutPutHandler;


public class MetricHandler {
	protected Metric metric;
	protected OutPutHandler outPutHandler;
	private boolean doItem = true;
	private boolean doUser = false;
	private boolean doItemUser = false;
	private boolean doItemItem = false;
	
	
	public MetricHandler(Metric metric2)
	{
		metric = metric2;
	}
	
	public MetricHandler() {
		// TODO Auto-generated constructor stub
	}
	
	public void setMetric(Metric m)
	{
		metric = m;
	}

	public OutPutHandler getOutPutHandler()
	{
		return outPutHandler;
	}	
	public void setOutPutHandler(OutPutHandler o)
	{
		outPutHandler = o;
	}
	
	
	public void createOutPut(String basePath) throws IOException
	{
		createOutPut(basePath, 0);
	}
	
	public void createOutPut(String basePath, int bufferSize) throws IOException
	{
		createOutPut(basePath, bufferSize, false);
	}
	
	public void createOutPut(String basePath, int bufferSize, boolean outputTxt) throws IOException
	{
		outPutHandler = new OutPutHandler();
		outPutHandler.setParameters(doItem, doUser, doItemUser, doItemItem);
		outPutHandler.setBufferSize(bufferSize);
		outPutHandler.createOutput(basePath, metric.getMetricNameID(), outputTxt);
	}
	
	public void setParameters(boolean doI, boolean doU, boolean doIU, boolean doII) {
		// isto foi criado seguindo o modelo da filtragem colaborativa
		doItem = doI;
		doUser = doU;
		doItemUser = doIU;
		doItemItem = doII;
	}
	
	public void finishOutPut() throws IOException
	{
		if (metric.isNormalized())
		{
			metric.normalize();
		}
		outPutHandler.finishOutput();
	}
	
	public Resource createResources(IndexReader reader) throws IOException
	{
		return createResources(reader, true, null);
	}
	
	public Resource createResources(IndexReader reader, boolean storeFreqsInMemory, LinkedList<Integer> fieldList) throws IOException{
			Resource resource = new Resource(reader.maxDoc());
			resource.storeFrequenciesInMemory(storeFreqsInMemory);
			String sep = "\t";
			// TODO: GET this option
			if(doItem)
				resource.setOutPut(outPutHandler.getOutFileItem()); //Usamos Item ou User, pois trata-se de CB
			else if(doUser)
				resource.setOutPut(outPutHandler.getOutFileUser()); //Usamos Item ou User, pois trata-se de CB
			resource.setMetric(metric);
			resource.setReader(reader, fieldList);
			System.out.println(resource.getMax()+" items.");
			resource.prepareTermFreqs();
			
			return resource;
	}
	
}
