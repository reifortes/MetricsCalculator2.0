package CB.Metrics;

import java.io.IOException;

//import org.apache.lucene.index.TermFreqVector;

import com.ecyrd.speed4j.StopWatch;

import CB.TFVector;
import Output.OutPut;

public class Dice extends AbstractMetric implements SimilarityMetric{

	@Override
	public String getMetricNameID() {
		return "Dice";
	}

	@Override
	public void itemValues(int itemId, OutPut o, StopWatch w ){
		double diceSum = 0.0;
		for (int i=0; i<resource.maxDoc(); i++)
		{
			if (i==itemId) continue;
			try {
				double d = getDice(itemId, i);
				//System.out.println(d);
				diceSum += d;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		double diceAverage = diceSum / (resource.maxDoc()-1); //-1 because we skip i==itemid
		//System.out.println("Dice sum for item "+itemId+" = "+diceSum+" to be divided by "+  (resource.maxDoc()-1));
		//System.out.println("Dice for item "+itemId+" = "+diceAverage);
		writeOutput(itemId, diceAverage, o);
	}

	double getDice(int a, int b) throws IOException{ 	
    	
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
	    
	    double diceSum = 0.0;
	    
	    for (int i=0; i<tfvs0.getNumFields() && i<tfvs1.getNumFields(); i++) // for each common field
	    {	
	    	if (resource.isIdFieldNumber(i)) continue; // skip id field, we don't want it in the calculations
	    	   	
	    	total0+=tfvs0.getNumTerms(i);  
	    	total1+=tfvs1.getNumTerms(i);
	    	
	    	intersection += tfvs0.intersection(tfvs1, i);
	    }
	    
	    int totalSum = total0+total1;
	    //System.out.println(totalSum);
	    //System.out.println(intersection);
	    if(totalSum == 0) return 0.0;
	    
	    double dice = 2.0*(double)intersection /  totalSum;
	    
	    
	    //System.out.println("Dice("+tfvs0.getId()+","+tfvs1.getId()+") = "+dice);
	    //System.out.println(totalSum);
	    //System.out.println(intersection);
	    
	    return dice;
	    
    }

	@Override
	public double similarity(int idA, int idB) throws IOException {
		return getDice(idA, idB);
	}
	
}
