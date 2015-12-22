package com.stonecraft.datastore;

import java.util.ArrayList;
import java.util.List;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interfaces.IDBConnector;
import com.stonecraft.datastore.interfaces.OnTaskCompleteListener;
import com.stonecraft.datastore.interfaces.Tasker;

abstract class DatabaseTask implements Tasker {

	protected int myToken;
	protected IDBConnector myConnection;
	private int myTaskId;
	private List<OnTaskCompleteListener> myTaskListeners;

	/**
	 * This runnable executes the statement in a separate thread and notifies
	 * any listeners when it is complete.
	 */
	private Runnable myRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				executeTask();
			} catch (DatabaseException e) {
				// This exception can be ignored here as it is returned in the
				// statement complete listeners
			}
		}
	};

	public DatabaseTask(int taskId, int token, IDBConnector conn) {
		myTaskId = taskId;
		myToken = token;
		myConnection = conn;

		myTaskListeners = new ArrayList<OnTaskCompleteListener>();
	}

	/**
	 * @return the taskId
	 */
	public int getTaskId() {
		return myTaskId;
	}

	public int getToken() {
		return myToken;
	}

	/**
	 * This method adds a listener that will be notified when this task has
	 * completed.
	 * 
	 * @param listener
	 */
	public void addOnTaskCompleteListener(OnTaskCompleteListener listener) {
		if (listener != null) {
			myTaskListeners.add(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * au.com.delaney.listeners.Tasker#removeOnTaskCompleteListener(au.com.delaney
	 * .listeners.OnTaskCompleteListener)
	 */
	@Override
	public void removeOnTaskCompleteListener(OnTaskCompleteListener listener) {
		myTaskListeners.remove(listener);
	}

	/**
	 * This method will execute the Database statement in a separate thread and
	 * return the data in all registered statement complete listeners. if no
	 * onStatmentComplete listeners have been register the result of this task
	 * as well as any exceptions will be lost.
	 */
	public void Start() throws DatabaseException {
		new Thread(myRunnable).start();
	}

	/**
	 * This method notifys any listeners that the task has completed.
	 */
	public void notifyTaskListeners() {
		for (OnTaskCompleteListener listener : myTaskListeners) {
			listener.onTaskComplete(this);
		}
	}

	/**
	 * This method executes the task, and returns the result of the statement in
	 * a RSData object
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	public abstract void executeTask() throws DatabaseException;
}
