package CB;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.index.TermFreqVector;

import com.ecyrd.speed4j.StopWatch;

import CB.Metrics.Metric;
import Measure.CPUTimer;
import Output.OutPut;

public class Processor extends Thread 
{
	Metric metric;
	IndexReader reader;
	Resource resource;
	OutPut output;
	CPUTimer timer;

	public Processor(Resource r, Metric m, IndexReader rd)
	{
		timer = new CPUTimer("P thread ");
		timer.start();
		
		resource = r;
		metric = m;
		reader = rd;
		setOutput(r.getOutPut());
	}
	
	public void setOutput(OutPut o)
	{
		output = o;
	}
	
	public synchronized void run()
	{
		long maxMemoryConsumption = 0;
		while ((resource.isFinished() == false) || (resource.getNumOfRegisters() != 0)) 
		{
			int docn = -1;
		
			try { docn = resource.getRegister(); } catch (Exception e) {e.printStackTrace(); }
			if(docn == -1) break;
			
			metric.itemValues(docn, output);
			if(docn%100==0)
				{
				long consumption = resource.getUsedMemoryMegas();
				if (consumption > maxMemoryConsumption)
					maxMemoryConsumption = consumption;
				}
			if(docn%2000 == 0) System.out.println("Done with : " + docn + ", " + docn*100/resource.getMax() + "% , " + timer.status());
		}
		
		timer.stop();
		timer.print();
		System.out.println("Processing max memory consumption: "+maxMemoryConsumption);
	}
}
