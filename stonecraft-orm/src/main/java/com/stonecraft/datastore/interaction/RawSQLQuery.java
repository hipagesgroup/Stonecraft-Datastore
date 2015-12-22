package com.stonecraft.datastore.interaction;

/**
 * This class is used to run a simple SQL statement on the database. There is no validation in
 * this class so it is the caller's task to ensure the correct SQL is used.
 * 
 * @author Michael Delaney
 * @author $Author: michael.delaney $
 * @created 16/03/2012
 * @date $Date: 16/03/2012 01:50:39 $
 * @version $Revision: 1.0 $
 */
public class RawSQLQuery extends Statement {
	private String myQuery;

	/**
	 * This constructor creates an instance of RawSQLQuery. The result from running
	 * 
	 * @param query
	 */
	public RawSQLQuery(String tableName, String query) {
		super(tableName);
		myQuery = query;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return myQuery;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(String query) {
		myQuery = query;
	}
}
