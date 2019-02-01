package CB;

import java.io.IOException;

import org.apache.lucene.index.Fields;

/*
 * 
 * A classe contem um array de termFrequencyVectors. Um por field.
 */

public interface TFVector<TermType extends Comparable<?>>
	{
	    public void setTermFrequencyVectors(Fields docFields, boolean [] enabledFields) throws IOException; //eFields = null means all enabled
		public int getNumFields();
		public TermType[] getTerms(int fieldId);
		public TermType getTerm(int fieldId, int position);
		public int getNumTerms(int idField);
		public int[] getTermFrequencies(int fieldId);
 		public int getFrequency(int fieldId, TermType term);
 		public String getField(int fieldId);
 		public int getId();
 		public int intersection(@SuppressWarnings("rawtypes") TFVector tfvs1, int idField);
	}
