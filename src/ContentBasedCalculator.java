
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import CB.Indexer;
import CB.Processor;
import CB.Resource;
import CB.Metrics.AbstractMetric;
import CB.Metrics.MetricHandler;
import Measure.CPUTimer;

import com.ecyrd.speed4j.StopWatch;


public class ContentBasedCalculator {
	// general parameters
	private int numCores = 0;
	String pathLucene = "lucene-index";
	String pathBase = "/home/carlos/BD/";
	boolean isIndex = false;
	boolean doItem;
	boolean doUser;
	private LinkedList<Integer> fieldList;

	// metric parameters:
	private String metricClassName;
	private String outputFolder = "";
	private int numParameters = 0;
	private ArrayList<String> parameterIds;
	private ArrayList<String> parameterValues;
	
	// Indexing parameters
	private String separator;
	private String resourceFile;
	private int startingField = 0;
	private int numFields;
	private boolean firstLineTitle;
	private int bufferSize = 2048;
	private double userPreferenceThreshold = 0.5;
	private boolean isUserPreferenceThresholdPercent = false;
	private String userPreferenceFile = "unknown_preference_file";

	// Time measurement
	private StopWatch stepwatch;
	private CPUTimer cputimer;

	//memory management:
	boolean storeFreqsInMemory = true;
	
	// output type
	private boolean useTxtOutput = true;
	
	/*TODO:
	 * partition_length?
	 */
	
	public ContentBasedCalculator()
	{
		parameterIds = new ArrayList<String>();
		parameterValues = new ArrayList<String>();
		fieldList = new LinkedList<Integer>();
		numParameters = 0;
	}
	
