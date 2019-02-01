package CB.Metrics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ecyrd.speed4j.StopWatch;

import CB.TFVector;
import Output.OutPut;

public class Cosine extends AbstractMetric implements SimilarityMetric{


	@Override
	public String getMetricNameID() {
		return "Cosine";
	}

	
	@Override
	public void itemValues(int itemId, OutPut o, StopWatch watch) {
		double cosSum = 0.0;
		//for (int i=itemId; i<resource.maxDoc(); i++) // removido: isto fazia o cosseno de todos com todos. Agora fazemos a média.
		for (int i=0; i<resource.maxDoc(); i++)
		{
			if(i==itemId) continue;
			
			if (watch!=null) watch.start();
			try {
				cosSum += getCosine(itemId, i);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (watch!=null) watch.stop();
			//System.out.println(itemId + " - " + i + watch.toString());
			

			//writeOutput(itemId, i, cos, o);	// removido: isto fazia o cosseno de todos com todos. Agora fazemos a média.
	
			
		}
		
		double cosAverage = cosSum / (resource.maxDoc()-1); //-1 because we skip i==itemid
		//System.out.println("cosene item "+itemId+", average "+cosAverage);
		writeOutput(itemId, cosAverage, o);
	}

	double getCosine(int a, int b) throws IOException{ 	//try to calculate cosine
		//System.out.println("cosine");
		double cosine;
	
    	TFVector tfvs0 = resource.getTermFreqVectors(a);
	    TFVector tfvs1 = resource.getTermFreqVectors(b);
	    
	    int dotProduct = 0;
	    int squareSum0 = 0;
	    int squareSum1 = 0;
	    

	    if(tfvs0 == null || tfvs1 == null) // in case of empty item
	    {
	    	System.err.println("Warning: empty item "+a+" or "+b);
	    	return 0.0;
	    }
	   
	    
	    for (int i=0; i<tfvs0.getNumFields() && i<tfvs1.getNumFields(); i++) // for each common field... 
	    {
	    	
	    	if (resource.isIdFieldNumber(i)) continue; // skip id field, we don't want it in the calculations
	    	
	    	Comparable[] keys0 = tfvs0.getTerms(i);            // keys0 = terms of doc a
	    	int[] freqs0 = tfvs0.getTermFrequencies(i);	 // freqs0 = frequencies of doc a
	    		    	
	    	//String[] keys1 = tfvs1.getTerms(i);			 // keys1 = terms of doc b ---> not needed
	    	int[] freqs1 = tfvs1.getTermFrequencies(i);    // freqs1 = frequencies of doc b
	    	 
	    	for (int j=0; j<keys0.length; j++)				 // for each term of doc a
	    	{
	    		//System.err.println("ooooh");
	    		int freq1 = tfvs1.getFrequency(i, keys0[j]);	//get frequency for that term in doc b
	    		dotProduct = dotProduct + freq1 * freqs0[j];		//multiply those two frequencies and add to a sum	      
	    	}
	    	for (int k=0; k<freqs0.length; k++) // sum the square of all the frequencies in a
	    	{
	    		squareSum0 += freqs0[k] * freqs0[k];  
	    	}
	    	for (int k=0; k<freqs1.length; k++)
	    	{
	    		squareSum1 += freqs1[k] * freqs1[k]; // sum the square of all the frequencies in b
	    	}
	    		
	    }
	    
	    
	    if(squareSum0 == 0)
	    {
	    	//System.err.println("Warning: empty document "+tfvs0.getId());
	    	return 0.0;
	    }
	    if(squareSum1 == 0)
	    {
	    	//System.err.println("Warning: empty document "+tfvs1.getId());
	    	return 0.0;
	    }
	    
	   
	    cosine = (double)dotProduct / java.lang.Math.sqrt(squareSum0 * squareSum1); // A.B / sqrt( ||A|| * ||B|| ) see cosine definition
	    
	    if (cosine > 1.0 || cosine !=cosine) // this means explosion of the double type on the multiplication. The method below is more expensive and less precise.
	    {
	    	cosine = (double)dotProduct / (java.lang.Math.sqrt(squareSum0) * java.lang.Math.sqrt(squareSum1)); // A.B / sqrt( ||A|| * ||B|| ) see cosine definition
	    }
	    if (cosine > 1.0 || cosine != cosine)
	    {
	    	//if (cosine > 1.1 || cosine != cosine)
	    	{
		    	System.err.println("Error: cosine ("+a+", "+b+") = "+cosine);
		    	System.err.println("dot Product: "+dotProduct);
		    	System.err.println("Square sum A: "+squareSum0);
		    	System.err.println("Square sum B: "+squareSum1);
		    	System.err.println("srqt (Square sum A): "+java.lang.Math.sqrt(squareSum0));
		    	System.err.println("sqrt (Square sum B): "+java.lang.Math.sqrt(squareSum1));
		    	//System.err.println("A:\n"+tfvs0.toString());
		    	//System.err.println("A:\n"+tfvs1.toString());
	    	}
	    }
		
	    //System.out.println("Cos("+tfvs0.getId()+","+tfvs1.getId()+") = "+cosine);
	    return cosine;
	    
    }


	@Override
	public double similarity(int idA, int idB) throws IOException {
		return getCosine(idA, idB);
	}


}
