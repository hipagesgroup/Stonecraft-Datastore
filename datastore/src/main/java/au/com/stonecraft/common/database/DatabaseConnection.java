package au.com.stonecraft.common.database;

import au.com.stonecraft.common.database.exceptions.DatabaseException;
import au.com.stonecraft.common.database.interfaces.IDBConnector;
import au.com.stonecraft.common.database.interfaces.ISchemaCreator;
import au.com.stonecraft.common.database.interfaces.ISyntaxer;

/**
 * This class is the base class for all Connections to a database.
 * 
 * It implements IDBConnector that contains the signatures for interacting with
 * the database. All sub-classes will need to implement these methods
 * 
 * @author Michael Delaney
 * @author $Author: michael.delaney $
 * @created March 16, 2012
 * @date $Date: 16/03/2012 01:50:39 $
 * @version $Revision: 1.0 $
 */
public abstract class DatabaseConnection implements IDBConnector {
	protected ISyntaxer mySyntaxer;
	private String myName;
	private int myCurrentVersion;

	public DatabaseConnection(String name, int version, ISyntaxer syntaxer) {
		myName = name;
		myCurrentVersion = version;
		mySyntaxer = syntaxer;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return myName;
	}

	/**
	 * @return the currentVersion
	 */
	public int getVersion() {
		return myCurrentVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * au.com.standard.datastore.DatabaseConnection#createSchema(au.com.standard
	 * .datastore.IDatabase)
	 */

	public void createSchema(ISchemaCreator database) throws DatabaseException {
		DatabaseSchema schema = database.getSchema();
		
		startTransaction();
		for (String createStmt : schema.getTableCreateStmts()) {
			executeRawStatement(createStmt);
		}
		commit();
	}
}
