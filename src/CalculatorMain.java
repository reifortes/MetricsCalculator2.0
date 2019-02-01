
import org.apache.mahout.cf.taste.common.TasteException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import java.io.*;

public class CalculatorMain {

	static boolean globalDoUser = false;
	static boolean globalDoItem = false;
	static boolean globalDoItemUser = false;
	static String globalBasePath = "Unknown_base_path";
	static String globalResourceFile = "Unknown_resource_file";
	static int globalPartitionLength = 1000;
	static String globalOutputFolder = "Unknown_output_folder";
	static String globalDataFile = "Unknown_data_file";
	static String globalPreferenceFile = "Unknown_preference_file";
	static int globalBufferSize = 1024 * 1024 * 2;
	static String globalLuceneFolder = "index-directory";
	
	static String globalSeparator = "\\|";
	static int globalNumFields = 0;
	static int globalStartingField = 0;
	static boolean globalUseFirstLineAsTitle = true;
	static boolean useTxtOutput = false;

	/*
	 * TODO: 
	 * organizar packages multiplas metricas no mesmo "process"
	 * Medir o tempo gasto!
	 */

	public static void main(String[] args)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TasteException,
			ClassNotFoundException, InstantiationException, IllegalAccessException {
		
		
		String configFile = "/home/carlos/BD/config.xml";
		if(args.length > 0)
			configFile = args[0];
		
		/* reading xml file with the specs for the calculation */
		File inputFile = new File(configFile);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.parse(inputFile);

		/* --- global settings from file --- */
		NodeList globalVarsList = doc.getElementsByTagName("global");
		if (globalVarsList.getLength() != 0) {
			Node globalVarsNode = globalVarsList.item(0);
			Element gel = (Element) globalVarsNode;

			Node gbasePathNode = gel.getElementsByTagName("basePath").item(0);
			if (gbasePathNode != null)
				globalBasePath = gbasePathNode.getFirstChild().getNodeValue();

			Node gresourceFile = gel.getElementsByTagName("resourceFile").item(0);
			if (gresourceFile != null)
				globalResourceFile = gresourceFile.getFirstChild().getNodeValue();

			Node gpLength = gel.getElementsByTagName("partitionLength").item(0);
			if (gpLength != null)
				globalPartitionLength = Integer.parseInt(gpLength.getFirstChild().getNodeValue());

			Node goutputFolder = gel.getElementsByTagName("outputFolder").item(0);
			if (goutputFolder != null)
				globalOutputFolder = goutputFolder.getFirstChild().getNodeValue();

			Node gdataFile = gel.getElementsByTagName("dataFile").item(0);
			if (gdataFile != null)
				globalDataFile = gdataFile.getFirstChild().getNodeValue();

			Node gBufferSize = gel.getElementsByTagName("bufferSize").item(0);
			if (gBufferSize != null)
			{
				globalBufferSize = Integer.parseInt(gBufferSize.getFirstChild().getNodeValue());
			}
			else
			{
				System.err.println("No global bufferSize");
			}
				
			NodeList gdoUserNodeList = gel.getElementsByTagName("doUser");
			if (gdoUserNodeList.getLength() > 0)
				globalDoUser = true;
			else
				globalDoUser = false;

			NodeList gdoItemNodeList = gel.getElementsByTagName("doItem");
			if (gdoItemNodeList.getLength() > 0)
				globalDoItem = true;
			else
				globalDoItem = false;

			NodeList gdoItemUserNodeList = gel.getElementsByTagName("doItemUser");
			if (gdoItemUserNodeList.getLength() > 0)
				globalDoItemUser = true;
			else
				globalDoItemUser = false;
			
			NodeList guseTxtOutput = gel.getElementsByTagName("useTextOutput");
			if (guseTxtOutput.getLength() > 0)
				useTxtOutput = true;
			else
				useTxtOutput = false;
		}
		/* --- */

		NodeList processList = doc.getElementsByTagName("process");

		for (int i = 0; i < processList.getLength(); i++) {
			System.out.println("\nFound process " + (i + 1));
			Node process = processList.item(i);

			if (process.getNodeType() == Node.ELEMENT_NODE) {

				Element el = (Element) process;

				Node typeNode = el.getElementsByTagName("type").item(0);
				String typeString = typeNode.getFirstChild().getNodeValue();

				if (typeString.equals("collaborative")) {
					System.out.println("type : " + typeString);
					CollaborativeCalculator calculator = new CollaborativeCalculator();

					calculator.setTxtOutput(useTxtOutput);
					
					Node basePathNode = el.getElementsByTagName("basePath").item(0);
					if (basePathNode != null) {
						String basePathString = basePathNode.getFirstChild().getNodeValue();
						calculator.setPathBase(basePathString);
					} else {
						calculator.setPathBase(globalBasePath);
					}

					Node bufferSizeNode = el.getElementsByTagName("bufferSize").item(0);
					if (bufferSizeNode != null) {
						int bufferSizeInt = Integer.parseInt(bufferSizeNode.getFirstChild().getNodeValue());
						calculator.setBufferSize(bufferSizeInt);
					} else {
						calculator.setBufferSize(globalBufferSize);
					}
					
					Node metricNode = el.getElementsByTagName("metric").item(0);
					String metricString = metricNode.getFirstChild().getNodeValue();
					calculator.setMetricClassName(metricString);

					NodeList numThreadsNodeList = el.getElementsByTagName("numThreads");
					if (numThreadsNodeList.getLength() > 0) {
						Node numThreadsNode = numThreadsNodeList.item(0);
						String numThreadsString = numThreadsNode.getFirstChild().getNodeValue();

						int numThreadsInt = Integer.parseInt(numThreadsString);
						calculator.setNumCores(numThreadsInt);
					}
					boolean di, du, diu;

					NodeList doUserNodeList = el.getElementsByTagName("doUser");
					if (doUserNodeList.getLength() > 0)
						du = true;
					else
						du = false;

					NodeList doItemNodeList = el.getElementsByTagName("doItem");
					if (doItemNodeList.getLength() > 0)
						di = true;
					else
						di = false;

					NodeList doItemUserNodeList = el.getElementsByTagName("doItemUser");
					if (doItemUserNodeList.getLength() > 0)
						diu = true;
					else
						diu = false;

					if (du == false && di == false && diu == false) {
						calculator.setDoItemUser(globalDoItemUser);
						calculator.setDoUser(globalDoUser);
						calculator.setDoItem(globalDoItem);
					} else {
						calculator.setDoItemUser(diu);
						calculator.setDoUser(du);
						calculator.setDoItem(di);
					}

					Node itemsNode = el.getElementsByTagName("items").item(0);
					Element itemsEl = (Element) itemsNode;
					NodeList itemList = itemsEl.getElementsByTagName("item");

					for (int j = 0; j < itemList.getLength(); j++) {
						Element itemElement = (Element) itemList.item(j);

						Node dataFileNode = itemElement.getElementsByTagName("dataFile").item(0);
						if (dataFileNode != null) {
							String dataFileString = dataFileNode.getFirstChild().getNodeValue();
							calculator.setPathData(dataFileString);
						} else {
							calculator.setPathData(globalDataFile);
						}

						Node resourceFileNode = itemElement.getElementsByTagName("resourceFile").item(0);
						if (resourceFileNode != null) {
							String resourceFileString = resourceFileNode.getFirstChild().getNodeValue();
							calculator.setResourceFile(resourceFileString);
						} else {
							calculator.setResourceFile(globalResourceFile);
						}

						Node outputFolderNode = itemElement.getElementsByTagName("outputFolder").item(0);
						if (outputFolderNode != null) {
							String outputFolderString = outputFolderNode.getFirstChild().getNodeValue();
							calculator.setOutputFolder(outputFolderString);
						} else {
							calculator.setOutputFolder(globalOutputFolder);
						}

						NodeList partitionLengthNodeList = el.getElementsByTagName("partitionLength");
						if (partitionLengthNodeList.getLength() > 0) {
							Node partitionLengthNode = partitionLengthNodeList.item(0);
							String partitionLengthString = partitionLengthNode.getFirstChild().getNodeValue();
							int partitionLengthInt = Integer.parseInt(partitionLengthString);
							calculator.setPartitionLength(partitionLengthInt);
						} else {
							calculator.setPartitionLength(globalPartitionLength);
						}

						System.out.println("run...");
						calculator.run();
					}
				} else if (typeString.equals("content-based")) {
					System.out.println("type : " + typeString);
					ContentBasedCalculator calculator = new ContentBasedCalculator();

					calculator.setTxtOutput(useTxtOutput);
					
					// Global parameters:
					Node basePathNode = el.getElementsByTagName("basePath").item(0);
					if (basePathNode != null) {
						String basePathString = basePathNode.getFirstChild().getNodeValue();
						calculator.setPathBase(basePathString);
					} else {
						calculator.setPathBase(globalBasePath);
					}

					Node bufferSizeNode = el.getElementsByTagName("bufferSize").item(0);
					if (bufferSizeNode != null) {
						int bufferSizeInt = Integer.parseInt(bufferSizeNode.getFirstChild().getNodeValue());
						calculator.setBufferSize(bufferSizeInt);
					} else {
						calculator.setBufferSize(globalBufferSize);
					}
					
					Node luceneFolderNode = el.getElementsByTagName("indexFolder").item(0);
					if (luceneFolderNode != null) {
						String luceneFolderString = luceneFolderNode.getFirstChild().getNodeValue();
						calculator.setPathLucene(luceneFolderString);
					} else {
						calculator.setPathLucene(globalLuceneFolder);
					}
					
					//store term frequencies in memory or not (leaving freqs in disk results in very slow operation)
					boolean storeFreqsInMemory = true;
					Node freqsInMemoryNode = el.getElementsByTagName("storeFrequenciesInMemory").item(0);
					if(freqsInMemoryNode != null) {
						storeFreqsInMemory = Boolean.parseBoolean(freqsInMemoryNode.getFirstChild().getNodeValue());
					}
					calculator.storeFreqsInMemory(storeFreqsInMemory);
					
					// check if it is an indexing process
					boolean isIndex = false;
					NodeList indexNodeList = el.getElementsByTagName("index");
					if(indexNodeList.getLength() > 0) isIndex = true;
					
					boolean di, du;

					NodeList doUserNodeList = el.getElementsByTagName("doUser");
					if (doUserNodeList.getLength() > 0)
						du = true;
					else
						du = false;

					NodeList doItemNodeList = el.getElementsByTagName("doItem");
					if (doItemNodeList.getLength() > 0)
						di = true;
					else
						di = false;

					if (du == false && di == false) {
						calculator.setDoUser(globalDoUser);
						calculator.setDoItem(globalDoItem);
					} else {
						calculator.setDoUser(du);
						calculator.setDoItem(di);
					}
					
					if(isIndex)
					{
						calculator.setIndex(true);
						
						//Indexing parameters:
						System.out.println("Lucene index process");
						
						Node preferenceFileNode = el.getElementsByTagName("userPreferenceFile").item(0);
						if (preferenceFileNode != null) {
							String preferenceFileString = preferenceFileNode.getFirstChild().getNodeValue();
							calculator.setPreferenceFile(preferenceFileString);
						} else {
							calculator.setPreferenceFile(globalPreferenceFile);
						}
						
						Node resourceFileNode = el.getElementsByTagName("resourceFile").item(0);
						if (resourceFileNode != null) {
							String resourceFileString = resourceFileNode.getFirstChild().getNodeValue();
							calculator.setResourceFile(resourceFileString);
						} else {
							calculator.setResourceFile(globalResourceFile);
						}
						
						Node sepNode = el.getElementsByTagName("separatorCharacter").item(0);
						if (sepNode != null) {
							String sepString = sepNode.getFirstChild().getNodeValue();
							calculator.setSeparator(sepString);
						} else {
							calculator.setSeparator(globalSeparator);
						}
						
						NodeList startingFieldNodeList = el.getElementsByTagName("startingField");
						if (startingFieldNodeList.getLength() > 0) {
							Node startingFieldNode = startingFieldNodeList.item(0);
							String startingFieldString = startingFieldNode.getFirstChild().getNodeValue();
							int startingFieldInt = Integer.parseInt(startingFieldString);
							calculator.setStartingField(startingFieldInt);
						} else {
							calculator.setNumFields(globalNumFields);
						}
						
						NodeList numFieldsNodeList = el.getElementsByTagName("numFields");
						if (numFieldsNodeList.getLength() > 0) {
							Node numFieldsNode = numFieldsNodeList.item(0);
							String numFieldsString = numFieldsNode.getFirstChild().getNodeValue();
							int numFieldsInt = Integer.parseInt(numFieldsString);
							calculator.setNumFields(numFieldsInt);
						} else {
							calculator.setNumFields(globalNumFields);
						}
						
						NodeList userThresholdNodeList = el.getElementsByTagName("userPreferenceThreshold");
						if (userThresholdNodeList.getLength() > 0) {
							Node thresholdNode = userThresholdNodeList.item(0);
							String thresholdString = thresholdNode.getFirstChild().getNodeValue();
							double thresholdDouble = 0.0;
							if (thresholdString.charAt(thresholdString.length() - 1) == '%')
							{
								thresholdDouble = Double.parseDouble(thresholdString.substring(0, thresholdString.length()-1));
								calculator.setUserPreferenceThreshold(thresholdDouble, true);
							}
							else 
							{
								thresholdDouble = Double.parseDouble(thresholdString);
								calculator.setUserPreferenceThreshold(thresholdDouble, false);
							}
							
						} else {
							calculator.setUserPreferenceThreshold(0.5);
						}
						
						NodeList titleNodeList = el.getElementsByTagName("useFirstLineAsTitle");
						if (titleNodeList.getLength() > 0) {
							Node titleNode = titleNodeList.item(0);
							String titleString = titleNode.getFirstChild().getNodeValue();
							int titleInt = Integer.parseInt(titleString);
							calculator.setFirstLineTitle(titleInt);
						} else {
							calculator.setFirstLineTitle(globalUseFirstLineAsTitle);
						}

						
						
					}
					
					else if(!isIndex)
					{
						// Fields
						
						NodeList fieldNodes = el.getElementsByTagName("field");
						for (int o=0; o<fieldNodes.getLength(); o++)
						{
							Node fNode = fieldNodes.item(o);
							String fString = fNode.getFirstChild().getNodeValue();
							
							int fInt = Integer.parseInt(fString);
							calculator.addField(fInt-1);
						}
						
						
						// Metric parameters:
						Node metricNode = el.getElementsByTagName("metric").item(0);
						String metricString = metricNode.getFirstChild().getNodeValue();
						calculator.setMetricClassName(metricString);
						System.out.println("Metric: " + metricString);
	
						NodeList numThreadsNodeList = el.getElementsByTagName("numThreads");
						if (numThreadsNodeList.getLength() > 0) 
						{
							Node numThreadsNode = numThreadsNodeList.item(0);
							String numThreadsString = numThreadsNode.getFirstChild().getNodeValue();
	
							int numThreadsInt = Integer.parseInt(numThreadsString);
							calculator.setNumCores(numThreadsInt);
						}
	
						Node outputFolderNode = el.getElementsByTagName("outputFolder").item(0);
						if (outputFolderNode != null) 
						{
							String outputFolderString = outputFolderNode.getFirstChild().getNodeValue();
							calculator.setOutputFolder(outputFolderString);
						} 
						else 
						{
							calculator.setOutputFolder(globalOutputFolder);
						}
						
						NodeList parameterList = el.getElementsByTagName("metricParameter");
						
						for (int j = 0; j < parameterList.getLength(); j++) 
						{
							System.out.println("Found parameter " + (j + 1));
							Node parameterNode = parameterList.item(j);
							
							if (parameterNode.getNodeType() == Node.ELEMENT_NODE) 
							{
								Element parameterElement = (Element) parameterNode;
								String idString = null;
								String valueString = null;
								
								Node idNode = parameterElement.getElementsByTagName("id").item(0);
								if (idNode != null) 
								{
									idString = idNode.getFirstChild().getNodeValue();
								}
								
								Node valueNode = parameterElement.getElementsByTagName("value").item(0);
								if (valueNode != null) 
								{
									valueString = valueNode.getFirstChild().getNodeValue();	
								}
						
								calculator.setMetricParameter(idString, valueString);
							}
						}
						
						
						if (outputFolderNode != null) {
							String outputFolderString = outputFolderNode.getFirstChild().getNodeValue();
							calculator.setOutputFolder(outputFolderString);
						} else {
							calculator.setOutputFolder(globalOutputFolder);
						}
					}
					
					System.out.println("run...");
					calculator.run();
					
				} else {
					System.err.println("Unknown metric type : " + typeString);
				}
			}
			System.gc();
		}
		System.out.println("END");
	}

}
