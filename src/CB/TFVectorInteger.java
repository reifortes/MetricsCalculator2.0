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
/*
 * Atualmente a classe so funciona se nao houverem Fields faltantes em documentos provenientes dos dados do indice.
 * 
 */
public class TFVectorInteger implements TFVector<Integer>{
	private int[][] frequencies;
	private Integer[][] terms;

	private int numFields;
	private String[] fields;
	
	private int[] length;
	private int id = -1;
	
	// for all objects:
	private static Map<String, Integer> stringMap;  // As strings sao fieldName+termString para evitar ambiguidades
	private static int mapCounter = 0;
	private static boolean sortFlag = false;
	
	public static void init()
	{
		System.err.println("Warning: use init(reader) or order all the term vectors manually");
		stringMap = new HashMap<String, Integer>();
		
	}
	
	public static void init(IndexReader reader) throws IOException
	{
		mapCounter = 0;
		stringMap = new HashMap<String, Integer>();
		
		Fields allFields = MultiFields.getFields(reader);
		
		for (String field : allFields)
		{
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
	
	TFVectorInteger()
	{
		frequencies = null;
		terms = null;
		numFields = 0;
	}
	
	public TFVectorInteger(Fields fields, boolean [] enabledFields)
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
	
	public int getFrequency(int fieldId, Integer integerTerm)
	{
		if(numFields >= fieldId)
			{
				if(integerTerm == null) 
				{ 
					System.err.println("Got null term"); 
					return 0; 
				}
				for(int i=0; i<length[fieldId]; i++)
				{
					if (terms[fieldId][i].compareTo(integerTerm) == 0)
					{
						return frequencies[fieldId][i];
					}
				}
			}
		return 0;
	}
	
	public void setTermFrequencyVectors(Fields docFields, boolean [] enabledFields) throws IOException 
	{
		numFields = docFields.size();
		frequencies = new int[numFields][];
		terms = new Integer[numFields][];
		fields = new String[numFields];
		length = new int[numFields];
		
		Iterator<String> it = docFields.iterator();
		for (int i=0; i<numFields; i++)
		{
			fields[i] = it.next();
			Terms docTerms = docFields.terms(fields[i]);
			TermsEnum docTermsEnum;
			
			docTermsEnum = docTerms.iterator();
			if (fields[i].equals("_id"))
			{
				docTermsEnum.next();
				id = Integer.parseInt(docTermsEnum.term().utf8ToString()); 		
				// TODO: remove id fields
			}
			
			if (enabledFields!=null)
			{
				if (!enabledFields[i])
				{
					length[i] = 0;
					terms[i] = new Integer[0];
					frequencies[i] = new int[0];
					continue;
				}
			}
			
			PostingsEnum postingsEnum = null;	
			if (docTerms.size() < 1) frequencies[i] = null;
			frequencies[i] = new int [(int) docTerms.size()];
			docTermsEnum = docTerms.iterator();
			for (int j=0; j<docTerms.size(); j++)
			{
				docTermsEnum.next();
				//System.out.println("Term: "+docTermsEnum.term().utf8ToString());
				postingsEnum = docTermsEnum.postings(postingsEnum, PostingsEnum.FREQS);
				postingsEnum.nextDoc();
				frequencies[i][j] = postingsEnum.freq();
				//System.out.println("doc id = "+postingsEnum.docID()+" Field = "+fields[i]+" Term: "+docTermsEnum.term().utf8ToString()+", freq ="+frequencies[i][j]);
			}
			if (frequencies[i]!= null) length[i]=frequencies[i].length; else length[i]=0;

			String[] strTerms = new String[length[i]];
			docTermsEnum = docTerms.iterator();
			for (int j=0; j<length[i]; j++)
			{
				docTermsEnum.next();
				strTerms[j] = docTermsEnum.term().utf8ToString();
			}
			
			terms[i] = new Integer[strTerms.length];
			for (int j=0; j<strTerms.length;j++)
			{
				Integer termInt =stringMap.get(fields[i]+strTerms[j]);
				if (termInt==null) // Se cairmos nesse caso teremos um processamento mais lento por causa do Arrays.sort. A funcao init ja deveria ter criado o hashMap ordenado
				{
					System.err.println("Warning: Got unexpected term, this will result in slow operation due to term vector sorting for now on");

					stringMap.put(fields[i]+strTerms[j], mapCounter); 
					terms[i][j] = mapCounter;
					mapCounter++;	
					sortFlag = true;
				}
				else
				{
					terms[i][j] = termInt;
				}
			}
			if(sortFlag) 
			{
				int auxT, auxF;
				for (int j=0; j<terms[i].length; j++) // TODO: Mudar para insertion sort
				{
					for (int k=0; k<terms[i].length-1; k++)
					{
						if(terms[i][k] > terms[i][k+1])
						{
							auxT = terms[i][j];
							auxF = frequencies[i][j];
							terms[i][j] = terms[i][j+1];
							frequencies[i][j] = frequencies[i][j+1];
							terms[i][j+1] = auxT;
							frequencies[i][j+1] = auxF;
						}
					}
				}
			}
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
						break;
					}
				}
			}
			fields = fieldNames;
			numFields = fixedNumFields;
			frequencies = fixedFreqs;
			length = fixedLength;
			terms = fixedTerms;
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
}
