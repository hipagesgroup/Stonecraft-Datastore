package au.com.fairfaxdigital.common.database.interaction;

import android.util.Pair;

/**
 * This class contains the required data to perform a delete on a Database. An
 * object of this class is passed to Datastore to perform the delete
 * 
 * @author Michael Delaney
 * @author $Author: michael.delaney $
 * @created 16/03/2012
 * @date $Date: 16/03/2012 01:50:39 $
 * @version $Revision: 1.0 $
 */
public class Delete extends Statement {
	private String myWhereClause;
	private Pair<String, String>[] myArguments;

	/**
	 * This constructor creates a Delete object with everything required to
	 * perform a delete. If the Where clause is not a parameterised query, the
	 * whereArgs argument can be null.
	 * 
	 * @param tableName
	 * @param whereClause
	 * @param whereArgs
	 *            A name value pair array. The name of the pair is the data type
	 *            of the argument. The value is the value to appear in place of
	 *            the '?' in the where clause
	 */
	public Delete(String tableName, String whereClause,
        Pair<String, String>[] whereArgs) {
		super(tableName);
		myWhereClause = whereClause;
		myArguments = whereArgs;
	}

	/**
	 * @return the whereClause
	 */
	public String getWhereClause() {
		return myWhereClause;
	}

	/**
	 * @return the arguments
	 */
	public Pair<String, String>[] getArguments() {
		return myArguments;
	}
}
