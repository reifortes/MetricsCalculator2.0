
package CB;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;

public class TFVectorHashInt implements TFVector<Integer>{
	private int[][] frequencies;
	private Integer[][] terms;
	private Map<Integer, Integer>[] termFreqMaps; // termo => posicao

	private int numFields;
	private String[] fields;
	
	private int[] length;
	private int id = -1;
	
	// for all objects:
	private static Map<String, Integer> stringMap; // As strings sao fieldName+termString para evitar ambiguidades
	private static int mapCounter = 0;
	private static boolean sortFlag = false;
	
	private static String [] staticFieldNames;
	private static int staticNumFields;
	
	public static void init()
	{
		System.err.println("Warning: use init(reader) or order all the term vectors and set fields manually");
		stringMap = new HashMap<String, Integer>();
		
	}
	
	public static void init(IndexReader reader) throws IOException
	{
		
		mapCounter = 0;
		stringMap = new HashMap<String, Integer>();
		Fields allFields = MultiFields.getFields(reader);
		
		
		
		staticNumFields = allFields.size();
		
		if (staticNumFields == -1) // nao conseguiu por motivo desconhecido ler o numero de fields.
		{
			staticNumFields = 0;
			for (String field : allFields)
			{
				staticNumFields ++;
			}
			
		}
		
		staticFieldNames = new String [staticNumFields];
		
		int i =0;
		for (String field : allFields)
		{
			staticFieldNames[i] = field;
			//System.out.println("Got field from MultiFields.getFields(reader) : "+staticFieldNames[i]);
            Terms terms = allFields.terms(field);
            TermsEnum te = terms.iterator();
            
            while(te.next() != null)
            {
            	String termStr = field+te.term().utf8ToString(); // mudar para toString para ganhar performance?. Usear BytesRef mesmo? Testar.
            	//if (!field.equals("_id")) System.out.println("Adicionando no stringMap:"+field+" : "+termStr);
            	if(stringMap.get(termStr) == null) // !!!
    			{
    				stringMap.put(termStr, mapCounter);
    				//System.out.println(mapCounter+" - "+te.term());
    				mapCounter++;
    			}
            }
            
            i++;
		}
		
	}
	
	public static int numTerms()
	{
		return mapCounter;
	}
	
	public static void clearMap()
	{
		stringMap.clear();
	}
	
	TFVectorHashInt()
	{
		termFreqMaps = null;
		frequencies = null;
		terms = null;
		numFields = 0;
	}
	
	public TFVectorHashInt(Fields fields, boolean[] enabledFields)
	{
		try 
		{
			setTermFrequencyVectors(fields, enabledFields);
		} 
		catch (IOException e) 
		{
			System.err.println("Unknown IO exception while creating TFVectorHashInt(fields)");
			e.printStackTrace();
		}
	}
	
	@Override
	public int getFrequency(int fieldId, Integer integerTerm)
	{
		if(length[fieldId]>0)
		{
			Integer result = null;
			try {
				result = termFreqMaps[fieldId].get(integerTerm);
			}
			catch (Exception e)
			{
				System.err.println("Null pointer "+fieldId+" no id "+id);
				System.err.println("Field length "+length[fieldId]);
				System.err.println("Fields:");
				for (int i=0; i<fields.length; i++) System.err.println(fields[i]);
			}
			if (result!=null) return result;
		}
		return 0;
	}
	
	public int findStaticFieldNumber(int n, String name)
	{
		if (staticFieldNames[n].compareTo(name) == 0)
		{
			return n;
		}
		else
		{
			for (int i=0; i<staticFieldNames.length; i++)
			{
				if(staticFieldNames[i].compareTo(name) == 0) return i;
			}
		}
		System.err.println("Nao achei o field "+name+" nos staticFieldNames.");
		return -1;
		
	}
	
