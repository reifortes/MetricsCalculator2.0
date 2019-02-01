package CF.Metrics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.model.Preference;

import CF.Metrics.Constant.VersionElementMetricEnum;
import CF.Producer_Consumer.ParallelElement.RegisterElement;
import CF.Producer_Consumer.ParallelElement.ResourceElement;
import CF.Producer_Consumer.Preprocessing.RegisterPreprocessing;
import CF.Producer_Consumer.Preprocessing.ResourcePreprocessing;
import Output.OutPut;
import Output.OutPutFile;
import Output.OutPutHandler;

public class MetricHandler 
{
	protected Metric metric;
	protected OutPutHandler outPutHandler;
	private boolean doItem = false;
	private boolean doUser = false;
	private boolean doItemUser = false;
	
	public MetricHandler(Metric m)
	{
		metric = m;
	}
	
	public MetricHandler()
	{
		
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
	
	public void createOutPut(String basePath, int bufferSize, boolean useTxtOutput) throws IOException
	{
		
		outPutHandler = new OutPutHandler();
		outPutHandler.setBufferSize(bufferSize);
		outPutHandler.setParameters(doItem, doUser, doItemUser);
		outPutHandler.createOutput(basePath, metric.getMetricNameID(), useTxtOutput);
		
	}

	public void finishOutPut() throws IOException
	{
		outPutHandler.finishOutput();
	}
	
	public ResourceElement createResources(String resourceFile, int partitionLength) throws IOException, TasteException {
			System.out.println("Partition l = " + partitionLength);
			ResourceElement resource = new ResourceElement();
			String sep = "\t";
			// TODO: GET this option
		try {
			BufferedReader br = new BufferedReader(new FileReader(resourceFile));
			String line;
			String[] data;
			int count = 0;
			int countItem = 0;
			int countUser = 0;
			ArrayList<Preference> preferenceList = new ArrayList<Preference>();
			ArrayList<Preference> preferenceListItem = new ArrayList<Preference>();
			ArrayList<Preference> preferenceListUser = new ArrayList<Preference>();
			HashSet<Long> hsItem = new HashSet<Long>();
			HashSet<Long> hsUser = new HashSet<Long>();
			Preference pref;
			while ((line = br.readLine()) != null) {
				
				data = line.split(sep);
				pref = new GenericPreference(Long.parseLong(data[0]), Long.parseLong(data[1]), Float.parseFloat(data[2]));
				if (doItem || doItemUser) {
					if (countItem < partitionLength) {
						countItem = insertPreference(pref.getItemID(), pref, preferenceListItem, hsItem, partitionLength, countItem);
					} else {
						countItem = insertPreference(pref.getItemID(), pref, preferenceListItem, hsItem, partitionLength, countItem);
						insertResource(resource, VersionElementMetricEnum.ITEM_VALUE, metric, outPutHandler.getOutFileItem(), preferenceListItem.iterator());
						preferenceListItem = new ArrayList<Preference>();
					}
				}
				if (doUser || doItemUser) {
					if (countUser < partitionLength) {
						countUser = insertPreference(pref.getUserID(), pref, preferenceListUser, hsUser, partitionLength, countUser);
					} else {
						countUser = insertPreference(pref.getUserID(), pref, preferenceListUser, hsUser, partitionLength, countUser);
						insertResource(resource, VersionElementMetricEnum.USER_VALUE, metric, outPutHandler.getOutFileUser(), preferenceListUser.iterator());
						preferenceListUser = new ArrayList<Preference>();
					}
				}
				if(doItemUser) {
					if (count < partitionLength) {
						preferenceList.add(pref);
						count++;
					} else {
						preferenceList.add(pref);
						insertResource(resource, VersionElementMetricEnum.ITEM_USER_VALUE, metric, outPutHandler.getOutFileItemUser(), preferenceList.iterator());
						count = 0;
						preferenceList = new ArrayList<Preference>();
					}
				}
			}
			br.close();
			// Inserindo ultimos recursos
			if (preferenceListItem.size() > 0) insertResource(resource, VersionElementMetricEnum.ITEM_VALUE, metric, outPutHandler.getOutFileItem(), preferenceListItem.iterator());
			if (preferenceListUser.size() > 0) insertResource(resource, VersionElementMetricEnum.USER_VALUE, metric, outPutHandler.getOutFileUser(), preferenceListUser.iterator());
			if (preferenceList.size() > 0) insertResource(resource, VersionElementMetricEnum.ITEM_USER_VALUE, metric, outPutHandler.getOutFileItemUser(), preferenceList.iterator());	
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
	return resource;
	}
	
	public ResourcePreprocessing  createResourcePreprocessing(String resourceFile, int partitionLength) {
		ResourcePreprocessing resourcePreProcessing = new ResourcePreprocessing();
		OutPutFile outPutPreProcessing = null;
		try {
			
			String sep = "\t";
			
			BufferedReader br = new BufferedReader(new FileReader(resourceFile));
			String line;
			String[] data;
			int countItem = 0;
			int countUser = 0;
			ArrayList<Preference> preferenceListItem = new ArrayList<Preference>();
			ArrayList<Preference> preferenceListUser = new ArrayList<Preference>();
			HashSet<Long> hsItem = new HashSet<Long>();
			HashSet<Long> hsUser = new HashSet<Long>();
			Preference pref;
			while ((line = br.readLine()) != null) {
				data = line.split(sep);
				pref = new GenericPreference(Long.parseLong(data[0]), Long.parseLong(data[1]), Float.parseFloat(data[2]));
				if (doItem || doItemUser) {
					if (countItem < partitionLength) {
						countItem = insertPreference(pref.getItemID(), pref, preferenceListItem, hsItem, partitionLength, countItem);
					} else {
						countItem = insertPreference(pref.getItemID(), pref, preferenceListItem, hsItem, partitionLength, countItem);
						insertResourcePreprocessing(resourcePreProcessing, VersionElementMetricEnum.ITEM_VALUE, outPutPreProcessing, preferenceListItem.iterator());
						preferenceListItem = new ArrayList<Preference>();
					}
				}
				if (doUser || doItemUser) {
					if (countUser < partitionLength) {
						countUser = insertPreference(pref.getUserID(), pref, preferenceListUser, hsUser, partitionLength, countUser);
					} else {
						countUser = insertPreference(pref.getUserID(), pref, preferenceListUser, hsUser, partitionLength, countUser);
						insertResourcePreprocessing(resourcePreProcessing, VersionElementMetricEnum.USER_VALUE, outPutPreProcessing, preferenceListUser.iterator());
						preferenceListUser = new ArrayList<Preference>();
					}
				}
			}
			br.close();
			// Inserindo ultimos recursos
			if (preferenceListItem.size() > 0) insertResourcePreprocessing(resourcePreProcessing, VersionElementMetricEnum.ITEM_VALUE, outPutPreProcessing, preferenceListItem.iterator());
			if (preferenceListUser.size() > 0) insertResourcePreprocessing(resourcePreProcessing, VersionElementMetricEnum.USER_VALUE, outPutPreProcessing, preferenceListUser.iterator());
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
		return resourcePreProcessing; 
	}
	
	private static int insertPreference(long id, Preference pref, ArrayList<Preference> preferenceList, HashSet<Long> hs, int partitionLength, int count) {
		if(!hs.contains(id)) {
			preferenceList.add(pref);
			count++;
			hs.add(id);
		}
		return count;
	}
	
	private static void insertResource(ResourceElement resource, Constant.VersionElementMetricEnum version, Metric metric, OutPut outPut, Iterator<Preference> preferences) {
		RegisterElement register = new RegisterElement();
		register.setIterador(preferences);
		register.setMetric(metric);
		register.setVersion(version);
		register.setOutPut(outPut);
		resource.putRegister(register);
	}
	
	private static void insertResourcePreprocessing(ResourcePreprocessing resource, Constant.VersionElementMetricEnum version, OutPut outPut, Iterator<Preference> preferences) {
		RegisterPreprocessing register = new RegisterPreprocessing();
		register.setIterador(preferences);
		register.setVersion(version);
		register.setOutPut(outPut);
		resource.putRegister(register);
	}

	public void setParameters(boolean doI, boolean doU, boolean doIU) {
		doItem = doI;
		doUser = doU;
		doItemUser = doIU;
	}
	
}