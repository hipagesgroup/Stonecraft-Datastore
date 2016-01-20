package com.stonecraft.datastore;

import java.util.ArrayList;
import java.util.List;

/**
 * This interface contains the methods required for creating the DB Statements
 * in a syntax the DB understands
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class AggregateQuery {
    private String myTable;
    private String myWhereClause;
    private List<String> myArguments;

    public AggregateQuery(String tableName) {
        myTable = tableName;
        myArguments = new ArrayList<String>();
    }

    /**
     * @return the table
     */
    public String getTable() {
        return myTable;
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
     * You may include ?s in selection, which will be replaced by the
     * values from selectionArgs, in order that they appear in the
     * selection. The values will be bound as Strings.
     *
     * @param arg
     * @return
     */
    public AggregateQuery addArgument(String arg) {
        myArguments.add(arg);
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
    public AggregateQuery whereClause(String where) {
        myWhereClause = where;
        return this;
    }
}
