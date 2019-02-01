package CB;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;

import CB.Metrics.Metric;
import Output.OutPut;


public class Resource {

	protected int current;
	protected int max;
	protected boolean finished;
	private Metric metric;
	OutPut outPut;
	IndexReader reader;
	String readerDirectory = "";
	boolean outOfMemory = false;
		
	/* term frequency vectors */
	TFVectorHashInt[] termFreqVector;
	
	/* stuff to control integrity of fields */
	int numFields;
	boolean [] enabledFields = null;
	String[] fieldNames;
	private int idField = -1;
	
	public Resource(int size) {
		current = 0;
		max = size;
		this.finished = false;
	}

	public Metric getMetric() {
		return metric;
	}

	public void setMetric(Metric metric) {
		this.metric = metric;
	}

	public void setReader(IndexReader r, List fieldList) throws CorruptIndexException, IOException
	{
		reader = r;
		setFieldInfo(fieldList);
		setReaderDirectory();
	}

	private void setReaderDirectory() 
		{
		//String readerDir = reader.directory().toString();
		//readerDirectory = readerDir.substring(readerDir.indexOf('@')+1, readerDir.indexOf(' '))+"/";
		readerDirectory = "!!!: setReaderDirectory() not implemented";
		}

	protected void wakeup() {
		this.notify();
	}

	public synchronized int getRegister() throws Exception {
		if (current != max) {
			current++;
			if(current == max) setFinished();
			return current-1;
			}
		else {
			if (finished == false) {
				suspend();
			}
			return -1;
		}
	}

	protected synchronized void suspend() throws Exception {
		wait();
	}

	public int getNumOfRegisters() {
		return max;
	}

	public int maxDoc() {
		return max;
	}
	
	public synchronized void setFinished() {
		this.finished = true;
		this.notifyAll();
	}

	public boolean isFinished() {
		return this.finished;
	}

	public OutPut getOutPut() {
		return outPut;
	}

	public void setOutPut(OutPut outPut) {
		this.outPut = outPut;
	}

	public IndexReader getReader()
	{ 
		return reader;
	}
	
	public int getIdFieldNumber()
	{
		return idField;
	}
	
	public boolean isIdFieldNumber(int i)
	{
		return (idField == i);
	}
	
	public int getMax()
	{
		return max;
	}
	
	public void storeFrequenciesInMemory(boolean b)
	{
		outOfMemory = !b;
	}
	
	
	public Integer getId(int docn)
	{
		return getTermFreqVectors(docn).getId();
	}
	
	public long getUsedMemoryMegas()
	{
		Runtime runtime = Runtime.getRuntime();
		long usedHeap = runtime.totalMemory() - runtime.freeMemory();
		return (usedHeap)/1000000;
	}
	
	public long getFreeMemoryMegas()
	{
		Runtime runtime = Runtime.getRuntime();
		long heapMaxSize = runtime.maxMemory();
		long heapSize = runtime.totalMemory();
		long usedHeap = runtime.totalMemory() - runtime.freeMemory();
		return (heapMaxSize - usedHeap)/1000000;
	}
	
	public long getFreeMemoryPercent()
	{
		Runtime runtime = Runtime.getRuntime();
		long heapMaxSize = runtime.maxMemory();
		long heapSize = runtime.totalMemory();
		long usedHeap = runtime.totalMemory() - runtime.freeMemory();
		return ((heapMaxSize - usedHeap) *100 ) / heapMaxSize;
	}

	//Essa funcao serve para preparar as informações sobre fields, para posteriormente testar a integridade (numero correto de fields) dos dados recuperados do reader.
	public void setFieldInfo(List<Integer> fieldList) throws CorruptIndexException, IOException
	{
		Fields allFields = MultiFields.getFields(reader);
		
		numFields = allFields.size();
		
		System.out.println("got "+numFields+" from MultiFields.getFields(reader)");
		
		if (numFields <0 )  // nao conseguiu por motivo desconhecido ler o numero de fields.
		{
			numFields = 0;
			for (String field : allFields)
			{
				numFields ++;
			}
			
		}
		
		
		setEnabledFields(numFields, fieldList);
		
		fieldNames = new String[numFields];
			

		int i = 0;
		for(String fieldName : allFields)
		{
			fieldNames[i] = fieldName;
			if (fieldNames[i].equals("_id")) { idField = i; }
			
			//System.out.println(fieldNames[i]);
			i++;
		}
	}
	

	
	private void setEnabledFields(int numFields, List<Integer> fieldList) 
	{
		System.out.println("num_fields="+numFields);
		System.out.println("field list size="+fieldList.size());
		if (fieldList == null || fieldList.size() == 0)
		{
			return;
		}
		enabledFields = new boolean[numFields];
		for (int i =0; i<numFields; i++)
		{
			enabledFields[i] = false;
		}
		Iterator<Integer> it = fieldList.iterator();
		while(it.hasNext())
		{
			enabledFields[it.next()] = true;
		}
	}

