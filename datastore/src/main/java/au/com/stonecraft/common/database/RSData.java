package au.com.stonecraft.common.database;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import au.com.stonecraft.common.database.exceptions.DatabaseException;

/**
 * This interface contains the methods required for interacting with the data
 * returned from a databae query
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public interface RSData extends Iterator<RSData> {
	/**
	 * This method returns the number of rows that were affected. For inserts,
	 * updates and deletes it will return the number of records inserted,
	 * updated or deleted.
	 * 
	 * This method will return 0 if the statement was a query
	 * 
	 * @return
	 */
	public int getCount();

	/**
	 * This method moves the position of the result set to the passed in
	 * position
	 * 
	 * @param postion
	 * @return
	 */
	public boolean moveToPosition(int postion);

	/**
	 * Returns the value of the specified column as a string
	 * 
	 * @param column
	 * @return
	 */
	public String getStringValue(String column) throws DatabaseException;

	/**
	 * Returns the value of the specified column as a boolean
	 * 
	 * @param column
	 * @return
	 * @throws DatabaseException
	 */
	public boolean getBooleanValue(String column) throws DatabaseException;

	/**
	 * Returns the value of the specified column as an integer
	 * 
	 * If the value cannot be represented as an integer a DatabaseException will
	 * be thrown
	 * 
	 * @param column
	 * @return
	 * @throws DatabaseException
	 */
	public int getIntValue(String column) throws DatabaseException;

	/**
	 * Returns the value of the specified column as a Long
	 * 
	 * If the value cannot be represented as a long a DatabaseException will be
	 * thrown
	 * 
	 * @param column
	 * @return
	 * @throws DatabaseException
	 */
	public long getLongValue(String column) throws DatabaseException;

	/**
	 * Returns the value of the specified column as an double
	 * 
	 * If the value cannot be represented as a double a DatabaseException will
	 * be thrown
	 * 
	 * @param column
	 * @return
	 */
	public double getDoubleValue(String column) throws DatabaseException;

	/**
	 * Returns the value of the specified column as an float
	 *
	 * If the value cannot be represented as a float a DatabaseException will
	 * be thrown
	 *
	 * @param column
	 * @return
	 */
	public float getFloatValue(String column) throws DatabaseException;

	/**
	 * Returns the value of the specified column as a Date object
	 * 
	 * If the columns data type is not a Date a DatabaseException will be thrown
	 * 
	 * @param column
	 * @return
	 */
	public Date getDateValue(String column) throws DatabaseException;
	
	/**
	 * Returns the value of the specified column as a Calendar object
	 * 
	 * If the columns data type is not a Calendar a DatabaseException will be thrown
	 * 
	 * @param column
	 * @return
	 */
	public Calendar getCalendarValue(String column) throws DatabaseException;
	
	/**
	 * This method returns whether the column passed in contains a null value.
	 *
	 * @param column
	 * @return
	 * @throws DatabaseException
	 */
	public boolean containsNull(String column) throws DatabaseException;
	
	/**
	 * Returns the value of the specified column as a byte array
	 * 
	 * If the columns data type is not a Blob a DatabaseException will be thrown
	 * 
	 * @param column
	 * @return
	 */
	public byte[] getBlobData(String column) throws DatabaseException;

	/**
	 * This method moves the position of this cursor to the first record
	 */
	public void moveToFirst();

	/**
	 * This method checks if the current position is the last position in this
	 * result set
	 */
	public boolean isAfterLast();

	/**
	 * This method closes the underlying Data source.
	 */
	public void close();
	
	/**
	 * This method returns whether this object contains a column based on it's name
	 *
	 * @param columnName
	 * @return
	 */
	public boolean hasColumn(String columnName);
}
