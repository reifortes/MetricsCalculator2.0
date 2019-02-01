package CF.DataModel;

import java.io.File;
import java.io.IOException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;

public class FileDataModelMC extends FileDataModel implements DataModelMC{

	public FileDataModelMC(File dataFile) throws IOException {
		super(dataFile);
		// TODO Auto-generated constructor stub
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int getNumItemsWithPreferenceBy(long userID) throws TasteException {
		return getPreferencesFromUser(userID).length();
	}
}
