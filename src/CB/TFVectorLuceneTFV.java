package CB;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;


/*
 * 
 * A classe continha um array de termFrequencyVectors. Um por field.
 * Com a utilizacao do lucene 6, guardamos entao o correspondente: Fields. 
 * Essa classe foi editada por razoes de compatibilitade, caso venha ser util em algum momento

 *
 * Atualmente a classe so funciona se nao houverem Fields faltantes em documentos provenientes dos dados do indice.
 * 
 
 *
 */

public class TFVectorLuceneTFV implements TFVector<String> {
	private String[] fieldNames; 
	private Fields fields;
	private Terms[] terms;
	private int length;
	private int id = -1;
	
	public TFVectorLuceneTFV()
	{
		terms = null;
		fields = null;
		fieldNames = null;
		length = 0;
	}
	
	public TFVectorLuceneTFV(Fields tfv, boolean [] enabledFields)
	{
		try 
		{
			setTermFrequencyVectors(tfv, enabledFields);
		} 
		catch (IOException e) 
		{
			System.err.println("Unknown IO exception while creating TFVectorHashInt(fields)");
			e.printStackTrace();
		}
	}
	
	
	
	public static void init(IndexReader reader) throws IOException
	{

	}
	
	public Fields getTermFrequencyVectors() 
	{
		return fields;
	}

	public void setTermFrequencyVectors(Fields docFields, boolean [] enabledFields) throws IOException 
	{
		length = docFields.size();
		this.terms = new Terms[length];
		this.fields = docFields;
		this.fieldNames = new String[length];
		Iterator<String> it = docFields.iterator();
		for (int i=0; i<length; i++)
		{
			if (fieldNames[i].equals("_id"))
			{
				
				TermsEnum docTermsEnum = docFields.terms(fieldNames[i]).iterator();
				docTermsEnum.next();
				id = Integer.parseInt(docTermsEnum.term().utf8ToString()); 	
			}
			fieldNames[i] = it.next();
			if (enabledFields!=null)
			{
				if (!enabledFields[i])
				{
					terms[i] = null;
					continue;
				}
			}
			this.terms[i] = docFields.terms(fieldNames[i]);	
		}
	}

	public String getField(int i)
	{
		if (i >= length) return new String(); 
		return fieldNames[i];
	}
	
	public String[] getTerms(int fieldn)
	{
		try
		{	
			if (fieldn >= fields.size()) return new String[0]; 
			
			
			TermsEnum termsEnum = terms[fieldn].iterator();
			int numTerms = (int)terms[fieldn].size();
			String [] strTerms = new String[numTerms];
			for(int i=0; i<numTerms; i++)
			{	
				strTerms[i] = termsEnum.next().utf8ToString(); 
			}	
			return strTerms;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return new String[0];
		}
		
	}
	
	public int[] getTermFrequencies(int fieldn)
	{
		try
		{	
			if (fieldn >= fields.size()) return new int[0]; 
			if (terms[fieldn]==null) return new int[0];
			
			TermsEnum termsEnum = terms[fieldn].iterator();
			int numTerms = (int)terms[fieldn].size();
			int [] intFreqs = new int[numTerms];
			for(int i=0; i<numTerms; i++)
			{	
				termsEnum.next();
				PostingsEnum postings = null;
				postings = termsEnum.postings(postings, PostingsEnum.FREQS);
				postings.nextDoc();
				intFreqs[i] = postings.freq();
			}	
			return intFreqs;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return new int[0];
		}
	}
	
	public String getTerm(int fieldId, int position)
	{
		if (fieldId >= fields.size()) return new String(); 
		return getTerms(fieldId)[position];
	}
	
	
	public int getNumFields()
	{
		return fields.size();
	}
	
	public int getNumTerms(int idField) 
	{
		if (idField >= fields.size()) return 0; 
		if (terms == null) return 0;
		if (terms[idField]==null) return 0;
		try 
		{
			return (int)terms[idField].size();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return 0;
		}
	}
	
	public int length()
	{
		return getNumFields();
	}
	
	public int getFrequency(int fieldId, String key)
	{
		if (fieldId >= fields.size()) return 0; 
		try 
		{
			TermsEnum termsEnum = terms[fieldId].iterator();
			termsEnum.seekExact(new BytesRef(key));
			PostingsEnum postings = null;
			postings = termsEnum.postings(postings, PostingsEnum.FREQS);
			postings.nextDoc();
			return postings.freq();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
		return 0;
	}
	
	
	public String toString()
	{
		return "TFVectorLuceneTFV::toString() not implemented";
		/*
		String s = new String();
		for (int i=0; i<getNumFields(); i++)
		{
			if(termFrequencyVectors[i] != null)
				s+=termFrequencyVectors[i].toString()+"\n";
			else 
				s+="** missing field "+i+" **";
		}
		return s;
		*/
	}
	
	
	
	//essa funcao testa se o numero de fields de um termFreqVector esta correto (usando um outro). Se nao tiver, arruma. A ser chamada na hora de montar esses vetores. setFieldInfo tem que ter sido chamada antes.
	public void checkAndFixFields(int fixedNumFields, String fieldNames[])
	{
		if (getNumFields() == fixedNumFields) return; // ok
		else // missing fields
		{	
			int currentNumFields = getNumFields();
			
			length = fixedNumFields;
			String[] fixedFieldNames = new String[length]; 
			Terms[] fixedTerms = new Terms[length];
			
			
			for (int i=0; i<fixedNumFields; i++)
			{
				for (int j=0; j<currentNumFields; j++)
				{
					if (getField(j).equals(fieldNames[i]))
					{
						fixedFieldNames[i]=fieldNames[j];
						fixedTerms[i]=terms[j];
						break;
					}
				}
			}
		fieldNames = fixedFieldNames;
		terms = fixedTerms;
		}
	}
	
	@Override
	public int getId() 
	{
		return id;
	}
	
	
	@Override
	public int intersection(TFVector<String> b, int idField) // terms MUST be ordered
	{
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

	
	/*Apagar daqui pra baixo*/
	
	public static String numTerms() {
		return "not implemented number of";
	}

	public static void clearMap() {
		
	}


	
}

 //1347
	//9 8869 0353