package com.stonecraft.datastore.interfaces;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.Delete;
import com.stonecraft.datastore.interaction.Insert;
import com.stonecraft.datastore.interaction.Query;
import com.stonecraft.datastore.interaction.Update;

/**
 * This interface contains the methods required for creating the DB Statements
 * in a syntax the DB understands
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public interface ISyntaxer {
	/**
	 * This method returns a delete statement based on the pass in object
	 * 
	 * @param delete
	 * @return
	 * @throws DatabaseException
	 */
	public String getDelete(Delete delete) throws DatabaseException;

	/**
	 * This method returns a insert statement based on the pass in object
	 * 
	 * @param insert
	 * @return
	 * @throws DatabaseException
	 */
	public String[] getInsert(Insert insert) throws DatabaseException;

	/**
	 * This method returns an update statement based on the pass in object
	 * 
	 * @param update
	 * @return
	 * @throws DatabaseException
	 */
	public String getUpdate(Update update) throws DatabaseException;

	/**
	 * This method returns a query statement based on the pass in object
	 * 
	 * @param query
	 * @return
	 * @throws DatabaseException
	 */
	public String getQuery(Query query) throws DatabaseException;
}
