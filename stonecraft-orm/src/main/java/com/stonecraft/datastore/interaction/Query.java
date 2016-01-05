package com.stonecraft.datastore.interaction;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the required data to perform a query on a Database. An
 * object of this class is passed to Datastore to perform the query
 * 
 * @author Michael Delaney
 * @author $Author: michael.delaney $
 * @created 16/03/2012
 * @date $Date: 16/03/2012 01:50:39 $
 * @version $Revision: 1.0 $
 */
public class Query {
	private Boolean myIsdistinct;
	private String[] myColumns;
	private String myWhereClause;
	private List<String> myArguments;
	private String myGroupBy;
	private String myHaving;
	private String myOrderBy;
	private String myLimit;
	private String myTable;
	private List<Join> myJoins;

	public Query (String tableName) {
		myTable = tableName;
		myArguments = new ArrayList<String>();
		myJoins = new ArrayList<Join>();
	}

	/**
	 * @return the table
	 */
	public String getTable() {
		return myTable;
	}

	/**
	 * @return the joins
	 */
	public List<Join> getJoins() {
		return myJoins;
	}

	/**
	 * the joins to set
	 *
	 * @param join
	 */
	public void addJoins(Join join) {
		myJoins.add(join);
	}

	/**
	 * This method returnes whether this statement is to be distinct
	 *
	 * @return
	 */
	public boolean isdistinct() {
		// This query defaults to false if null was passed in.
		if (myIsdistinct == null) {
			return false;
		}
		return myIsdistinct;
	}

	/**
	 * This method returns the columns this query will contain in it's result
	 * set
	 *
	 * @return
	 */
	public String[] getColumns() {
		return myColumns;
	}

	/**
	 * @return the whereClause
	 */
	public String getWhereClause() {
		return myWhereClause;
	}

	/**
	 * @return the selection arguments
	 */
	public List<String> getSelectionArgs() {
		return myArguments;
	}

	/**
	 * @return the group by query
	 */
	public String getGroupBy() {
		return myGroupBy;
	}

	/**
	 * @return returns the having query
	 */
	public String getHaving() {
		return myHaving;
	}

	/**
	 * @return the order by query
	 */
	public String getOrderBy() {
		return myOrderBy;
	}

	/**
	 * @return the limit of this query
	 */
	public String getLimit() {
		return myLimit;
	}

	/**
	 * true if you want each row to be unique, false otherwise.
	 *
	 * @param isDistinct
	 * @return
	 */
	public Query distinct(boolean isDistinct) {
		myIsdistinct = isDistinct;
		return this;
	}

	/**
	 * A list of which columns to returned. Passing null will return
	 * all columns, which is discouraged to prevent reading data from
	 * storage that isn't going to be used.
	 *
	 * There is no need to set the columns if a query result class is set as the annotated
	 * fields in that class will be used instead.
	 *
	 * @param columns
	 * @return
	 */
	public Query columns(String[] columns) {
		myColumns = columns;
		return this;
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
	public Query whereClause(String where) {
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
	public Query addArgument(String arg) {
		myArguments.add(arg);
		return this;
	}

	/**
	 *  A filter declaring how to group rows, formatted as an SQL
	 *  GROUP BY clause (excluding the GROUP BY itself). Passing null
	 *  will cause the rows to not be grouped.
	 *
	 * @param groupBy
	 * @return
	 */
	public Query groupBy(String groupBy) {
		myGroupBy = groupBy;
		return this;
	}

	/**
	 * A filter declare which row groups to include in the cursor, if
	 * row grouping is being used, formatted as an SQL HAVING clause
	 * (excluding the HAVING itself). Passing null will cause all row
	 * groups to be included, and is required when row grouping is
	 * not being used.
	 *
	 * @param having
	 * @return
	 */
	public Query having(String having) {
		myHaving = having;
		return this;
	}

	/**
	 * How to order the rows, formatted as an SQL ORDER BY clause
	 * (excluding the ORDER BY itself). Passing null will use the
	 * default sort order, which may be unordered.
	 *
	 * @param orderBy
	 * @return
	 */
	public Query orderBy(String orderBy) {
		myOrderBy = orderBy;
		return this;
	}

	/**
	 * Limits the number of rows returned by the query, formatted as
	 * LIMIT clause. Passing null denotes no LIMIT clause.
	 *
	 * @param limit
	 * @return
	 */
	public Query limit(String limit) {
		myLimit = limit;
		return this;
	}
}
