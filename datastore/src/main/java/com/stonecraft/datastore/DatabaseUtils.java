package com.stonecraft.datastore;

/**
 * This class cannot be instanciated.
 * 
 * It contains static utility methods for use with a database.
 * 
 * @author michaeldelaney
 * @created 06/01/2012
 * @version 1.0
 *
 */
/**
 * 
 * This class contains static utility methods for use with a database.
 * 
 * An instance of this class cannot be created
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created Jun 27, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class DatabaseUtils {
	/**
	 * Private so an instance can't be created
	 */
	private DatabaseUtils() {
	}

	/**
	 * This method converts an int in the database to a boolean
	 * 
	 * @param value
	 * @return
	 */
	public static boolean convertFromSqliteBoolean(int value) {
		return value != DBConstants.SQLITE_FALSE;
	}

	/**
	 * This method convert a boolean to an int for use in a sqlite database
	 * 
	 * @param value
	 * @return
	 */
	public static int convertToSqliteBoolean(boolean value) {
		if (value) {
			return DBConstants.SQLITE_TRUE;
		}

		return DBConstants.SQLITE_FALSE;
	}

	/**
	 * This method returns the corresponding int value for a datatype.
	 * 
	 * Valid datatypes are those that are found in DatabaseConstants with a
	 * prefix of DATATYPE
	 * 
	 * @param dataType
	 * @return
	 */
	public static int getIntDatatype(String dataType) {
		if (dataType.equals(DBConstants.DATATYPE_NULL)) {
			return DBConstants.DATATYPE_INT_NULL;
		} else if (dataType.equals(DBConstants.DATATYPE_INTEGER)) {
			return DBConstants.DATATYPE_INT_INTEGER;
		} else if (dataType.equals(DBConstants.DATATYPE_BOOLEAN)) {
			return DBConstants.DATATYPE_INT_BOOLEAN;
		} else if (dataType.equals(DBConstants.DATATYPE_DOUBLE)) {
			return DBConstants.DATATYPE_INT_DOUBLE;
		} else if (dataType.equals(DBConstants.DATATYPE_STRING)) {
			return DBConstants.DATATYPE_INT_STRING;
		} else if (dataType.equals(DBConstants.DATATYPE_DATETIME)) {
			return DBConstants.DATATYPE_INT_DATETIME;
		} else if (dataType.equals(DBConstants.DATATYPE_BLOB)) {
			return DBConstants.DATATYPE_INT_BLOB;
		}

		return DBConstants.DATATYPE_INT_UNKNOWN;
	}
}
