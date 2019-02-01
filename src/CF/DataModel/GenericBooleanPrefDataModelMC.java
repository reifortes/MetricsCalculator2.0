package CF.DataModel;
import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;


public final class GenericBooleanPrefDataModelMC extends AbstractDataModelMC{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	GenericBooleanPrefDataModel model;


	public GenericBooleanPrefDataModelMC(FastByIDMap<FastIDSet> userData) {
		super();
		model = new GenericBooleanPrefDataModel(userData);
	}

	public GenericBooleanPrefDataModelMC(FastByIDMap<FastIDSet> userData, FastByIDMap<FastByIDMap<Long>> timestamps) {
		super();
		model = new GenericBooleanPrefDataModel(userData, timestamps);
	}

	public int getNumItemsWithPreferenceBy(long userID) throws TasteException {
		return model.getPreferencesFromUser(userID).length();		
	}

	public LongPrimitiveIterator getItemIDs() throws TasteException {
		return model.getItemIDs();
	}


	public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
		return model.getItemIDsFromUser(userID);
	}


	public int getNumItems() throws TasteException {
		return model.getNumItems();
	}


	public int getNumUsers() throws TasteException {
		return model.getNumUsers();
	}


	public int getNumUsersWithPreferenceFor(long itemID) throws TasteException {
		return model.getNumUsersWithPreferenceFor(itemID);
	}


	public int getNumUsersWithPreferenceFor(long itemID1, long itemID2)
	throws TasteException {
		return model.getNumUsersWithPreferenceFor(itemID1, itemID2);
	}


	public Long getPreferenceTime(long userID, long itemID)
	throws TasteException {
		return model.getPreferenceTime(userID, itemID);
	}


	public Float getPreferenceValue(long userID, long itemID)
	throws TasteException {
		return model.getPreferenceValue(userID, itemID);
	}

	public PreferenceArray getPreferencesForItem(long itemID)
	throws TasteException {
		return model.getPreferencesForItem(itemID);
	}

	public PreferenceArray getPreferencesFromUser(long userID)
	throws TasteException {
		return model.getPreferencesFromUser(userID);
	}

	public LongPrimitiveIterator getUserIDs() throws TasteException {
		return model.getUserIDs();
	}

	public boolean hasPreferenceValues() {
		return model.hasPreferenceValues();
	}

	public void removePreference(long userID, long itemID)
	throws TasteException {
		model.removePreference(userID, itemID);

	}

	public void setPreference(long userID, long itemID, float value)
	throws TasteException {
		model.setPreference(userID, itemID, value);
	}


	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		model.refresh(alreadyRefreshed);
	}
}
