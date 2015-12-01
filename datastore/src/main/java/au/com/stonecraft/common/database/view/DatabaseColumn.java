package au.com.stonecraft.common.database.view;

import au.com.stonecraft.common.database.exceptions.DatabaseException;

/**
 * This class is the base class for a abstract view of a database column. The
 * sub classes are in charge of implement the db specific syntax for creating a
 * column
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public abstract class DatabaseColumn {
	public static final String PRIMARY_KEY = "PRIMARY KEY";
	public static final String NOT_NULL = "NOT NULL";
	public static final String DEFAULT = "DEFAULT";
	public static final String AUTO_INCREMENT = "AUTOINCREMENT";

	private String myName;
	private int myType;
	private boolean myIsPrimarykey;
	private boolean myIsAutoIncrement;
	private boolean myIsNullable;
	private int myLength;
	private String myDefaultValue;

	/**
	 * This constructor represents a database column that is non-nullable, is
	 * not a primary key and does not auto increment
	 * 
	 * @param name
	 * @param type
	 */
	public DatabaseColumn(String name, int type, int length) {
		this(name, type, length, false);
	}

	/**
	 * This constructor represents a database column that does not
	 * auto-increment.
	 * 
	 * @param name
	 * @param type
	 * @param isPrimaryKey
	 */
	public DatabaseColumn(String name, int type, int length,
			boolean isPrimaryKey) {
		this(name, type, length, isPrimaryKey, false);
	}

	/**
	 * This constructor represents a database column that does not
	 * auto-increment. If this column is set as a primary key it will be made
	 * non-nullable regardless of what is passed in
	 * 
	 * @param name
	 * @param type
	 * @param isPrimaryKey
	 * @param isNullable
	 */
	public DatabaseColumn(String name, int type, int length,
			boolean isPrimaryKey, boolean isNullable) {
		this(name, type, length, isPrimaryKey, isNullable, false);
	}

	/**
	 * If the column is set as a primary key it will be made non-nullable
	 * regardless of what is passed in
	 * 
	 * @param name
	 * @param type
	 * @param isPrimaryKey
	 * @param isNullable
	 * @param isAutoIncrement
	 */
	public DatabaseColumn(String name, int type, int length,
			boolean isPrimaryKey, boolean isNullable, boolean isAutoIncrement) {
		myName = name;
		myType = type;
		myIsPrimarykey = isPrimaryKey;
		myIsAutoIncrement = isAutoIncrement;
		myLength = length;

		if (isPrimaryKey) {
			myIsNullable = false;
		} else {
			myIsNullable = isNullable;
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return myName;
	}

	/**
	 * @return the column data type
	 */
	public int getType() {
		return myType;
	}

	/**
	 * @return whether this column is part of the primary key
	 */
	public boolean isPrimarykey() {
		return myIsPrimarykey;
	}

	/**
	 * @return whether this column should autoincrement
	 */
	public boolean isAutoIncrement() {
		return myIsAutoIncrement;
	}

	/**
	 * @return whether this column is nullable
	 */
	public boolean isNullable() {
		return myIsNullable;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return myLength;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(int length) {
		myLength = length;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		DatabaseColumn compareCol = (DatabaseColumn)o;
		if(myName.equals(compareCol.myName) && myType == compareCol.myType &&
			myLength == compareCol.myLength && myIsAutoIncrement == compareCol.myIsAutoIncrement &&
			myIsNullable == compareCol.myIsNullable && myIsPrimarykey == compareCol.myIsPrimarykey) {
			return true;
		}
		
		return false;
	}

	public void setDefaultValue(String defaultValue) {
		myDefaultValue = defaultValue;
	}

	public String getDefaultValue() {
		return myDefaultValue;
	}

	/**
	 * This method returns the string that will be used in a create statement to
	 * create this column.
	 */
	public abstract String getCreateColumnStmt(boolean hasTableCompositeKey)
			throws DatabaseException;

	/**
	 * This method returns the DB specific datatype for each of the Standard
	 * datatypes
	 * 
	 * @return
	 */
	protected abstract String getTypeString() throws DatabaseException;
}
