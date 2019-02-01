package CB.Metrics;


import com.ecyrd.speed4j.StopWatch;

import CB.Resource;
import CB.TFVector;
import Output.OutPut;


public abstract class AbstractMetric implements Metric
{
	protected Resource resource;
	

	public void setParameter(String id, String value)
	{
		
	}
	
	public AbstractMetric()
	{
		super();
	}
	
	public AbstractMetric(Resource r)
	{
		super();
		resource = r;
	}
	
	public abstract void itemValues(int itemId, OutPut o, StopWatch watch);
	
	public void itemValues(int itemId, OutPut o)
	{
		itemValues(itemId, o, null);
	}
	
	public void writeOutput(int docIdA, int docIdB, double value, OutPut o, StopWatch watch)
	{
		writeOutput(docIdA, docIdB, value, o);
	}
	
	public void writeOutput(int docIdA, int docIdB, double value, OutPut o)
	{
		long ida = 0;
		long idb = 0;
		ida = resource.getId(docIdA);
		idb = resource.getId(docIdB);;
		o.setValue(ida, idb, value);
	}
	
	public void writeOutput(int docIdA, double value, OutPut o)
	{
		long ida = 0;
		ida  = resource.getId(docIdA);
		o.setValue(ida, value);
	}
	
	public String getMetricNameId()
	{
		return "Unknown";
	}
	
	//old
	public int intersection(TFVector a, TFVector b, int idField) // Arrays must be ordered! lucene gives them ordered
	{
		int posA =0;
		int posB =0;
		int count = 0;
		
		
		while (posA != a.getNumTerms(idField) && posB !=b.getNumTerms(idField))
		{
			int comparison = a.getTerm(idField, posA).compareTo(b.getTerm(idField, posB));
			//System.out.println("comparison:"+comparison);
			if(comparison == 0)
			{
				count++;
				posA++;
				posB++;
			}
			else if(comparison < 0) // a is smaller
			{
				posA++;
			}
			else if(comparison > 0) // b is smaller
			{
				posB++;
			}
		}
		
	
	return count;	
	}
	
	//old 
	public int getFrequency(String[] k, int[] v, String key) // Arrays must be ordered! lucene gives them ordered (deixei pois nao utilizamos mais esta funcao)
	{
		// TODO transformar isto em busca binaria
		for (int i=0; i<k.length; i++)	
		{
			int comparison = key.compareTo(k[i]);
			if (comparison == 0) return v[i];
			if (comparison < 0) break;
		}
	return 0;
	}
	
	public boolean isNormalized()
	{
		return false;
	}
	public void normalize()
	{
		return;
	}
	
	/*
	public void setReader(IndexReader reader2) {
		reader = reader2;
	}
	*/
	
	@Override
	public void setResource(Resource r)
	{
		resource = r;
	}
	
}
