package com.stonecraft.datastore;

/**
 * This interface is used as the call back when a query statement is completed
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public abstract class OnQueryComplete<T> implements QueryComplete{

	/**
	 * This method is used to parse the data that has been returned from the database query.
	 *
	 * @param data
	 * @return
	 */
	public T[] parseData(RSData data) {
		return null;
	}

	/**
	 * This method returns the results of a query that has been made to
	 * Datastore.
	 * 
	 * @param token
	 * @param resultSet
	 */
	public abstract void onQueryComplete(int token, T[] resultSet);

}
