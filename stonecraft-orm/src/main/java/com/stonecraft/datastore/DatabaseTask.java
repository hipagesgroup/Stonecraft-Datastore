package com.stonecraft.datastore;

import android.os.AsyncTask;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interfaces.IDBConnector;
import com.stonecraft.datastore.interfaces.OnTaskCompleteListener;
import com.stonecraft.datastore.interfaces.Tasker;

import java.util.ArrayList;
import java.util.List;

abstract class DatabaseTask extends AsyncTask<Void, Void, DatabaseException> implements Tasker {

	protected int myToken;
	protected IDBConnector myConnection;
	private int myTaskId;
	private List<OnTaskCompleteListener> myTaskListeners;

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
	 * This method notifys any listeners that the task has completed.
	 */
	public void notifyTaskListeners() {
		for (OnTaskCompleteListener listener : myTaskListeners) {
			listener.onTaskComplete(this);
		}
	}

	@Override
	protected void onPostExecute(DatabaseException e) {
		notifyStmtListeners(e);
		notifyTaskListeners();
	}

	@Override
	protected DatabaseException doInBackground(Void... params) {
		try {
			startTask();
			return null;
		} catch (DatabaseException e) {
			return e;
		}
	}

	/**
	 * This method executes the task, and returns the result of the statement in
	 * a RSData object
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	public abstract void startTask() throws DatabaseException;

	abstract void notifyStmtListeners(DatabaseException e);
}
