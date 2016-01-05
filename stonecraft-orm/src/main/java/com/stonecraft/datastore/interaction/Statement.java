package com.stonecraft.datastore.interaction;

/**
 * This class is the base for all statements. It is a POJO for the table the
 * statement is to be run on
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created Jun 27, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class Statement {
	private String myTable;

	/**
	 * Constructor
	 */
	public Statement(String table) {
		myTable = table;
	}

	/**
	 * @return the table
	 */
	public String getTable() {
		return myTable;
	}
	
}
