package CF.Producer_Consumer.Preprocessing;

import java.util.Iterator;

import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import CF.DataModel.DataModelMC;
import Output.OutPut;
import Measure.CPUTimer;

import com.ecyrd.speed4j.StopWatch;

public class ConsumerPreprocessing extends Thread {

	private ResourcePreprocessing resource;
	private DataModelMC model;
	private OutPut outFile;
	private StopWatch stItem;
	private StopWatch stUser;
	private long metric;
	CPUTimer timer;

	public ConsumerPreprocessing(DataModelMC model, ResourcePreprocessing resource, OutPut outFile, long metric) {
		timer = new CPUTimer("consumerPreprocessing thread");
		timer.start();
		this.resource = resource;
		this.outFile = outFile;
		this.model = model;
		this.metric = metric;
		this.stItem = new StopWatch();
		this.stUser = new StopWatch();
	}

	public synchronized void run() {
		Iterator<Preference> preferences;
		RegisterPreprocessing register = null;
		try {
			while ((resource.isFinished() == false) || (resource.getNumOfRegisters() != 0)) {
				if ((register = resource.getRegister()) != null) {
					switch (register.getVersion()) {
						case ITEM_VALUE:
							stItem.start();
							preferences = register.getIterador();
							while (preferences.hasNext()) {
								preProcessingItem(preferences.next().getItemID());
							}
							stItem.stop();
							if(outFile != null) outFile.setValue(metric, null, 1.0); /* ,stItem.getTimeNanos() */
							break;
						case USER_VALUE:
							stUser.start();
							preferences = register.getIterador();
							while (preferences.hasNext()) {
								preProcessingUser(preferences.next().getUserID());
							}
							stUser.stop();
							if(outFile != null) outFile.setValue(metric, null, 2.0); /* ,stItem.getTimeNanos() */
							break;
						case ITEM_USER_VALUE:
							// O VALOR DE ITEM_USER E O SOMATORIO DOS VALORES DE ITEM E USER
							// POR ISSO, NAO E NECESSARIO FAZER NENHUMA OPERCAO AQI.
							break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		timer.stop();
		timer.print();
	}

	protected void preProcessingItem(long currentItem) throws TasteException {
		try {
			PreferenceArray preferenceItemArray = model.getPreferencesForItem(currentItem);
			Iterator<Preference> arrayIterator = preferenceItemArray.iterator();
			FastIDSet commomItems = new FastIDSet();
			for (; arrayIterator.hasNext();) {
				long userID = arrayIterator.next().getUserID();
				PreferenceArray preferencesFromuser = model.getPreferencesFromUser(userID);
				Iterator<Preference> arrayAux = preferencesFromuser.iterator();
				for (; arrayAux.hasNext();) {
					commomItems.add(arrayAux.next().getItemID());
				}
				commomItems.remove(currentItem); // Removendo o item em questao
			}
			resource.getHashCommomItem().put(currentItem, commomItems.size());
		} catch (NoSuchItemException e) {
			resource.getHashCommomItem().put(currentItem, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void preProcessingUser(long currentUser) throws TasteException {
		try {
			PreferenceArray preferenceUserArray = model.getPreferencesFromUser(currentUser);
			Iterator<Preference> arrayIterator = preferenceUserArray.iterator();
			FastIDSet commomUsers = new FastIDSet();
			for (; arrayIterator.hasNext();) {
				long itemID = arrayIterator.next().getItemID();
				PreferenceArray preferencesForItem = model.getPreferencesForItem(itemID);
				Iterator<Preference> arrayAux = preferencesForItem.iterator();
				for (; arrayAux.hasNext();) {
					commomUsers.add(arrayAux.next().getUserID());
				}
				commomUsers.remove(currentUser); // Removendo o usuario em questao
			}
			resource.getHashCommomUser().put(currentUser, commomUsers.size());
		} catch (NoSuchUserException e) {
			resource.getHashCommomUser().put(currentUser, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}