package au.com.stonecraft.common.database.interaction;

import java.util.ArrayList;
import java.util.List;

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
	private List<Join> myJoins;

	/**
	 * Constructor
	 */
	public Statement(String table) {
		myTable = table;
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
	 * @param joins the joins to set
	 */
	public void addJoins(Join join) {
		myJoins.add(join);
	}
	
	
}
