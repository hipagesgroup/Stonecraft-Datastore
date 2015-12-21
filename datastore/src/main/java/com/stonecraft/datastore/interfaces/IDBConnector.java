package com.stonecraft.datastore.interfaces;

import android.net.Uri;

import com.stonecraft.datastore.DatabaseSchema;
import com.stonecraft.datastore.RSData;
import com.stonecraft.datastore.interaction.Delete;
import com.stonecraft.datastore.interaction.Insert;
import com.stonecraft.datastore.interaction.Query;
import com.stonecraft.datastore.interaction.Update;
import com.stonecraft.datastore.view.DatabaseColumn;
import com.stonecraft.datastore.view.DatabaseTable;
import com.stonecraft.datastore.view.DatabaseViewFactory;
import com.stonecraft.datastore.exceptions.DatabaseException;

/**
 * This Interface contains the methods for all interaction to any database.
 * 
 * Classes implementing this interface will implement the functionality of the
 * database it is intending to connect to
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public interface IDBConnector {
	
	public static final int CHANGE_NONE = 0;
	public static final int CHANGE_ALLOWED = 1;
	public static final int CHANGE_EXCEPTION = -1;
	
	/**
	 * This method returns the name of this connection type
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * This method returns the version of the database the connection will be
	 * connecting to.
	 * 
	 * This is used to work out if this connection is connecting to the correct
	 * version of the database.
	 * 
	 * @return
	 */
	public int getVersion();

	/**
	 * This method starts a transaction. A database transaction ensures all
	 * changes are done in a block. If an error occurs rollBack() is called so
	 * that no changes done in the transaction are committed. Enclosing multiple
	 * DB changes in a transaction is also more efficient.
	 * 
	 * @throws DatabaseException
	 */
	public void startTransaction() throws DatabaseException;

	/**
	 * This method saves all changes done in the current transaction to the
	 * database
	 * 
	 * @throws DatabaseException
	 */
	public void commit() throws DatabaseException;

	/**
	 * This method stops any changes in the current transaction from being
	 * committed to the database.
	 * 
	 * @throws DatabaseException
	 */
	public void rollBack() throws DatabaseException;

	/**
	 * This method closes the database connection. This method cleans up the
	 * database connection and ensures it is closed correctly
	 * 
	 * @throws DatabaseException
	 */
	public void close() throws DatabaseException;

	/**
	 * This method checks if the current connection is open and ready to be
	 * interacted with
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	public boolean isOpen() throws DatabaseException;

	/**
	 * This method performs a query on this connection. It returns an RSData
	 * object containing the data for this query
	 * 
	 * @param query
	 * @return
	 * @throws DatabaseException
	 */
	public RSData query(Query query) throws DatabaseException;

	/**
	 * This method inserts data into the database
	 * 
	 * @param insert
	 * @return
	 * @throws DatabaseException
	 */
	public void insert(Insert insert) throws DatabaseException;

	/**
	 * This method updates data in the database
	 * 
	 * @param update
	 * @return
	 * @throws DatabaseException
	 */
	public int update(Update update) throws DatabaseException;

	/**
	 * This method deletes data from the database
	 * 
	 * @param delete
	 * @return
	 * @throws DatabaseException
	 */
	public int delete(Delete delete) throws DatabaseException;

	/**
	 * This method executes an sql statement without any sort of validation
	 * 
	 * @param stmt
	 * @return
	 * @throws DatabaseException
	 */
	public void executeRawStatement(String stmt) throws DatabaseException;
	
	/**
	 * This method executes an sql query without any sort of validation
	 *
	 * @param stmt
	 * @throws DatabaseException
	 */
	public RSData executeRawQuery(String stmt) throws DatabaseException;
	
	/**
	 * This method checks if a table currently exists in the Database.
	 *
	 * @param tableName
	 * @return
	 * @throws DatabaseException
	 */
	public int doesTableExist(String tableName) throws DatabaseException;

	/**
	 * This method creates a connection to the database
	 * 
	 * @throws DatabaseException
	 */
	public void createConnection() throws DatabaseException;

	/**
	 * This method creates a database schema. If a schema already exists, all
	 * tables will be dropped before creating the new schema.
	 * 
	 * @param database
	 * @throws DatabaseException
	 */
	public void createSchema(ISchemaCreator database) throws DatabaseException;

    /**
     * This method updates a table in the database with the same name name as the passed
     * in table.
     *
     * @param oldTable
     * @param newTable
     * @throws DatabaseException
     */
	public void updateTable(DatabaseTable oldTable, DatabaseTable newTable) throws DatabaseException;
	
	/** 
	 * This method returns a new column object that represent a column for this type
	 * of connection
	 *
	 * @return
	 */
	public DatabaseViewFactory getTableObjectFactory();
	
	/**
	 * This method checks if the two passed in columns are allowed to be upgrade based on the
	 * implementing classes rules
	 *
	 * @param column
	 * @param newSchemaColumn
	 * @return
	 * @throws DatabaseException
	 */
	public int checkColumnUpdateRules(DatabaseColumn column,
                                      DatabaseColumn newSchemaColumn) throws DatabaseException;
	
	/**
	 * This method returns the current database schema.
	 *
	 * @return
	 */
	public DatabaseSchema getDatabaseSchema() ;

	/**
	 * This method returns the uri of the passed in table. This can be used to register a content
	 * observer so that events can be received when there are changes to a table.
	 *
	 * @param tableName
	 * @return
	 */
	public Uri getTableUri(String tableName);
}
