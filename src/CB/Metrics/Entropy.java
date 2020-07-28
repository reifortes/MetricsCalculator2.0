package CB.Metrics;

import java.io.IOException;

import CB.Resource;
import CB.TFVector;
import Output.OutPut;

import com.ecyrd.speed4j.StopWatch;
//import org.apache.lucene.index.TermFreqVector;

/*
 * 
 * double pd = (double) token.getValue() / (double) getFieldTokenSum(Fields.valueOf(field.name()));
			sum += pd * Math.log10(pd);
 * 
 */

public class Entropy extends AbstractMetric{

	// para fins de normalizacao:
	private double [] values;
	private int [] ids;
	int currentId;
	private double max;
	private double min;
	
	public Entropy()
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
		return "cb_Entropy";
	}

	@Override
	public void itemValues(int itemId, OutPut o, StopWatch w)
	{
		double entropy = 0.0;
		try {
			entropy = getEntropy(itemId);
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
	
double getEntropy(int a) throws IOException
	{ 	
    	
		double sum = 0.0;
    	TFVector<?> tfvs0 = resource.getTermFreqVectors(a);
    	if(tfvs0 == null) // in case of empty item
	    {
	    	//System.err.println("Warning: empty item "+a+" or "+b);
	    	return 0.0;
	    }
	    
    	//int total_terms = 0;
    	double freq_sum = 0;
    	for (int i=0; i<tfvs0.getNumFields(); i++) // for each field... 
	    {
    		if (resource.isIdFieldNumber(i)) continue; // skip id field, we don't want it in the calculations
    		
    		//total_terms += tfvs0.getNumTerms(i); // computing "n".
    		
	    	int[] freqs = tfvs0.getTermFrequencies(i); // get term frequencies array.
	    	for (int j=0; j<freqs.length; j++)
	    	{
	    		freq_sum += freqs[j]; // sum all the frequencies
	    	}
	    }

    	//double normalizador = Math.log(total_terms); // dividimos pelo documento de maxima entropia possivel.
    	
	    for (int i=0; i<tfvs0.getNumFields(); i++) 
	    { 	   	
	    	if (resource.isIdFieldNumber(i)) continue; // skip id field, we don't want it in the calculations
	    	
	    	int[] freqs = tfvs0.getTermFrequencies(i);
	
	    	for (int j=0; j<freqs.length; j++)
	    	{
	    		double px = freqs[j]/freq_sum;
	    		sum += (px * Math.log(px)) ;
	    	}	    	
	    }
	    
	  
	    
	    //System.out.println("Entropy("+tfvs0.getId()+") = "+(-1*sum));
	    // if (-1* sum  / normalizador >  1.0 ) System.err.println("Normalizacao errada: " + ( -1* sum / normalizador ));
	    
	    double result = -1 * sum; // / normalizador;
	    
	    
	    return result;
	    
	    // return Math.pow(result, 100);
	    
    }

}
