package CB;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.BytesRef;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import com.ecyrd.speed4j.*;

import CF.DataModel.DataModelMC;
import CF.DataModel.FileDataModelMC;

public class Indexer {
	
	
	public static void createLuceneIndex(String basePath,  String inFile, String relativeOutDir, String sep, int startingField, int numFields, boolean firstLineTitle) throws CorruptIndexException, LockObtainFailedException, IOException 
	{
		 createLuceneIndex(basePath,  inFile, relativeOutDir, sep, startingField, numFields, firstLineTitle, false,  "", 0.0, false);
	}
	
	public static void createLuceneIndex(String basePath,  String inFile, String relativeOutDir, String sep, int startingField, int numFields, boolean firstLineTitle, boolean doUser) throws CorruptIndexException, LockObtainFailedException, IOException 
	{
		 createLuceneIndex(basePath,  inFile, relativeOutDir, sep, startingField, numFields, firstLineTitle, doUser,  "", 0.0, false);
	}
	
	public static void createLuceneIndex(String basePath,  String inFile, String relativeOutDir, String sep, int startingField, int numFields, boolean firstLineTitle, boolean doUser,  String userPreferenceFile, double userPreferenceThreshold, boolean isUserPreferenceThresholdPercent) throws CorruptIndexException, LockObtainFailedException, IOException 
	{
		  startingField--; // first field is actually 0
		  if(startingField == -1) startingField = 0;
		  
		  StopWatch stopwatch = new StopWatch();
		  StopWatch globalwatch = new StopWatch();
		  stopwatch.start();
		
		  String outDir = basePath+relativeOutDir;		  
		  String infile = basePath+inFile;
		  String preffile = basePath+userPreferenceFile;
		
		  System.out.println("Creating lucene index for " + infile + " at " +outDir);
		  System.out.println("Using separator '"+sep+"'");
		  
		  FSDirectory indexDir;
		  Path path = Paths.get(outDir);
		  indexDir = FSDirectory.open(path);
		  
		  IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		  config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // overwrite current index
		  IndexWriter indexWriter = new IndexWriter(indexDir, config);
		  
		  Document doc;
		  
		  FieldType type = new FieldType();
		  type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		  type.setStoreTermVectorPositions(true);
		  type.setStoreTermVectorOffsets(true);
		  type.setStoreTermVectors(true);
		  type.setStored(true);

		  String separator = sep;
		  
		  List<String> lines;
		  try
		  {
			  lines = Files.readAllLines(Paths.get(infile), StandardCharsets.UTF_8);
		  }
		  catch (java.nio.charset.MalformedInputException e)
		  {
			  lines = Files.readAllLines(Paths.get(infile), StandardCharsets.ISO_8859_1);
		  }
		  
		  int count = 0;
		  String firstLine = lines.get(0);
		  String [] fieldNames;
		  
		  if(firstLineTitle)
		  {	  
			  fieldNames = firstLine.split(separator); 
			  count--;
		  }
		  else
		  {
			  int n = firstLine.split(separator).length;
			  fieldNames = new String[n]; 
			  for (int i=0; i<n; i++)
			  {
				  fieldNames[i] = "Field"+(i+1);
			  }
		  }
		  
		  if (numFields == 0)
		  {
			  numFields = fieldNames.length;
		  }
		  
		  //System.out.println(startingField);
		  //System.out.println(numFields);
		  
		  System.out.println("Fields: " + fieldNames[startingField] + " - " +fieldNames[startingField+numFields-1]);
		  
		  		  
		  for (String line : lines)
		  {  
			  if(count == -1) { count++; continue; } //skip first line.
			  doc = new Document();
			  
		      String [] parts = line.split(separator);
		      
		      doc = new Document();
		      //Field f = new IntField("_id", Integer.parseInt(parts[0]), IntField.Store.YES);  why this doesnt work?
    		  Field f = new Field("_id", parts[0], type);
    		  //Field f = new Field("_id", parts[0], Field.Store.YES,Field.Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS);  
		      
    		  doc.add(f); 
		      

		      for (int i=startingField; i<startingField+numFields; i++)
		      {
		    	  if(parts.length > i) 
		    	  { 
		    		  doc.add(new Field(fieldNames[i], parts[i], type)); 	 
		    		  //doc.add(new Field(fieldNames[i], parts[i], Field.Store.YES,Field.Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS)); 	    		  
		    		  //System.out.println("Field "+fieldNames[i]+", data: "+parts[i]);
		    	  }
		    	  else
		    	  {
		    		  doc.add(new Field(fieldNames[i], " ", type)); 
		    		  //doc.add(new Field(fieldNames[i], " ", Field.Store.YES,Field.Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS)); 
		    	  }
		      }

		      indexWriter.addDocument(doc);
		      count++;
		   }
	indexWriter.commit();
	indexWriter.close();
	
	indexDir.close();
	
	stopwatch.stop();
	System.out.println("Processed: "+count+" item entries, time:"+stopwatch.toString());
	
	
	//user index should start here
	if(doUser) 
	{
		double threshold = userPreferenceThreshold;
		boolean thresholdPercent = isUserPreferenceThresholdPercent;
		userIndex(outDir, numFields, startingField, fieldNames, preffile, threshold, thresholdPercent);
	}
	
	
	globalwatch.stop();
	System.out.println("Indexer time:"+globalwatch.toString());
	System.out.println("done.");
	}