	public void run() throws CorruptIndexException, LockObtainFailedException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		System.out.println("\n");
		if (isIndex)
		{
			Indexer.createLuceneIndex(pathBase, resourceFile, pathLucene, getSeparator(), startingField, numFields, isFirstLineTitle(), doUser, userPreferenceFile, userPreferenceThreshold, isUserPreferenceThresholdPercent);
			return;
		}
		if(doItem) 
		{
			System.out.println("Executing content-based calculation Item x Item...");
			execution(false);
		}
		if(doUser)
		{
			System.out.println("Executing content-based calculation User x User...");
			execution(true);
		}
	}
	
	public void execution(boolean isDoUser)throws CorruptIndexException, LockObtainFailedException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		
		
		String dir = pathBase+pathLucene;
		boolean doi = true;
		boolean dou = false;
		
		if(isDoUser) // modificacoes para atender doUser
		{
			dir = pathBase+pathLucene+"/user";
			doi = false; // do item e do user. Para formatar os arquivos de saida corretamente.
			dou = true;
		}
		else
		{
			System.out.println("ITEM VALUES");
		}
		
		
		stepwatch = new StopWatch("step");
		cputimer = new CPUTimer("Main ");
		cputimer.start();
		
		stepwatch.start();
		
		int numProcessors = numCores;
		// Se o numero de consumidores for invalido, seta para o numero de threads do sistema
		if (numProcessors <= 0) {
			numProcessors = Runtime.getRuntime().availableProcessors();
		}
		
		FSDirectory directory;
		Path path = Paths.get(dir);
		directory = FSDirectory.open(path);
		IndexReader reader = DirectoryReader.open(directory);

		//weird stuff to define the correct metric by class name:
		Class<?> cls = Class.forName("CB.Metrics."+metricClassName);
		AbstractMetric metric = (AbstractMetric) cls.newInstance();
		/* --- */
		
		MetricHandler metricHandler = new MetricHandler(metric);
		metricHandler.setParameters(doi, dou, false, false);
		metricHandler.createOutPut(pathBase+outputFolder, bufferSize, useTxtOutput);
		
		Resource resource = metricHandler.createResources(reader, storeFreqsInMemory, fieldList);
		metric.setResource(resource);
		
		for(int k=0; k<numParameters; k++)
		{
			metric.setParameter(parameterIds.get(k), parameterValues.get(k));
		}
		
		stepwatch.stop();
		System.out.println("Reading and preparation time: "+ stepwatch.toString());	
		stepwatch.start();
		
		Processor[] processors = new Processor[numProcessors];
		for (int i=0; i<numProcessors; i++)
		{
			processors[i] = new Processor(resource, metric, reader);
			processors[i].start();
		}
		
		try {
			resource.setFinished();
			for (int i = 0; i < processors.length; i++)
				processors[i].join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		reader.close();

		metricHandler.finishOutPut();
		
		stepwatch.stop();
		cputimer.stop();
		
		System.out.println("Processing time: "+ stepwatch.toString());	
		cputimer.print();
		
		System.out.println("done");	
		System.out.println("\n");
		
	}
	
	
	
	public int getNumCores() {
		return numCores;
	}


	public void setNumCores(int numCores) {
		this.numCores = numCores;
	}


	public String getPathBase() {
		return pathBase;
	}

	public void setPathBase(String pathBase) {
		this.pathBase = pathBase;
	}

	public void setPathLucene(String pathLucene) {
		this.pathLucene = pathLucene;
	}


	public String getPathLucene() {
		return pathLucene;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public String getMetricClassName() {
		return metricClassName;
	}

	public void setMetricClassName(String metricClassName) {
		this.metricClassName = metricClassName;
	}



	public String getSeparator() {
		return separator;
	}



	public void setSeparator(String separator) {
		this.separator = separator;
	}



	public String getResourceFile() {
		return resourceFile;
	}



	public void setPreferenceFile(String resourceFile) {
		this.userPreferenceFile = resourceFile;
	}

	public String getPreferenceFile() {
		return userPreferenceFile;
	}



	public void setResourceFile(String resourceFile) {
		this.resourceFile = resourceFile;
	}



	public int getNumFields() {
		return numFields;
	}

	public int getStartingFields() {
		return startingField;
	}


	public void setNumFields(int numFields) {
		this.numFields = numFields;
	}
	
	public void setStartingField(int sField) {
		this.startingField = sField;
	}
	
	
	public boolean isIndex() {
		return isIndex;
	}

	public void setIndex(boolean isIndex) {
		this.isIndex = isIndex;
	}



	public void setFirstLineTitle(boolean useFirstLineAsTitle) {
		firstLineTitle = useFirstLineAsTitle;	
		}
	
	public void setFirstLineTitle(int useFirstLineAsTitle) {
		if (useFirstLineAsTitle > 0) firstLineTitle = true;
		else firstLineTitle = false;
		}

	public boolean isFirstLineTitle() {
		return firstLineTitle;
	}
	
	public void setBufferSize(int size)
	{
		bufferSize = size;
	}


	public void setUserPreferenceThreshold(double threshold) 
	{
		setUserPreferenceThreshold(threshold, false);
	}
	
	public void setUserPreferenceThreshold(double threshold, boolean percent) {
		userPreferenceThreshold = threshold;
		isUserPreferenceThresholdPercent = percent;
	}
	
	public boolean isDoUser() {
		return doUser;
	}


	public void setDoUser(boolean doUser) {
		this.doUser = doUser;
	}


	public boolean isDoItem() {
		return doItem;
	}


	public void setDoItem(boolean doItem) {
		this.doItem = doItem;
	}

	public void storeFreqsInMemory(boolean storeFreqsInMemory) {
		this.storeFreqsInMemory = storeFreqsInMemory;
		
	}

	public void setTxtOutput(boolean useTxtOutput) {
		this.useTxtOutput = useTxtOutput;
		
		}

	public void setMetricParameter(String idString, String valueString) 
	{
			parameterIds.add(idString);
			parameterValues.add(valueString);
			numParameters++;
	}

	public void addField(int f)
	{
		fieldList.add(f);
	}
	


}




