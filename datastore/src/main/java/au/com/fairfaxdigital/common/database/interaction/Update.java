package au.com.fairfaxdigital.common.database.interaction;

import android.util.Pair;

import java.util.Map;

/**
 * This class contains the required data to perform an update on a Database. An
 * object of this class is passed to Datastore to perform the update
 * 
 * @author Michael Delaney
 * @author $Author: michael.delaney $
 * @created 16/03/2012
 * @date $Date: 16/03/2012 01:50:39 $
 * @version $Revision: 1.0 $
 */
public class Update extends Statement {
	private Map<String, Object> myValues;
	private String myWhereClause;
	private Pair<String, String>[] myArguments;

	public Update(String tableName, Map<String, Object> values,
			String whereClause, Pair<String, String>[] whereArgs) {
		super(tableName);
		myValues = values;
		myWhereClause = whereClause;
		myArguments = whereArgs;
	}

	/**
	 * @return the values
	 */
	public Map<String, Object> getValues() {
		return myValues;
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
