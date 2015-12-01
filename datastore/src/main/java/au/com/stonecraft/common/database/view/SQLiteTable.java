package au.com.stonecraft.common.database.view;


import android.net.Uri;

import au.com.stonecraft.common.database.exceptions.DatabaseException;
import au.com.stonecraft.common.database.utils.StringUtils;

/**
 * This class represents a SQLite database table. It contains the specifics for
 * creating a table in a mySQL DB
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class SQLiteTable extends DatabaseTable {
	public SQLiteTable(String name, Uri uri) {
		super(name, uri);
	}

	@Override
	/**
	 * This method returns a string that is used to create a table in a SQLite DB
	 */
	public String getCreateTableStmt() throws DatabaseException {
		boolean hasCompositeKey = hasCompositeKey();
		StringBuilder builder = new StringBuilder();
		builder.append(CREATE_TABLE).append(" ");
		
		builder.append(getDBName()).append("(");

		StringBuilder columnsString = new StringBuilder();
		for (DatabaseColumn col : myColumns.values()) {
			columnsString.append(col.getCreateColumnStmt(hasCompositeKey))
					.append(",");
		}

		builder.append(StringUtils.removeStringSuffix(columnsString.toString(),
				","));

		if (hasCompositeKey) {
			builder.append(",").append(getPrimaryKeyString());
		}
		builder.append(")");

		return builder.toString();
	}

	/**
	 * This method checks if this table contains a composite key
	 */
	public boolean hasCompositeKey() {
		int primaryKeyCount = 0;
		for (DatabaseColumn col : myColumns.values()) {
			if (col.isPrimarykey()) {
				primaryKeyCount += 1;
			}
		}

		return primaryKeyCount > 1;
	}

	/**
	 * This method returns the string required to a make a column or columns a
	 * primary key.
	 * 
	 * @return
	 */
	private String getPrimaryKeyString() {
		StringBuilder builder = new StringBuilder();
		builder.append(SQLiteColumn.PRIMARY_KEY).append("(");

		StringBuilder columns = new StringBuilder();
		for (DatabaseColumn col : myColumns.values()) {
			if (col.isPrimarykey()) {
				columns.append(col.getName()).append(",");
			}

		}

		builder.append(StringUtils.removeStringSuffix(columns.toString(), ","))
				.append(")");

		return builder.toString();
	}
}
