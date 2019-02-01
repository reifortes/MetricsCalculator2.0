package CF.DataModel;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;


public interface DataModelMC extends DataModel {

	/**
	   * @param itemID item ID to check for
	   * @return the number of users who have expressed a preference for the item
	   * @throws TasteException if an error occurs while accessing the data
	   */
	  int getNumItemsWithPreferenceBy(long userID) throws TasteException;


}