	public static void userIndex(String itemIndexDir, int numFields, int startingField, String[] fieldNames, String userPreferenceFile, double userPreferenceThreshold, boolean isUserPreferenceThresholdPercent) throws CorruptIndexException, IOException
	{
		int count = 0;
		StopWatch stopwatch = new StopWatch();
		stopwatch.start();
		double threshold = userPreferenceThreshold;
		boolean tresholdPercent = isUserPreferenceThresholdPercent;
		String filename = userPreferenceFile;
		
		FSDirectory directory;
		Path path = Paths.get(itemIndexDir);
		directory = FSDirectory.open(path);
		IndexReader reader = DirectoryReader.open(directory);
	
		Path path2 = Paths.get(itemIndexDir+"/user");
		Directory userIndexDir = FSDirectory.open(path2);
		
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // overwrite current index
		IndexWriter indexWriter = new IndexWriter(userIndexDir, config);
		   
		FieldType type = new FieldType();
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);

		type.setStoreTermVectorPositions(true);
		type.setStoreTermVectorOffsets(true);
		type.setStoreTermVectors(true);
		
		DataModelMC datamodelmc; 
		
		try 
		{

			datamodelmc = new FileDataModelMC(new File(filename));
			LongPrimitiveIterator it = datamodelmc.getUserIDs();
			
			while (it.hasNext()) // for each user
			{
				Long uid = it.next();
				
				PreferenceArray prefs = datamodelmc.getPreferencesFromUser(uid); // get preferences from that user
				Iterator<Preference> prefiter = prefs.iterator();
				
				Document doc;
				
			    doc = new Document();
			    //Field f = new IntField("_id", Integer.parseInt(parts[0]), IntField.Store.YES);  why this doesnt work?
	    		Field f = new Field("_id", Long.toString(uid), type); 
	    		
	    		doc.add(f); 
			      
	    		
	    		Field[] fs = new Field[numFields];
	    		StringBuilder[] fsstring = new StringBuilder[numFields]; 
	    		
	    		for (int i=0; i<numFields; i++)
			    {
	    			fsstring[i] = new StringBuilder('\0');
			    }

	    		if (isUserPreferenceThresholdPercent)
				{ // o limiar e percentual sobre o desvio padrao dos ratings daquele user
	    			
	    			threshold = userPreferenceThreshold;
	    			
	    			Double pdouble;
	    			Double sum = 0.0;
	    			
	    			while (prefiter.hasNext())
	    			{
	    				Preference p = prefiter.next();
	    				pdouble = new Double(p.getValue());
	    				sum+= pdouble;
	    			}
	    			
	    			Double mean = sum / prefs.length();
	    			
	    			//System.out.println("Percent: "+threshold/100);
	    			//System.out.println("Media: "+mean);
	    			
	    			sum = 0.0;
	    			prefiter = prefs.iterator();
	    			while (prefiter.hasNext())
	    			{
	    				Preference p = prefiter.next();
	    				pdouble = new Double(p.getValue());
	    				pdouble -= mean;
	    				sum += pdouble * pdouble;
	    			}
	    			
	    			Double desviopadrao = Math.sqrt( sum / prefs.length() );
	    			
	    			//System.out.println("Desvio: "+desviopadrao);
	    			if (threshold >= 0)
	    				threshold = desviopadrao*(threshold/100.0)+mean;
	    			else
	    				threshold = desviopadrao*(threshold/100.0)-mean;
	    			
	    			//System.out.println("limiar: "+threshold);
	    			
	    			prefiter = prefs.iterator();
				}
	    		
				while(prefiter.hasNext())
				{

					Preference p = prefiter.next();
					//System.out.println("preference: "+p.getValue());
					double compare = p.getValue();
					if (threshold < 0) compare = -compare; //threshold negativo significa que usamos os itens que nao sao de preferencia
					if (compare >= threshold)
					{						
						
						IndexSearcher searcher = new IndexSearcher(reader);
						//Integer id = (int) p.getItemID();
						Query q = new TermQuery(new Term("_id", Long.toString(p.getItemID())));
						
						TopDocs td = searcher.search(q, 1);
						
						if(td.totalHits != 1) 
						{
							System.err.println("Error: Total hits for item id "+p.getItemID()+" = "+td.totalHits+" (user id "+uid+") ");
							continue;
						}
						
						ScoreDoc sd = td.scoreDocs[0];
						int hitDocNumber = sd.doc;
						//System.out.println("got doc "+hitDocNumber+" from query ");
						
						Document retrievedDocument = reader.document(hitDocNumber);
						//System.out.println("retrived id = "+retrievedDocument.get("_id"));
	
						/*uncomment to check if reading is correct*
						System.out.println("user "+uid+" likes item "+retrievedDocument.get("_id")+"(="+p.getItemID()+") = "+p.getValue());
						/**/
						
						
						List<IndexableField> fds = retrievedDocument.getFields();
						
						IndexableField fd = fds.get(0);
						//System.out.println("field 0 name :"+fd.name());
						
						int j = 0; //pra pular o id
						for (int i=startingField; i<startingField+numFields; i++) 
					    {
							if(fieldNames[i] == "_id") continue;
							fsstring[j].append(retrievedDocument.get(fieldNames[i])); // get field value
							fsstring[j].append(" "); // espaco
							j++;
							//System.out.println(fieldNames[j]+" : "+retrievedDocument.get(fieldNames[j]));
					    }

						/* --- */
					    
					    if (td.totalHits > 1) System.err.println("Error: total hits > 1 for user " + uid);
						
					}
				}	
				
				doc.add( new Field("_id", Long.toString(uid), type) );
				
				for (int i=0; i<numFields; i++)
			    {
					//if(fsstring[i].length() > 0)  System.out.println(fieldNames[i+startingField]+" = "+fsstring[i]);
			    	fs[i] = new Field(fieldNames[i+startingField], fsstring[i].toString(), type); 
			    }
				
				for(int i=0; i<numFields; i++)
				{
					doc.add( fs[i]);
				}
				
				indexWriter.addDocument(doc);
				count++;
			}
			
		indexWriter.close();
			
		} 
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		stopwatch.stop();
		System.out.println("Processed: "+count+" user entries, time:"+stopwatch.toString());
		
	}
}
