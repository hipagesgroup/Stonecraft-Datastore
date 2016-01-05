/**
 * 
 */
package com.stonecraft.datastore;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interfaces.OnNonQueryComplete;
import com.stonecraft.datastore.view.DatabaseTable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class
 *
 * @author mdelaney
 * @author Author: michael.delaney
 * @created Aug 9, 2013
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class CreateTableTask extends DatabaseTask {
	
	private static final int TABLE_CREATED = 1;
	private static final int TABLE_NOT_CREATED = 0;
	private DatabaseTable myTable;
	private List<OnNonQueryComplete> myStmtListeners;
	private int myResult;
	
	public CreateTableTask(int taskId, int token, Datastore datastore,
		DatabaseTable table){
		super(taskId, token, datastore);
		myTable = table;
		myStmtListeners = new ArrayList<OnNonQueryComplete>();
		myResult = TABLE_NOT_CREATED;
	}

	/* (non-Javadoc)
	 * @see DatabaseTask#startTask()
	 */
	@Override
	public void startTask() throws DatabaseException {

		try{
			String createStatement = myTable.getCreateTableStmt();
			myDatastore.getActiveDatabase().executeRawStatement(createStatement);
			myResult = TABLE_CREATED;
			notifyStmtListeners(null);
		} catch (DatabaseException e) {
			myResult = TABLE_NOT_CREATED;
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
