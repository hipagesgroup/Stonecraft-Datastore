package com.stonecraft.datastore;

import android.os.AsyncTask;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interfaces.OnTaskCompleteListener;
import com.stonecraft.datastore.interfaces.Tasker;

import java.util.ArrayList;
import java.util.List;

abstract class DatabaseTask extends AsyncTask<Void, Void, DatabaseException> implements Tasker {

	protected int myToken;
	protected Datastore myDatastore;
	private int myTaskId;
	private List<OnTaskCompleteListener> myTaskListeners;
	private RuntimeException myPendingRuntimeException;

	public DatabaseTask(int taskId, int token, Datastore datastore) {
		myTaskId = taskId;
		myToken = token;
		myDatastore = datastore;

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
	protected void onPreExecute() {
		myPendingRuntimeException = new RuntimeException();
		super.onPreExecute();
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
		} catch (RuntimeException e) {
			myPendingRuntimeException.initCause(e);
			throw myPendingRuntimeException;
		}
	}

	/**
	 * This method executes the task, and returns the result of the statement in
	 * a RSData object
	 * 
	 * @throws DatabaseException
	 */
	public abstract void startTask() throws DatabaseException;

	@Override
	public boolean isTaskRunning() {
		return getStatus() == Status.RUNNING;
	}

	abstract void notifyStmtListeners(DatabaseException e);
}
