package CF.DataModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.common.iterator.FileLineIterable;
//import org.slf4j.LoggerFactory; nao compativel com mahout 0.12


public final class NetflixDataModel extends AbstractDataModelMC {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//private static final org.slf4j.Logger log = LoggerFactory.getLogger(NetflixDataModel.class);
	private final DataModelMC delegate;     //DELEGANDO AO NOSSO DATA MODEL - DATAMODELMC -----------------------------
	private final boolean useSubset;


	public NetflixDataModel(File dataDirectory, boolean useSubset) throws IOException {
		if (dataDirectory == null) {
			throw new IllegalArgumentException("dataDirectory is null");
		}
		if (!dataDirectory.exists() || !dataDirectory.isDirectory()) {
			throw new FileNotFoundException(dataDirectory.toString());
		}

		this.useSubset = useSubset;

		//log.info("Creating NetflixDataModel for directory: {}");

		//log.info("Reading preference data...");
		FastByIDMap<PreferenceArray> users = readUsers(dataDirectory);

		//log.info("Creating delegate DataModel...");
		delegate = new GenericDataModelMC(users);    //UTILIZANDO DATAMODELMC-----------------------------------
	}


	private FastByIDMap<PreferenceArray> readUsers(File dataDirectory) throws IOException {
		FastByIDMap<Collection<Preference>> userIDPrefMap = new FastByIDMap<Collection<Preference>>();

		int counter = 0;
		FilenameFilter filenameFilter = new MovieFilenameFilter();
		for (File movieFile : new File(dataDirectory, "training_set").listFiles(filenameFilter)) {
			Iterator<String> lineIterator = new FileLineIterable(movieFile, false).iterator();
			String line = lineIterator.next();
			long movieID = Long.parseLong(line.substring(0, line.length() - 1)); // strip colon
			while (lineIterator.hasNext()) {
				line = lineIterator.next();
				if (++counter % 100000 == 0) {
					//log.info("Processed {} prefs");
				}
				int firstComma = line.indexOf((int) ',');
				long userID = Long.parseLong(line.substring(0, firstComma));
				int secondComma = line.indexOf((int) ',', firstComma + 1);
				float rating = Float.parseFloat(line.substring(firstComma + 1, secondComma));
				Collection<Preference> userPrefs = userIDPrefMap.get(userID);
				if (userPrefs == null) {
					userPrefs = new ArrayList<Preference>(2);
					userIDPrefMap.put(userID, userPrefs);
				}
				userPrefs.add(new GenericPreference(userID, movieID, rating));
			}
		}

		return GenericDataModel.toDataMap(userIDPrefMap, true);
	}


	public void refresh(Collection<Refreshable> arg0) {
		// do nothing 
	}

	public LongPrimitiveIterator getItemIDs() throws TasteException {
		return delegate.getItemIDs();
	}

	public FastIDSet getItemIDsFromUser(long arg0) throws TasteException {
		return delegate.getItemIDsFromUser(arg0);	
	}

	public float getMaxPreference() {
		return delegate.getMaxPreference(); 
	}

	public float getMinPreference() {
		return delegate.getMinPreference();
	}

	public int getNumItems() throws TasteException {
		return delegate.getNumItems();
	}

	public int getNumUsers() throws TasteException {
		return delegate.getNumUsers();
	}

	public int getNumUsersWithPreferenceFor(long arg0) throws TasteException {
		return delegate.getNumUsersWithPreferenceFor(arg0);
	}

	public int getNumUsersWithPreferenceFor(long arg0, long arg1)
			throws TasteException {
		return delegate.getNumUsersWithPreferenceFor(arg0, arg1);
	}

	public Long getPreferenceTime(long arg0, long arg1) throws TasteException {
		return delegate.getPreferenceTime(arg0, arg1);
	}

	public Float getPreferenceValue(long arg0, long arg1) throws TasteException {
		return delegate.getPreferenceValue(arg0, arg1);
	}

	public PreferenceArray getPreferencesForItem(long arg0)
			throws TasteException {
		return delegate.getPreferencesForItem(arg0);
	}

	public PreferenceArray getPreferencesFromUser(long arg0)
			throws TasteException {
		return delegate.getPreferencesFromUser(arg0);
	}

	public LongPrimitiveIterator getUserIDs() throws TasteException {
		return delegate.getUserIDs();
	}

	public boolean hasPreferenceValues() {
		return delegate.hasPreferenceValues();
	}

	public void removePreference(long arg0, long arg1) throws TasteException {
		throw new UnsupportedOperationException();
	}

	public void setPreference(long arg0, long arg1, float arg2)
			throws TasteException {
		throw new UnsupportedOperationException();
	}

	private class MovieFilenameFilter implements FilenameFilter {
		public boolean accept(File dir, String filename) {
			return filename.startsWith(useSubset ? "mv_0000" : "mv_");
		}
	}

	public String toString() {
		return "NetflixDataModel";
	}


	public DataModel getDelegate() {
		return delegate;
	}


	public int getNumItemsWithPreferenceBy(long userID) throws TasteException {
		return delegate.getNumItemsWithPreferenceBy(userID);
	}

}
