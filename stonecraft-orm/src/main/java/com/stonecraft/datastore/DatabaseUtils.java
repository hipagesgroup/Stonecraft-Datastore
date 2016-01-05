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

    /**
     * This method returns the tablename and column name in a format that should be used in
     * a statements AS clause.
     *
     * For example 'Select PHOTO.ID AS [photo.id] from PHOTO'
     *
     * NOTE: This method only returns the name. The clause should add the brackets '[]' so
     * that the clause don't encounter an error due to the invalid characters
     *
     * @param tableName
     * @param colName
     * @return
     */
	public static String getDatabaseAsName(String tableName, String colName) {
		return (tableName + colName).toLowerCase();
	}

    public static String normaliseTableColumnAsName(String name) {
        return name.replace(getTableColumnSeparator(), "").toLowerCase();
    }

    /**
     * This method returns the standard table column seperator used in SQL
     *
     * For example the '.' in PHOTO.ID
     * @return
     */
    public static String getTableColumnSeparator() {
        return ".";
    }
}