	public void setTermFrequencyVectors(Fields docFields, boolean [] enabledFields) throws IOException 
	{
		
		numFields = docFields.size();

		frequencies = new int[staticNumFields][];
		terms = new Integer[staticNumFields][];
		termFreqMaps = (Map<Integer, Integer>[]) new Map[staticNumFields];
		fields = new String[staticNumFields];
		length = new int[staticNumFields];
		
		Iterator<String> it = docFields.iterator();
		
		int chosenField = 0;
		int candidateField = 0;
		for (int l=0; l<numFields; l++) // for each field from doc
		{
			String field = it.next();
			
			int sfieldNumber = findStaticFieldNumber(l, field);
			
			fields[sfieldNumber] = field;	
			Terms docTerms = docFields.terms(field);
			TermsEnum docTermsEnum;
			
			docTermsEnum = docTerms.iterator();
			if (field.equals("_id"))
			{
				docTermsEnum.next();
				id = Integer.parseInt(docTermsEnum.term().utf8ToString()); 		
				// TODO: remove id fields
				//System.out.println("Doc de id "+id+" tem "+numFields+" fields.");
			}
		
				
			
			if (enabledFields!=null)
			{
				if (!enabledFields[sfieldNumber])
				{
					length[sfieldNumber] = 0;
					terms[sfieldNumber] = new Integer[0];
					frequencies[sfieldNumber] = new int[0];
					continue;
				}
			}
			
			PostingsEnum postingsEnum = null;	
			if (docTerms.size() < 1) frequencies[sfieldNumber] = null;
			frequencies[sfieldNumber] = new int [(int) docTerms.size()];
			docTermsEnum = docTerms.iterator();
			for (int j=0; j<docTerms.size(); j++)
			{
				docTermsEnum.next();
				//System.out.println("Term: "+docTermsEnum.term().utf8ToString());
				postingsEnum = docTermsEnum.postings(postingsEnum, PostingsEnum.FREQS);
				postingsEnum.nextDoc();
				frequencies[sfieldNumber][j] = postingsEnum.freq();
				//System.out.println("doc id = "+postingsEnum.docID()+" Field = "+fields[i]+" Term: "+docTermsEnum.term().utf8ToString()+", freq ="+frequencies[i][j]);
			}
	
			if (frequencies[sfieldNumber]!= null) length[sfieldNumber]=frequencies[sfieldNumber].length; else length[sfieldNumber]=0;

			String[] strTerms = new String[length[sfieldNumber]];
			docTermsEnum = docTerms.iterator();
			for (int j=0; j<length[sfieldNumber]; j++)
			{
				docTermsEnum.next();
				strTerms[j] = docTermsEnum.term().utf8ToString();
			}
			
			terms[sfieldNumber] = new Integer[strTerms.length];
			termFreqMaps[sfieldNumber] = new HashMap<Integer, Integer>(2*strTerms.length);
			for (int j=0; j<strTerms.length;j++)
			{
				Integer termInt =stringMap.get(fields[sfieldNumber]+strTerms[j]);
				if (termInt==null) // Se cairmos nesse caso teremos um processamento mais lento por causa do Arrays.sort. A funcao init ja deveria ter criado o hashMap ordenado
				{
					System.err.println("Warning: Got unexpected term '"+strTerms[j]+"', this will result in slow operation due to term vector sorting for now on");
					stringMap.put(fields[sfieldNumber]+strTerms[j], mapCounter); 
					terms[sfieldNumber][j] = mapCounter;
					termFreqMaps[sfieldNumber].put(mapCounter, frequencies[sfieldNumber][j]);
					mapCounter++;	
					sortFlag = true;
				}
				else
				{
					termFreqMaps[sfieldNumber].put(termInt, frequencies[sfieldNumber][j]);
					terms[sfieldNumber][j] = termInt;
				}
			}
			if(sortFlag) 
			{
				int auxT, auxF;
				for (int j=0; j<terms[sfieldNumber].length; j++) // TODO: Mudar para insertion sort
				{
					for (int k=0; k<terms[sfieldNumber].length-1; k++)
					{
						if(terms[sfieldNumber][k] > terms[sfieldNumber][k+1])
						{
							auxT = terms[sfieldNumber][j];
							auxF = frequencies[sfieldNumber][j];
							terms[sfieldNumber][j] = terms[sfieldNumber][j+1];
							frequencies[sfieldNumber][j] = frequencies[sfieldNumber][j+1];
							terms[sfieldNumber][j+1] = auxT;
							frequencies[sfieldNumber][j+1] = auxF;
						}
					}
				}
			}
			
			chosenField++;
			candidateField++;
			
		}
	}
	
	public Integer[] getTerms(int field)
	{
		if(terms[field] != null)
			return terms[field];
		return new Integer[0];
	}
	
	public Integer getTerm(int idField, int position)
	{
			return terms[idField][position];
	}
	
	public int[] getTermFrequencies(int idField)
	{
		if(frequencies[idField] != null)
			return frequencies[idField];
		return new int[0];
	}
	
	public int getNumFields()
	{
		return numFields;
	}
	
	public int getNumTerms(int idField)
	{
		return length[idField];
	}
	
	public void checkAndFixFields(int fixedNumFields, String fieldNames[])
	{
		if (getNumFields() == fixedNumFields) return; // ok
		else // missing fields
		{	
			int currentNumFields = getNumFields();
			int[] fixedLength = new int[fixedNumFields];
			int[][] fixedFreqs = new int[fixedNumFields][];
			Map<Integer, Integer>[] fixedTermFreqMaps = (Map<Integer, Integer>[]) new Map[fixedNumFields];
			Integer[][] fixedTerms = new Integer[fixedNumFields][];
			for (int i=0; i<fixedNumFields; i++)
			{
				for (int j=0; j<currentNumFields; j++)
				{
					if (getField(j) == fieldNames[i])
					{
						fixedLength[i]=terms[j].length;
						fixedTerms[i]=terms[j];
						fixedFreqs[i]=frequencies[j];
						fixedTermFreqMaps[i] = termFreqMaps[j];
						break;
					}
				}
			}
			fields = fieldNames;
			numFields = fixedNumFields;
			frequencies = fixedFreqs;
			length = fixedLength;
			terms = fixedTerms;
			termFreqMaps = fixedTermFreqMaps;
		}
	}
	
	
	public String getField(int i)
	{
		return fields[i];
	}

	@Override
	public int getId() 
		{
			return id;
		}

	/* TODO: Porque isto e mais lento? */
	/*
	@Override
	public int intersection(TFVector<Integer> B, int idField) 
	{
		int count = 0;
		int freq;
		
		for (int i=0; i<getNumTerms(idField); i++)
		{
			freq = B.getFrequency(idField, terms[idField][i]);
			if(freq>0) count++;
		}
		//System.out.println("Intersection("+this.getId()+","+B.getId()+") on field "+idField+" = "+count);
		return count;
	}
	/* --- */
	
	
	@Override
	public int intersection(TFVector<Integer> b, int idField) { // terms MUST be ordered
		int posA =0;
		int posB =0;
		int count = 0;
		
		int comparison;
		while (posA != this.getNumTerms(idField) && posB !=b.getNumTerms(idField))
		{
			comparison = this.getTerm(idField, posA).compareTo(b.getTerm(idField, posB));
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
	/* --- */
}


	

	
