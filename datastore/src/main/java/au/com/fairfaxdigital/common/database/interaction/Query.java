package au.com.fairfaxdigital.common.database.interaction;

import android.util.Pair;

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
public class Query extends Statement {
	private Boolean myIsdistinct;
	private String[] myColumns;
	private String myWhereClause;
	private Pair<String, String>[] myArguments;
	private String myGroupBy;
	private String myHaving;
	private String myOrderBy;
	private String myLimit;

	/**
	 * This constructor creates an instance of Query that can be used with
	 * Datastore to run a query on a database.
	 * 
	 * @param tableName
	 *            The table name to compile the query against.
	 * @param whereClause
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause (excluding the WHERE itself). Passing null will
	 *            return all rows for the given table.
	 */
	public Query(String tableName, String whereClause) {
		this(tableName, whereClause, null);
	}

	/**
	 * This constructor creates an instance of Query that can be used with
	 * Datastore to run a query on a database.
	 * 
	 * @param tableName
	 *            The table name to compile the query against.
	 * @param whereClause
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause (excluding the WHERE itself). Passing null will
	 *            return all rows for the given table.
	 * @param selectionArgs
	 *            You may include ?s in selection, which will be replaced by the
	 *            values from selectionArgs, in order that they appear in the
	 *            selection. The values will be bound as Strings.
	 */
	public Query(String tableName, String whereClause,
        Pair<String, String>[] selectionArgs) {
		this(tableName, null, whereClause, selectionArgs);
	}

	/**
	 * This constructor creates an instance of Query that can be used with
	 * Datastore to run a query on a database.
	 * 
	 * @param tableName
	 *            The table name to compile the query against.
	 * @param columns
	 *            A list of which columns to return. Passing null will return
	 *            all columns, which is discouraged to prevent reading data from
	 *            storage that isn't going to be used.
	 * @param whereClause
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause (excluding the WHERE itself). Passing null will
	 *            return all rows for the given table.
	 * @param selectionArgs
	 *            You may include ?s in selection, which will be replaced by the
	 *            values from selectionArgs, in order that they appear in the
	 *            selection. The values will be bound as Strings.
	 */
	public Query(String tableName, String[] columns, String whereClause,
        Pair<String, String>[] selectionArgs) {
		this(tableName, columns, whereClause, selectionArgs, null, null, null);
	}

	/**
	 * This constructor creates an instance of Query that can be used with
	 * Datastore to run a query on a database.
	 * 
	 * @param tableName
	 *            The table name to compile the query against.
	 * @param columns
	 *            A list of which columns to return. Passing null will return
	 *            all columns, which is discouraged to prevent reading data from
	 *            storage that isn't going to be used.
	 * @param whereClause
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause (excluding the WHERE itself). Passing null will
	 *            return all rows for the given table.
	 * @param selectionArgs
	 *            You may include ?s in selection, which will be replaced by the
	 *            values from selectionArgs, in order that they appear in the
	 *            selection. The values will be bound as Strings.
	 * @param groupBy
	 *            A filter declaring how to group rows, formatted as an SQL
	 *            GROUP BY clause (excluding the GROUP BY itself). Passing null
	 *            will cause the rows to not be grouped.
	 * @param having
	 *            A filter declare which row groups to include in the cursor, if
	 *            row grouping is being used, formatted as an SQL HAVING clause
	 *            (excluding the HAVING itself). Passing null will cause all row
	 *            groups to be included, and is required when row grouping is
	 *            not being used.
	 * @param orderBy
	 *            How to order the rows, formatted as an SQL ORDER BY clause
	 *            (excluding the ORDER BY itself). Passing null will use the
	 *            default sort order, which may be unordered.
	 */
	public Query(String tableName, String[] columns, String whereClause,
        Pair<String, String>[] selectionArgs, String groupBy, String having,
			String orderBy) {
		this(tableName, columns, whereClause, selectionArgs, groupBy, having,
				orderBy, null);
	}

