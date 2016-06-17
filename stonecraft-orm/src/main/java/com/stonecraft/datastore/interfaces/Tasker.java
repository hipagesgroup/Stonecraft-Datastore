/**
 * 
 */
package com.stonecraft.datastore.interfaces;

/**
 * This interface is used by classes that are used as a task. This interface
 * handles the setting and notifying of OnTaskCompleteListener Listeners
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created Aug 9, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public interface Tasker {
	/**
	 * This method sets the listener that will be notified when the task has
	 * been completed
	 * 
	 * @param listener
	 */
	public void addOnTaskCompleteListener(OnTaskCompleteListener listener);

	/**
	 * This method removes a listener so that they will no longer be notified
	 * when this task is completed.
	 * 
	 * @param listener
	 */
	public void removeOnTaskCompleteListener(OnTaskCompleteListener listener);

	/**
	 * This method notifys the listener that this task has been compelted.
	 */
	public void notifyTaskListeners();

	/**
	 * This method returns the token of this task
	 * 
	 * @return
	 */
	public int getToken();

	boolean isTaskRunning();
}
