package au.com.fairfaxdigital.common.database.interaction;

import java.util.ArrayList;
import java.util.List;
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
public class Insert extends Statement {
	private List<Map<String, Object>> myValues;

	public Insert(String tableName, Map<String, Object> values) {
		super(tableName);
		myValues = new ArrayList<Map<String, Object>>();
		myValues.add(values);
	}

	public Insert(String tableName, List<Map<String, Object>> values) {
		super(tableName);
		myValues = values;
	}

	public List<Map<String, Object>> getValues() {
		return myValues;
	}
}