	/**
	 * This constructor creates an instance of Query that can be used with
	 * Datastore to run a query on a database.
	 * 
	 * @param tableName
	 *            The table name to compile the query against.
	 * @param columns
	 *            A list of which columns to return. Passing null will return
	 *            all columns, which is discouraged to prevent reading data from
	 *            storage that isn't going to be used.
	 * @param whereClause
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause (excluding the WHERE itself). Passing null will
	 *            return all rows for the given table.
	 * @param selectionArgs
	 *            You may include ?s in selection, which will be replaced by the
	 *            values from selectionArgs, in order that they appear in the
	 *            selection. The values will be bound as Strings.
	 * @param groupBy
	 *            A filter declaring how to group rows, formatted as an SQL
	 *            GROUP BY clause (excluding the GROUP BY itself). Passing null
	 *            will cause the rows to not be grouped.
	 * @param having
	 *            A filter declare which row groups to include in the cursor, if
	 *            row grouping is being used, formatted as an SQL HAVING clause
	 *            (excluding the HAVING itself). Passing null will cause all row
	 *            groups to be included, and is required when row grouping is
	 *            not being used.
	 * @param orderBy
	 *            How to order the rows, formatted as an SQL ORDER BY clause
	 *            (excluding the ORDER BY itself). Passing null will use the
	 *            default sort order, which may be unordered.
	 * @param limit
	 *            Limits the number of rows returned by the query, formatted as
	 *            LIMIT clause. Passing null denotes no LIMIT clause.
	 */
	public Query(String tableName, String[] columns, String whereClause,
        Pair<String, String>[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		this(false, tableName, columns, whereClause, selectionArgs, groupBy,
				having, orderBy, limit);
	}

	/**
	 * This constructor creates an instance of Query that can be used with
	 * Datastore to run a query on a database.
	 * 
	 * @param isDistinct
	 *            true if you want each row to be unique, false otherwise.
	 * @param tableName
	 *            The table name to compile the query against.
	 * @param columns
	 *            A list of which columns to return. Passing null will return
	 *            all columns, which is discouraged to prevent reading data from
	 *            storage that isn't going to be used.
	 * @param whereClause
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause (excluding the WHERE itself). Passing null will
	 *            return all rows for the given table.
	 * @param selectionArgs
	 *            You may include ?s in selection, which will be replaced by the
	 *            values from selectionArgs, in order that they appear in the
	 *            selection. The values will be bound as Strings.
	 * @param groupBy
	 *            A filter declaring how to group rows, formatted as an SQL
	 *            GROUP BY clause (excluding the GROUP BY itself). Passing null
	 *            will cause the rows to not be grouped.
	 * @param having
	 *            A filter declare which row groups to include in the cursor, if
	 *            row grouping is being used, formatted as an SQL HAVING clause
	 *            (excluding the HAVING itself). Passing null will cause all row
	 *            groups to be included, and is required when row grouping is
	 *            not being used.
	 * @param orderBy
	 *            How to order the rows, formatted as an SQL ORDER BY clause
	 *            (excluding the ORDER BY itself). Passing null will use the
	 *            default sort order, which may be unordered.
	 * @param limit
	 *            Limits the number of rows returned by the query, formatted as
	 *            LIMIT clause. Passing null denotes no LIMIT clause.
	 */
	public Query(Boolean isDistinct, String tableName, String[] columns,
			String whereClause, Pair<String, String>[] selectionArgs, String groupBy,
			String having, String orderBy, String limit) {
		super(tableName);
		myIsdistinct = isDistinct;
		myColumns = columns;
		myWhereClause = whereClause;
		myArguments = selectionArgs;
		myGroupBy = groupBy;
		myHaving = having;
		myOrderBy = orderBy;
		myLimit = limit;
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
	public Pair<String, String>[] getSelectionArgs() {
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
}