	//3598
	/* stuff for managing the term freqs vector */
	public void prepareTermFreqs() throws IOException {		
		
		TFVectorHashInt.init(reader);
		boolean lowMemoryFlag = false;
		long maxMemoryConsumption = 0;
		termFreqVector = new TFVectorHashInt[max];
		
		for (int i=0; i<max; i++)
		{
			if(!outOfMemory)
			{
				try
				{

					termFreqVector[i] = new TFVectorHashInt(reader.getTermVectors(i), enabledFields);
					termFreqVector[i].checkAndFixFields(numFields, fieldNames);
					//System.out.println("Prep id "+termFreqVector[i].getTerms(idField)[0]); 
				}
				catch (OutOfMemoryError e)
				{
					outOfMemory = true;
					System.err.println("Out of memory at ("+100*i/max+"%) of preparing TermFreq vectors, switching the rest to disk.");
					termFreqVector[i] = null;
					termFreqVector[i-1] = null;
					termFreqVector[i-2] = null;
					termFreqVector[i-3] = null;
					termFreqVector[i-4] = null;
				}
				if (i%100 == 0) //TODO: switch to percentage or find a better way to measure this
				{ 
					long usedMemory = getUsedMemoryMegas();
					if (usedMemory > maxMemoryConsumption)
						maxMemoryConsumption = usedMemory;
				}
				if (i%1000 == 0) //TODO: switch to percentage or find a better way to measure this
				{ 
					//System.out.println("Prep done with "+i+" ("+100*i/max+"%)");
					//System.out.println("Heap free size = " + getFreeMemoryMegas() +"M");
					if (getFreeMemoryMegas() < 1000) // TODO: Check what is the best value for this
					{
						lowMemoryFlag = true;
					}
				}
				if (lowMemoryFlag)
				{
					//System.out.println("Heap free size = " + getFreeMemoryPercent() +"%, "+getFreeMemoryMegas()+"M");
					if (getFreeMemoryMegas() < 500) // TODO: Check what is the best value for this
					{
						System.out.println("Warning: Running out of memory ("+getFreeMemoryPercent()+"% free) at "+100*i/max+"% of preparing TermFreq vectors, switching the rest to disk.\n This may result in very slow processing and wrong results if the ordering is ignored");
						outOfMemory = true;
					}
				}
			}
			else
			{
				termFreqVector[i] = null;
				/* DISK STUFF */
				/*
				TFVectorDisk diskTFV = new TFVectorDisk(reader.getTermFreqVectors(i));
				diskTFV.checkAndFixFields(numFields, fieldNames);
				//System.out.println("Prep id "+termFreqVector[idField].getTerms(3)[0]);
				
				String pathStr = readerDirectory+"disk/"+(i%1000);
				
				File file = new File(pathStr);
				if (! (file.exists()))
				{
				    file.mkdir();
				}
				
				String fileName = pathStr+"/"+i+".tfv";
				// Write to disk with FileOutputStream
				//System.out.println(fileName);
				FileOutputStream f_out = new FileOutputStream(fileName);

				// Write object with ObjectOutputStream
				ObjectOutputStream obj_out = new ObjectOutputStream (f_out);

				// Write object out to disk
				obj_out.writeObject ( diskTFV );
				
				obj_out.close();
				/* -- */	
			}
			
		}
		if(!outOfMemory)
			TFVectorHashInt.clearMap(); // libera a memoria do hashMap String => int caso todos os vectors ja tenham sido computados
		/* --- */
		System.out.println(TFVectorHashInt.numTerms()+" terms.");
		System.out.println("Term freqs done");
		System.out.println("Max used memory for preparation:"+maxMemoryConsumption);
	}
	
	public TFVector getTermFreqVectors(int n)
	{
		if(termFreqVector[n] != null) 
			/* reading freqs from the memory: */
			return termFreqVector[n];
		else
		{ 
			/* READ FROM DISK */	
			/*
			String pathStr = readerDirectory+"disk/"+(n%1000);		
			String fileName = pathStr+"/"+n+".tfv";
			
			// Read from disk using FileInputStream
			FileInputStream f_in = null;
			try {
				f_in = new FileInputStream(fileName);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// Read object using ObjectInputStream
			ObjectInputStream obj_in = null;
			try {
				obj_in = new ObjectInputStream (f_in);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Read an object
			Object obj = null;
			try {
				obj = obj_in.readObject();
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				f_in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (obj instanceof TFVectorDisk)
			{
				// Cast object to a Vector
				TFVectorDisk vec = (TFVectorDisk) obj;
				return vec;
			}
			System.err.println("Nao li seu arquivo");
			return null;
			/* ------ */
			
			
			/* reading freqs using the index reader: */	
			TFVectorHashInt termFreqVector = null;
			try {
				termFreqVector = new TFVectorHashInt(reader.getTermVectors(n), enabledFields);
			} catch (IOException e) {
				e.printStackTrace();
			}		
			termFreqVector.checkAndFixFields(numFields, fieldNames);
			return termFreqVector;
			/* --- */
		}
	}

	
	

	
}
