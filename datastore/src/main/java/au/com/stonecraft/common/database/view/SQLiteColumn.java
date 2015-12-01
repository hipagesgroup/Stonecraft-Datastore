package au.com.stonecraft.common.database.view;

import android.text.TextUtils;

import org.w3c.dom.Text;

import au.com.stonecraft.common.database.DBConstants;
import au.com.stonecraft.common.database.exceptions.DatabaseException;
import au.com.stonecraft.common.database.utils.StringUtils;

/**
 * This class represents a SQLite database column. It holds everything that is
 * needed to create a column in the SQLiteDatabase
 * @author michaeldelaney
 *
 */
/**
 * This class represents a SQLite database column. It contains the specifics for
 * creating a column in a mySQL DB
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class SQLiteColumn extends DatabaseColumn {
	// SQLite Datatypes
	public static final String DATATYPE_NULL = "NULL";
	public static final String DATATYPE_INTEGER = "INTEGER";
	public static final String DATATYPE_REAL = "REAL";
	public static final String DATATYPE_TEXT = "TEXT";
	public static final String DATATYPE_BLOB = "BLOB";

	/**
	 * This constructor represents a database column that is non-nullable, is
	 * not a primary key and does not auto increment
	 * 
	 * @param name
	 * @param type
	 */
	public SQLiteColumn(String name, int type, int length) {
		super(name, type, length, false);
	}

	/**
	 * This constructor represents a database column that does not
	 * auto-increment.
	 * 
	 * @param name
	 * @param type
	 * @param isPrimaryKey
	 */
	public SQLiteColumn(String name, int type, int length, boolean isPrimaryKey) {
		super(name, type, length, isPrimaryKey, false);
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
	public SQLiteColumn(String name, int type, int length,
			boolean isPrimaryKey, boolean isNullable) {
		super(name, type, length, isPrimaryKey, isNullable, false);
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
	public SQLiteColumn(String name, int type, int length,
			boolean isPrimaryKey, boolean isNullable, boolean isAutoIncrement) {
		super(name, type, length, isPrimaryKey, isNullable, isAutoIncrement);
	}

	@Override
	public String getCreateColumnStmt(boolean hasTableCompositeKey)
			throws DatabaseException {
		StringBuilder builder = new StringBuilder();
		String type = getTypeString();
		builder.append(getName()).append(" ").append(type)
				.append(" ");

		if(!isPrimarykey()) {
			if (!isNullable()) {
				builder.append(NOT_NULL).append(" ");
			}
			if(!TextUtils.isEmpty(getDefaultValue())) {
				builder.append(DEFAULT).append(" ");
					if(type.equals(DATATYPE_TEXT)) {
						builder.append("'").append(getDefaultValue()).append("'");
					} else {
						builder.append(getDefaultValue());
					}
					builder.append(" ");
			}
		}

		if (!hasTableCompositeKey && isPrimarykey()) {
			builder.append(PRIMARY_KEY).append(" ");

			if (isAutoIncrement()) {
				builder.append(AUTO_INCREMENT);
			}
		}

		return StringUtils.removeStringSuffix(builder.toString(), " ");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		DatabaseColumn compareCol = (DatabaseColumn)o;
		if(getName().equals(compareCol.getName()) && getType() == compareCol.getType() &&
			isAutoIncrement() == compareCol.isAutoIncrement() &&
			isNullable() == compareCol.isNullable() && isPrimarykey() == compareCol.isPrimarykey()) {
			return true;
		}
		
		return false;
	}

	@Override
	protected String getTypeString() throws DatabaseException {
		switch (getType()) {
		case DBConstants.DATATYPE_INT_NULL:
			return DATATYPE_NULL;
		case DBConstants.DATATYPE_INT_INTEGER:
		case DBConstants.DATATYPE_INT_BOOLEAN:
		case DBConstants.DATATYPE_INT_DATETIME:
			return DATATYPE_INTEGER;
		case DBConstants.DATATYPE_INT_DOUBLE:
			return DATATYPE_REAL;
		case DBConstants.DATATYPE_INT_STRING:
			return DATATYPE_TEXT;
		case DBConstants.DATATYPE_INT_BLOB:
			return DATATYPE_BLOB;
		}

		throw new DatabaseException("The data type is unknown for " + "column "
				+ getName());
	}
}
