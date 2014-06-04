package au.com.fairfaxdigital.common.database;

import java.util.ArrayList;
import java.util.List;

import au.com.fairfaxdigital.common.database.exceptions.DatabaseException;
import au.com.fairfaxdigital.common.database.interaction.Query;
import au.com.fairfaxdigital.common.database.interaction.RawSQL;
import au.com.fairfaxdigital.common.database.interaction.Statement;
import au.com.fairfaxdigital.common.database.interfaces.IDBConnector;
import au.com.fairfaxdigital.common.database.interfaces.OnQueryComplete;

/**
 * This class is used for tasks that are to be run on a database. It handles
 * whether the task is to be run on a separate thread and notifies any listeners
 * on completion.
 * 
 * @author Michael Delaney
 * @author $Author: michael.delaney $
 * @created 16/03/2012
 * @date $Date: 16/03/2012 01:50:39 $
 * @version $Revision: 1.0 $
 */
class DatabaseQueryTask extends DatabaseTask {
	private Statement myQuery;
	private List<OnQueryComplete> myQueryListeners;
	private RSData myResult;

	public DatabaseQueryTask(int taskId, int token, IDBConnector conn,
		Statement query) {
		super(taskId, token, conn);
		myQuery = query;
		myQueryListeners = new ArrayList<OnQueryComplete>();
	}
	
	public <T> List<T> getInjectedObjects(Class classToInject) throws DatabaseException {
		RSData data = null;
		if (myQuery instanceof Query) {
			data = myConnection.query((Query)myQuery);
		} else if (myQuery instanceof RawSQL) {
			data = myConnection.executeRawQuery((((RawSQL)myQuery)).getQuery());
		}
		else {
			throw new DatabaseException("Unknown statement type " 
				+ myQuery.getClass().getSimpleName()
				+ ". Must be either " + Query.class.getSimpleName()
				+ " or " + RawSQL.class.getSimpleName());
		}
		
		List<T> returnList = new DatabaseObjectInjector().inject(data, classToInject);
		data.close();
		return returnList;
	}

	/**
	 * This method executes the task, and returns the result of the statement in
	 * a RSData object
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	@Override
	public void executeTask() throws DatabaseException {
		try {
			if (myQuery instanceof Query) {
				myResult = myConnection.query((Query)myQuery);
			} else if (myQuery instanceof RawSQL) {
				myResult = myConnection.executeRawQuery((((RawSQL)myQuery)).getQuery());
			}
			else {
				throw new DatabaseException("Unknown statement type " 
					+ myQuery.getClass().getSimpleName()
					+ ". Must be either " + Query.class.getSimpleName()
					+ " or " + RawSQL.class.getSimpleName());
			}

			notifyStmtListeners(null);
		} catch (DatabaseException e) {
			notifyStmtListeners(e);
			throw e;
		} finally {
			notifyTaskListeners();
		}
	}
	
	

	/**
	 * This method adds a listener that will be notified when this statement has
	 * completed.
	 * 
	 * @param listener
	 */
	public void addOnQueryCompleteListener(OnQueryComplete listener) {
		if (listener != null) {
			myQueryListeners.add(listener);
		}
	}

	/**
	 * This method returns the number of records that were
	 * updated/deleted/inserted in this task. executeTask() should be called
	 * before this method is called. null will be returned otherwise.
	 * 
	 * @return
	 */
	public RSData getTaskResult() {
		return myResult;
	}

	/**
	 * This method notifys any listeners that the task has completed.
	 */
	void notifyStmtListeners(DatabaseException e) {
		for (OnQueryComplete listener : myQueryListeners) {
			listener.onQueryComplete(myToken, myResult, e);
		}
	}
}
