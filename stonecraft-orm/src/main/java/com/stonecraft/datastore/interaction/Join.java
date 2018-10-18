/**
 * 
 */
package com.stonecraft.datastore.interaction;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * This class
 *
 * @author mdelaney
 * @author Author: michael.delaney
 * @created Dec 5, 2013
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class Join {
	
	/*
	 * A cross join will return all permutations between the rows in the two joined tables.
	 * This type of join should not have a join expression
	 */
	public static final int JOIN_CROSS = 0;
	/*
	 * An inner join will only return the rows where a record has been matched with a record
	 * in the joined table based on the join expression
	 */
	public static final int JOIN_INNER = 1;
	/*
	 * A left outer join will display all records from the left hand table in the join expression
	 * and only the records from the other table that matches a record from the left table.
	 */
	public static final int JOIN_LEFT_OUTER = 2;

	private String myTable;
	private int myJoinType;
	private List<JoinExpression> myJoinExpressions;
	
	public Join(String table, int joinType) {
		myJoinExpressions = new ArrayList<JoinExpression>();
		myTable = table;
		myJoinType = joinType;
	}

    public Join(Join join) {
	    myTable = join.myTable;
	    myJoinType = join.myJoinType;
        myJoinExpressions = new ArrayList<>(join.getJoinExpressions().size());
        for(JoinExpression joinExpression : join.myJoinExpressions) {
            myJoinExpressions.add(new JoinExpression(joinExpression));
        }
    }

    /**
	 * @return the table
	 */
	public String getTable() {
		return myTable;
	}
	/**
	 * @param table the table to set
	 */
	public Join setTable(String table) {
		myTable = table;
        return this;
	}
	/**
	 * @return the joinType
	 */
	public int getJoinType() {
		return myJoinType;
	}
	/**
	 * @param joinType the joinType to set
	 */
	public Join setJoinType(int joinType) {
		myJoinType = joinType;
        return this;
	}
	/**
	 * @return the leftJoinColumn
	 */
	public Join addJoinExpression(Pair<String, String> leftCol, Pair<String, String> rightCol) {
		myJoinExpressions.add(new JoinExpression(leftCol, rightCol));
		return this;
	}
	
	public List<JoinExpression> getJoinExpressions() {
		return myJoinExpressions;
	}

	public static class JoinExpression {
		private Pair<String, String> myLeftColumn;
		private Pair<String, String> myRightColumn;
		public JoinExpression(Pair<String, String> leftColumn,
                              Pair<String, String> rightColumn) {
			myLeftColumn = leftColumn;
			myRightColumn = rightColumn;
		}

        public JoinExpression(JoinExpression expression) {
            myLeftColumn = new Pair<>(expression.myLeftColumn.first,
                    expression.myLeftColumn.second);
            myRightColumn = new Pair<>(expression.myRightColumn.first,
                    expression.myRightColumn.second);
        }

        /**
		 * @return the leftColumn
		 */
		public Pair<String, String> getLeftColumn() {
			return myLeftColumn;
		}
		/**
		 * @return the rightColumn
		 */
		public Pair<String, String> getRightColumn() {
			return myRightColumn;
		}
	}
}
