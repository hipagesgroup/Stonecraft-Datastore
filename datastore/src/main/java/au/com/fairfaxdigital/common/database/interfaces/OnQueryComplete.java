package au.com.fairfaxdigital.common.database.interfaces;

import au.com.fairfaxdigital.common.database.RSData;
import au.com.fairfaxdigital.common.database.exceptions.DatabaseException;

/**
 * This interface is used as the call back when a query statement is completed
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public interface OnQueryComplete {
	/**
	 * This method returns the results of a query that has been made to
	 * Datastore.
	 * 
	 * @param token
	 * @param resultSet
	 */
	public void onQueryComplete(int token, RSData resultSet, DatabaseException e);
}
