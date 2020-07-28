package CB.Metrics;

import java.io.IOException;

import CB.Resource;
import CB.TFVector;
import Output.OutPut;

import com.ecyrd.speed4j.StopWatch;

/*
 * 
 * double pd = (double) token.getValue() / (double) getFieldTokenSum(Fields.valueOf(field.name()));
			sum += pd * Math.log10(pd);
 * 
 */

public class CountSize extends AbstractMetric{

	// para fins de normalizacao:
	private double [] values;
	private int [] ids;
	int currentId;
	private double max;
	private double min;
	
	public CountSize()
	{
		currentId = 0;
		max = 0.0;
		min = Double.MAX_VALUE;
	}
	
	@Override
	public void setResource(Resource r)
	{
			resource = r;
			values = new double[r.maxDoc()];
			ids = new int[r.maxDoc()];
	}
	
	@Override
	public boolean isNormalized()
	{
		return true;
	}
	
	
	@Override
	public String getMetricNameID() {
		return "cb_CountSize";
	}

	@Override
	public void itemValues(int itemId, OutPut o, StopWatch w)
	{
		double entropy = 0.0;
		try {
			entropy = getSize(itemId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ids[currentId] = itemId;
		currentId++;
		values[itemId] = entropy;
		if (entropy > max) max = entropy;		
		if (entropy < min) min = entropy;	
		
		//writeOutput(itemId, entropy, o);
	}

@Override
public void normalize()
{
	for (int i=0; i<values.length; i++)
	{
		values[i] = (values[i] - min) / (max - min);
		writeOutput(ids[i], values[i], resource.getOutPut());
	}
}
	
double getSize(int a) throws IOException
	{ 	
    	TFVector<?> tfvs0 = resource.getTermFreqVectors(a);
    	if(tfvs0 == null) // in case of empty item
	    {
	    	return 0.0;
	    }
	    
    	int total_terms = 0;
    	for (int i=0; i<tfvs0.getNumFields(); i++) // for each field... 
	    {
    		total_terms += tfvs0.getNumTerms(i); // computing "n".
	    }

    	return total_terms;
    }

}
