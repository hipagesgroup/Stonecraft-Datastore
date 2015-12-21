package com.stonecraft.datastore.interaction;

import java.util.List;

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
	private List<String> myArguments;

	public Delete(String tableName) {
		super(tableName);
	}

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
						 List<String> whereArgs) {
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
	public List<String> getArguments() {
		return myArguments;
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
	public Delete whereClause(String where) {
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
	public Delete addArgument(String arg) {
		myArguments.add(arg);
		return this;
	}
}
