
import java.io.File;

import java.io.IOException;


import org.apache.mahout.cf.taste.common.TasteException;


//import org.apache.log4j.BasicConfigurator;


import com.ecyrd.speed4j.StopWatch;

import CF.Metrics.*;
import CF.Metrics.Constant.MetricEnum;
import CF.Metrics.Constant.VersionElementMetricEnum;
import CF.Metrics.QualitativeMetrics.*;
import CF.Metrics.QuantitativeMetrics.*;
import CF.Producer_Consumer.ParallelElement.ConsumerElement;
import CF.Producer_Consumer.ParallelElement.RegisterElement;
import CF.Producer_Consumer.ParallelElement.ResourceElement;
import CF.Producer_Consumer.Preprocessing.ResourcePreprocessing;
import Measure.CPUTimer;
import CF.DataModel.DataModelMC;
import CF.DataModel.FileDataModelMC;
import Output.OutPut;
import Output.OutPutFile;

import com.ecyrd.speed4j.StopWatch;

public class CollaborativeCalculator {

	/*
	#id sai das preferences -> consumerElement.resource -> resource
	#preferencias do id saem de model (dataModelMC) -> PathData
	*/
	
	/* STUFF that are options or parameters: */
	private int numCores = 0;
	private String pathBase = "/home/carlos/BD/";
	private String pathData = "Sample1.txt";
	private String resourceFile = "ratings.txt";
	private String outputFolder = "";
	private String metricClassName;
	private int bufferSize = 2048;


	int partitionLength = 1000;
	
	boolean doUser = false;
	boolean doItem = false;
	boolean doItemUser = false;
	/* --- */

	// Time measurement
	private StopWatch stepwatch;
	private CPUTimer cputimer;
	
	//output type
	private boolean useTxtOutput;
	
	public CollaborativeCalculator()
	{
		
	}
	
	public void run() throws IOException, TasteException, ClassNotFoundException, InstantiationException, IllegalAccessException 
	{	
		
		stepwatch = new StopWatch("step");
		cputimer = new CPUTimer("Main");
		
		stepwatch.start();
		cputimer.start();
		
		//BasicConfigurator.configure(); // Configurando log4j: nao exibira as mensagens de erro e passara a exibir o log
		
		/*
		 * TODO: 		 * 
		 * -descobrir porque trabalhamos com dois arquivos de entrada: 
		 * >> Aparentemente usamos o resource file somente para obter os ids dos users e itens. O computo das metricas e feito sobre a base de dados criada com o outro arquivo.
		 */
		
		int numConsumidores = numCores;
		// Se o numero de consumidores for invalido, seta para o numero de threads do sistema
		if (numConsumidores <= 0) {
			numConsumidores = Runtime.getRuntime().availableProcessors();
		}
		
		
		DataModelMC datamodelmc = new FileDataModelMC(new File(pathBase+pathData));
		
		// Precisamos mesmo disso?
		OutPutFile outFileIO = null;//new OutPutFile(pathLog + "Geral.txt", "metrica; tipo; tempo", "#itemID#; #value#; #time#", 1000000, true, "NULL");
		OutPutFile outPutPreProcessing = null;// ver createResourcepreprocessing new OutPutFile(pathLog + "OutPut_Preprocessing.txt", "metrica; tipo; tempo", "#itemID#; #value#; #time#", 100000, true, "NULL");
		/* --- */
		
		//weird stuff to define the correct metric by class name:
		Class cls = Class.forName("CF.Metrics."+metricClassName);
		AbstractMetric metric = (AbstractMetric) cls.newInstance();
		metric.setModel(datamodelmc);
		/* --- */
		System.out.println("\nFound metric: " + metricClassName);
		
		if(doItem) System.out.println("doItem");
		if(doUser) System.out.println("doUser");
		if(doItemUser) 
		{
			System.out.println("doItemUser");
			doItem = true;
			doUser = true;
		}
			System.out.println(" ");
			
		if (!doItem && !doUser && !doItemUser) System.err.println("Error: No doItem, doUser or doItemUser selected");
		
		MetricHandler metricHandler = new MetricHandler(metric);
		metricHandler.setParameters(doItem, doUser, doItemUser);
		metricHandler.createOutPut(pathBase+outputFolder, bufferSize, useTxtOutput);
		
		//outFileIO = metricHandler.getOutPutHandler().getOutPutIO(); 
		stepwatch.stop();
		System.out.println("Reading and preparation time: "+ stepwatch.toString());
		stepwatch.start();
		
		if(metric.isPreprocessed())
		{
			ResourcePreprocessing rp = metricHandler.createResourcePreprocessing(pathBase+resourceFile, partitionLength);
			metric.setResourcePreprocessing(rp);	
		}
		metric.preProcessing(true, true, true); // para metricas que implementam a funcao. melhorar isso.
		
		ResourceElement resource = metricHandler.createResources(pathBase+resourceFile, partitionLength);
		ConsumerElement[] consumers = new ConsumerElement[numConsumidores];
		
		stepwatch.stop();
		System.out.println("Preprocessing time (if applicable): "+ stepwatch.toString());
		stepwatch.start();
		
		for (int i = 0; i < consumers.length; i++) 
		{
			consumers[i] = new ConsumerElement(resource, outFileIO);
			consumers[i].start();
		}
	
		try {
			resource.setFinished();
			for (int i = 0; i < consumers.length; i++)
				consumers[i].join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		metricHandler.finishOutPut();
		
		stepwatch.stop();
		System.out.println("Processing time: "+ stepwatch.toString());
		
		cputimer.stop();
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


	public String getPathData() {
		return pathData;
	}


	public void setPathData(String pathData) {
		this.pathData = pathData;
	}


	public String getResourceFile() {
		return resourceFile;
	}


	public void setResourceFile(String resourceFile) {
		this.resourceFile = resourceFile;
	}


	public int getPartitionLength() {
		return partitionLength;
	}


	public void setPartitionLength(int partitionLength) {
		this.partitionLength = partitionLength;
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


	public boolean isDoItemUser() {
		return doItemUser;
	}


	public void setDoItemUser(boolean doItemUser) {
		this.doItemUser = doItemUser;
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
	
	public void setBufferSize(int size)
	{
		bufferSize = size;
	}

	public void setTxtOutput(boolean useTxtOutput) 
	{
		this.useTxtOutput = useTxtOutput;
	}


}

