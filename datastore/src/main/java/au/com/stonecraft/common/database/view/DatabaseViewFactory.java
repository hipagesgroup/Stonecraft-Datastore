/**
 * 
 */
package au.com.stonecraft.common.database.view;

import android.net.Uri;

/**
 * The concrete implementation of this class will generate {@link DatabaseTable} and
 * {@link DatabaseColumn} for a specific type of table.
 *
 * @author mdelaney
 * @author Author: michael.delaney
 * @created Oct 22, 2013
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public interface DatabaseViewFactory {

	/**
	 * This method returns a {@link DatabaseTable} object containing the passed in
	 * table name
	 *
	 * @param tableName
	 * @return
	 */
	public DatabaseTable getNewTable(String tableName, Uri uri);
	
	/**
	 * This method returns a {@link DatabaseColumn} containing the passed in parameters
	 *
	 * @param name
	 * @param type
	 * @param length
	 * @return
	 */
	public DatabaseColumn getNewColumn(String name, int type, int length);
	
	/**
	 * This method returns a {@link DatabaseColumn} containing the passed in parameters
	 *
	 * @param name
	 * @param type
	 * @param length
	 * @param isPrimaryKey
	 * @return
	 */
	public DatabaseColumn getNewColumn(String name, int type, int length,
                                       boolean isPrimaryKey);
	
	/**
	 * This method returns a {@link DatabaseColumn} containing the passed in parameters
	 *
	 * @param name
	 * @param type
	 * @param length
	 * @param isPrimaryKey
	 * @param isNullable
	 * @return
	 */
	public DatabaseColumn getNewColumn(String name, int type, int length,
                                       boolean isPrimaryKey, boolean isNullable);
	
	/**
	 * This method returns a {@link DatabaseColumn} containing the passed in parameters
	 *
	 * @param name
	 * @param type
	 * @param length
	 * @param isPrimaryKey
	 * @param isNullable
	 * @param isAutoIncrement
	 * @return
	 */
	public DatabaseColumn getNewColumn(String name, int type, int length,
                                       boolean isPrimaryKey, boolean isNullable, boolean isAutoIncrement);
}
