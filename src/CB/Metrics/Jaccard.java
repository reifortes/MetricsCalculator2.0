package CB.Metrics;

import java.io.IOException;

//import org.apache.lucene.index.TermFreqVector;

import com.ecyrd.speed4j.StopWatch;

import CB.TFVector;
import Output.OutPut;

public class Jaccard extends AbstractMetric implements SimilarityMetric{


	@Override
	public String getMetricNameID() {
		return "Jaccard";
	}

	@Override
	public void itemValues(int itemId, OutPut o, StopWatch w) {
		double jacsum = 0.0;
		for (int i=0; i<resource.maxDoc(); i++)
		{
			if (i==itemId) continue;
			try {
				jacsum += getJaccard(itemId, i);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		double jacaverage = jacsum / (resource.maxDoc()-1); //-1 because we skip i==itemid
		//System.out.println("Jaccard for item "+itemId+" = "+jacaverage);
		writeOutput(itemId, jacaverage, o);
	}

	double getJaccard(int a, int b) throws IOException{ 	
    	    	
		//System.out.println("Jaccard "+a+", "+b);
		
    	TFVector tfvs0 = resource.getTermFreqVectors(a);
	    TFVector tfvs1 = resource.getTermFreqVectors(b);
	    
	    if(tfvs0 == null || tfvs1 == null) // in case of empty item
	    {
	    	//System.err.println("Warning: empty item "+a+" or "+b);
	    	return 0.0;
	    }
	   
	    int intersection = 0;
	    int total0 = 0;
	    int total1 = 0;
	    
	    for (int i=0; i<tfvs0.getNumFields() && i<tfvs1.getNumFields(); i++) // for each common field... 
	    {
	    	if (resource.isIdFieldNumber(i)) continue; // skip id field, we don't want it in the calculations

	    	total0+=tfvs0.getNumTerms(i);  
	    	total1+=tfvs1.getNumTerms(i);
	    	
	    	intersection += tfvs0.intersection(tfvs1, i);   	
	    }
	    
	    int denom = total0 + total1 - intersection;
	    if(denom == 0) return 0.0;
	    
	    double jaccard = (double)intersection / denom;
	    //System.out.println("Jac("+tfvs0.getId()+","+tfvs1.getId()+") = "+jaccard);
	    return jaccard;
    }

	@Override
	public double similarity(int idA, int idB) throws IOException {
		return getJaccard(idA, idB);
	}
	
}
