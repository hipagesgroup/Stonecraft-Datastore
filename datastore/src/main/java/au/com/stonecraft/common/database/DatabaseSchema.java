package au.com.stonecraft.common.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.stonecraft.common.database.view.DatabaseTable;
import au.com.stonecraft.common.database.exceptions.DatabaseException;

/**
 * This class is an abstract view of a database schema. It can be used to get
 * the create statements needed to create a database.
 * 
 * @author michaeldelaney
 * 
 */
public class DatabaseSchema {
	private Map<String, DatabaseTable> myTables;
	private String myName;
	private int myVersion;

	/**
	 * This constructor creates an instance of DatabaseSchema
	 */
	public DatabaseSchema() {
		myTables = new HashMap<String, DatabaseTable>();
	}

	/**
	 * @return the tables
	 */
	public Map<String, DatabaseTable> getTables() {
		return myTables;
	}
	
	/**
	 * @return the tables
	 */
	public DatabaseTable getTable(String name) {
		return myTables.get(name);
	}

	/**
	 * This method adds a table to the Database Schema
	 * 
	 * @param table
	 */
	public void addTable(DatabaseTable table) {
		myTables.put(table.getName(), table);
	}

	/**
	 * This method returns a list of create statements for all tables that are
	 * in this schema. This list can be used to create the database schema.
	 * 
	 * @return
	 */
	public List<String> getTableCreateStmts() throws DatabaseException {
		List<String> statements = new ArrayList<String>();
		for (DatabaseTable table : myTables.values()) {
			statements.add(table.getCreateTableStmt());
		}

		return statements;
	}

	public String getName() {
		return myName;
	}

	public void setName(String name) {
		myName = name;
	}

	public int getVersion() {
		return myVersion;
	}

	public void setVersion(int version) {
		myVersion = version;
	}
}
