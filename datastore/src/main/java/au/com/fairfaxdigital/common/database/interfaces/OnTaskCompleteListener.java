/**
 * 
 */
package au.com.fairfaxdigital.common.database.interfaces;

/**
 * This interface is implemented for listeners that need to be notified when the
 * task has completed.
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created Aug 9, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public interface OnTaskCompleteListener {
	/**
	 * This method catches the event that is fired when a task has been
	 * completed
	 * 
	 * @param task
	 */
	public void onTaskComplete(Tasker task);
}
