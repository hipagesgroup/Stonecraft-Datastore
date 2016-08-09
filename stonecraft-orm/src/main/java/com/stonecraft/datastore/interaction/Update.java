package com.stonecraft.datastore.interaction;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
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
public class Update<T> extends Statement {
	private Map<String, Object> myValues;
	private String myWhereClause;
	private List<String> myArguments;
	private T myUpdateClass;

	public Update(String tableName, Map<String, Object> values) {
		super(tableName);
		myValues = values;
	}

	public Update (String tableName, T updateClass) {
		super(tableName);
		myArguments = new ArrayList<String>();
		myUpdateClass = updateClass;
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
	public List<String> getArguments() {
		return myArguments;
	}

	public T getUpdateClass() {
		return myUpdateClass;
	}

	/**+
	 * A filter declaring which rows to return, formatted as an SQL
	 * WHERE clause (excluding the WHERE itself). Passing null will
	 * return all rows for the given table.
	 *
	 * The '?' character can be used in place of a parameter value if a paraterised
	 * query is going to be used. Use addArgument to set the values for the query
	 *
	 * @param where
	 * @return
	 */
	public Update whereClause(String where) {
		myWhereClause = where;
		return this;
	}

	/**
	 * You may include ?s in selection, which will be replaced by the
	 * values from selectionArgs, in order that they appear in the
	 * selection. The values will be bound as Strings.
	 *
	 * @param arg
	 * @return
	 */
	public Update addArgument(String arg) {
		myArguments.add(arg);
		return this;
	}

	@Override
	public String toString() {
		if(myValues != null) {
			StringBuilder sb = new StringBuilder();
			for(Map.Entry<String, Object> entry : myValues.entrySet()) {
				if(!TextUtils.isEmpty(sb.toString())){
					sb.append(", ");
				}
				sb.append(entry.getKey() + " " + entry.getValue());
			}
			return myValues.toString();
		} else {
			return myUpdateClass.toString();
		}
	}
}
