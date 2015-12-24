package com.stonecraft.datastore;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interfaces.IDBConnector;
import com.stonecraft.datastore.interfaces.OnNonQueryComplete;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for tasks that are to be run on a database. It handles
 * whether the task is to be run on a separate thread and notifies any listeners
 * on completion.
 * 
 * @author Michael Delaney
 * @author $Author: michael.delaney $
 * @created March 16, 2012
 * @date $Date: 16/03/2012 01:50:39 $
 * @version $Revision: 1.0 $
 */
class DatabaseNonQueryTask extends DatabaseTask {
	private DatastoreTransaction myTransaction;
	private List<OnNonQueryComplete> myStmtListeners;
	private int myResult;

	public DatabaseNonQueryTask(int taskId, int token, IDBConnector conn,
            DatastoreTransaction transaction) {
		super(taskId, token, conn);
        myTransaction = transaction;
		myStmtListeners = new ArrayList<OnNonQueryComplete>();
		myResult = DBConstants.NO_RECORDS_UPDATED;
	}

	/**
	 * This method executes the task, and returns the result of the statement in
	 * a RSData object
	 * 
	 * If the statement is not one of the below know statement types or the base
	 * Statement object is passed into the constructor this task will simply check
	 * if the table exists.
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	@Override
	public void startTask() throws DatabaseException {
        myTransaction.setConnection(myConnection);
        myResult = myTransaction.run();
	}

	/**
	 * This method adds a listener that will be notified when this statement has
	 * completed.
	 * 
	 * @param listener
	 */
	public void addOnStmtCompleteListener(OnNonQueryComplete listener) {
		if (listener != null) {
			myStmtListeners.add(listener);
		}
	}

	/**
	 * This method returns the number of records that were
	 * updated/deleted/inserted in this task. startTask() should be called
	 * before this method is called. DBConstants.NO_RECORDS_UPDATED will be
	 * returned otherwise.
	 * 
	 * @return
	 */
	public int getTaskResult() {
		return myResult;
	}

	/**
	 * This method notifys any listeners that the task has completed.
	 */
	@Override
	void notifyStmtListeners(DatabaseException e) {
		for (OnNonQueryComplete listener : myStmtListeners) {
			if(e != null) {
				listener.onNonQueryFailed(myToken, e);
			} else {
				listener.onNonQueryComplete(myToken, myResult);
			}
		}
	}
}
