package au.com.fairfaxdigital.common.database;

import java.sql.Types;

/**
 * This class contains the constants used within Datastore.
 * 
 * An instance of this class cannot be created
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class DBConstants {
	/**
	 * Can't make instance of this class
	 */
	private DBConstants() {
	}

	public static final String INSERT_INTO = "INSERT INTO";
	public static final String DELETE_FROM = "DELETE FROM";
	public static final String VALUES = "VALUES";
	public static final String COMMA = ",";
	public static final String SINGLE_QUOTE = "'";
	public static final String OPEN_BRACKET = "(";
	public static final String CLOSE_BRACKET = ")";
	public static final String ON = "ON";
	public static final String AND = "AND";
	public static final String SELECT = "SELECT";
	public static final String FROM = "FROM";
	public static final String DISTINCT = "DISTINCT";
	public static final String WHERE = "WHERE";
	public static final String ORDER_BY = "ORDER BY";
	public static final String GROUP_BY = "GROUP BY";
	public static final String HAVING = "HAVING";
	public static final String LIMIT = "LIMIT";

	public static final int NO_RECORDS_UPDATED = -1;

	public static final int DATATYPE_INT_NULL = Types.NULL;
	public static final int DATATYPE_INT_INTEGER = Types.INTEGER;
	public static final int DATATYPE_INT_BOOLEAN = Types.BOOLEAN;
	public static final int DATATYPE_INT_DOUBLE = Types.DOUBLE;
	public static final int DATATYPE_INT_STRING = Types.VARCHAR;
	public static final int DATATYPE_INT_DATETIME = Types.DATE;
	public static final int DATATYPE_INT_BLOB = Types.BLOB;
	public static final int DATATYPE_INT_UNKNOWN = -1;

	public static final String DATATYPE_NULL = "Null";
	public static final String DATATYPE_INTEGER = "Integer";
	public static final String DATATYPE_BOOLEAN = "Boolean";
	public static final String DATATYPE_DOUBLE = "Double";
	public static final String DATATYPE_STRING = "String";
	public static final String DATATYPE_DATETIME = "Datetime";
	public static final String DATATYPE_BLOB = "Blob";

	public static final int SQLITE_TRUE = 1;
	public static final int SQLITE_FALSE = 0;
	
	public static final String TABLE_MAP = "TABLE_MAP";
	public static final String TABLE_SCHEMA_SETTINGS = "SCHEMA_SETTINGS";
	
	public static final String COLUMN_TABLE_NAME = "TABLE_NAME";
	public static final String COLUMN_COLUMN_NAME = "COLUMN_NAME";
	public static final String COLUMN_DATA_TYPE = "DATA_TYPE";
	public static final String COLUMN_DATA_LENGTH = "DATA_LENGTH";
	public static final String COLUMN_IS_PRIMARY_KEY = "IS_PRIMARY_KEY";
	public static final String COLUMN_IS_AUTOINCREMENTING = "IS_AUTOINCREMENTING";
	public static final String COLUMN_IS_NULLABLE = "IS_NULLABLE";
	public static final String COLUMN_TYPE = "TYPE";
	public static final String COLUMN_VALUE = "VALUE";
	
	public static final String SCHEMA_SETTINGS_TYPE_DB_NAME = "DB_NAME";
	public static final String SCHEMA_SETTINGS_TYPE_DB_VERSION = "DB_VERSION";
}
