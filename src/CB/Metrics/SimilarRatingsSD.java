package CB.Metrics;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import CB.Resource;
import Output.OutPut;

import com.ecyrd.speed4j.StopWatch;

public class SimilarRatingsSD extends AbstractMetric {

	private SimilarityMetric simMetric = null;
	private double treshold = 9999.99;
	
	private boolean maiorIgual = true;
	
	@Override
	public void setParameter(String id, String value) 
	{
		//System.err.println("parameter "+id+" "+value);
		if (id.compareTo("similarityMetric") == 0)
		{
			Class<?> cls;
			try 
			{
				cls = Class.forName("CB.Metrics."+value);
				simMetric = (SimilarityMetric) cls.newInstance();
				simMetric.setResource(resource);
			} 
			catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
			} 
			catch (InstantiationException e) 
			{
				e.printStackTrace();
			} 
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
		}
		else if (id.compareTo("similarityThreshold") == 0)
		{
			if (value.startsWith("+"))
			{
				treshold = Double.parseDouble(value.replace("+", ""));
				maiorIgual = true;
			}
			else if (value.startsWith("-"))
			{
				treshold = Double.parseDouble(value.replace("-", ""));
				maiorIgual = false;	
			}
			else
			{
				treshold = Double.parseDouble(value);
				maiorIgual = true;
			}
		}
	}
	
	@Override
	public String getMetricNameID() 
	{
		return "SR_SD-" + simMetric.getMetricNameID() + (maiorIgual ? "-Abv" : "-Blw");
	}
	
	@Override
	public void itemValues(int itemId, OutPut o, StopWatch watch)
	{
		Double value = null;
		try 
		{
			value = mediaSim(itemId);
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
		writeOutput(itemId, value, o);
	}
	
	public Double mediaSim(int itemId) throws IOException
	{
		LinkedList<Double> simlist = new LinkedList<Double>();
		for (int i=0; i<resource.maxDoc(); i++)
		{
			if (i==itemId) continue;
			double sim = simMetric.similarity(itemId, i);
			if (maiorIgual && sim >= treshold)
			{
				simlist.add(sim);
			}
			else if (!maiorIgual && sim < treshold)
			{
				simlist.add(sim);
			}
		}
		return desvioPadrao(simlist);
		
	}
	
	public Double desvioPadrao(List<Double> list)
	{
		Iterator<Double> it = list.iterator();
		double sumi = 0;
		while(it.hasNext())
		{
			sumi += it.next();
		}
		double media = (double)sumi / list.size();
	
		double sumd = 0.0;
		it = list.iterator();
		double item;
		while(it.hasNext())
		{
			item = it.next();
			sumd += (media - item)* (media - item) ;
		}
		
		double variancia = sumd / list.size();
		
		return Math.sqrt(variancia);
		
	}
	
	@Override
	public void setResource(Resource r)
	{
		resource = r;
		simMetric.setResource(resource);
	}
	

}
