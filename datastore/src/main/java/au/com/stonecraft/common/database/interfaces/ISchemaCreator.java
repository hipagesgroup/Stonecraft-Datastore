package au.com.stonecraft.common.database.interfaces;

import au.com.stonecraft.common.database.DatabaseSchema;

/**
 * This interface is used to get the DB schema that is when creating a database.
 * It also contains the postCreation method that is call after the DB has been
 * created successfully
 * 
 * @author Michael Delaney
 * @author $Author: michael.delaney $
 * @created March 16, 2012
 * @date $Date: 16/03/2012 01:50:39 $
 * @version $Revision: 1.0 $
 */
public interface ISchemaCreator {
	public DatabaseSchema getSchema();

	public void postCreation();
}
