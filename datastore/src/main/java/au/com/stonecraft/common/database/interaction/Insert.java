package au.com.stonecraft.common.database.interaction;

import java.util.Map;

/**
 * This class contains the required data to perform an insert on a Database. An
 * object of this class is passed to Datastore to perform the insert
 * 
 * @author Michael Delaney
 * @author $Author: michael.delaney $
 * @created 16/03/2012
 * @date $Date: 16/03/2012 01:50:39 $
 * @version $Revision: 1.0 $
 */
public class Insert<T> extends Statement {
	private Map<String, Object> myValues;
	private T myRowObject;

	public Insert(String tableName, T rowObject) {
		super(tableName);
		myRowObject = rowObject;
	}

	public Insert(String tableName, Map<String, Object> values) {
		super(tableName);
		myValues = values;
	}

	public Map<String, Object> getValues() {
		return myValues;
	}

	public T getInsertRowClasses() {
		return myRowObject;
	}
}
