package CB;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;

public class TFVectorSimple implements TFVector<String>, java.io.Serializable{
	/*
	 *
  		Atualmente a classe so funciona se nao houverem Fields faltantes em documentos provenientes dos dados do indice.
	 * 
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*
	 * Classe Serializable cujos objetos podem ser melhor armazenados em disco em caso de falta de memoria.
	 */
	private int[][] frequencies;
	private String[][] terms;
	private int[] length;
	private int numFields;
	private String[] fields;
	private int id = -1;
	
	TFVectorSimple()
	{
		frequencies = null;
		terms = null;
		numFields = 0;
	}
	
	// metodos vazios (para compatibilidade)
	public static void init()
	{
		
	}
	public static void init(IndexReader reader) throws IOException
	{

	}
	public static void clearMap() {
		
	}
	public static String numTerms() {
		return "not implemented number of";
	}

	
	public TFVectorSimple(Fields fields, boolean [] enabledFields)
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
	
	public int getFrequency(int fieldId, String term)
	{
		//System.out.println("numFields="+numFields+", fieldId="+fieldId+", term="+term+", length="+length[fieldId]);
		if(numFields >= fieldId)
			{
				if(term == null) 
				{ 
					System.err.println("Got null term"); 
					return 0; 
				}
				for(int i=0; i<length[fieldId]; i++)
				{
					//System.out.println(terms[fieldId][i]+" == "+term+"?");
					if (terms[fieldId][i].equals(term))
					{
						//System.out.println("Freq["+fieldId+"]["+i+"] = "+frequencies[fieldId][i] );
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
		terms = new String[numFields][];
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
					terms[i] = new String[0];
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

			docTermsEnum = docTerms.iterator();
			terms[i] = new String[length[i]];
			for (int j=0; j<length[i]; j++)
			{
				docTermsEnum.next();
				terms[i][j] = docTermsEnum.term().utf8ToString();
			}
			
		}
	}
	
	public String[] getTerms(int field)
	{
		if(terms[field] != null)
			return terms[field];
		return new String[0];
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
	
	public String getTerm(int fieldId, int position)
	{
		if(terms[fieldId] != null)
			return terms[fieldId][position];
		return null;
	}
	
	public void checkAndFixFields(int fixedNumFields, String fieldNames[])
	{
		if (getNumFields() == fixedNumFields) return; // ok
		else // missing fields
		{	
			int currentNumFields = getNumFields();
			int[][] fixedFreqs = new int[fixedNumFields][];
			String[][] fixedTerms = new String[fixedNumFields][];
			int[] fixedLength = new int[fixedNumFields];
	
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
	public int intersection(@SuppressWarnings("rawtypes") TFVector b, int idField) 
	{
		int posA =0;
		int posB =0;
		int count = 0;
		
		
		while (posA != this.getNumTerms(idField) && posB !=b.getNumTerms(idField))
		{
			int comparison = this.getTerm(idField, posA).compareTo((String) b.getTerm(idField, posB));
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
