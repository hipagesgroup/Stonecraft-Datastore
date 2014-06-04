package au.com.fairfaxdigital.android.database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Pair;

import au.com.fairfaxdigital.common.database.DBConstants;
import au.com.fairfaxdigital.common.database.DatabaseSchema;
import au.com.fairfaxdigital.common.database.RSData;
import au.com.fairfaxdigital.common.database.exceptions.DatabaseException;
import au.com.fairfaxdigital.common.database.interaction.Delete;
import au.com.fairfaxdigital.common.database.interaction.Insert;
import au.com.fairfaxdigital.common.database.interaction.Join;
import au.com.fairfaxdigital.common.database.interaction.Join.JoinExpression;
import au.com.fairfaxdigital.common.database.interaction.Query;
import au.com.fairfaxdigital.common.database.interaction.Statement;
import au.com.fairfaxdigital.common.database.interaction.Update;
import au.com.fairfaxdigital.common.database.interfaces.IDBConnector;
import au.com.fairfaxdigital.common.database.interfaces.ISchemaCreator;
import au.com.fairfaxdigital.common.database.utils.StringUtils;
import au.com.fairfaxdigital.common.database.view.DatabaseColumn;
import au.com.fairfaxdigital.common.database.view.DatabaseTable;
import au.com.fairfaxdigital.common.database.view.DatabaseViewFactory;
import au.com.fairfaxdigital.common.database.view.SqliteDBViewFactory;

/**
 * This class is the database connector for all Android database connection
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class AndroidDBConnection implements IDBConnector {
	
	public static final String TABLE_SQLITE_MASTER = "sqlite_master";
	public static final String TABLE_ANDROID_METADATA = "android_metadata";
	public static final String TABLE_SQLITE_SEQUENCE = "sqlite_sequence";
	public static final String COL_SQL = "sql";
	
	private static final String QUERY_TABLE_EXISTS = "SELECT DISTINCT tbl_name FROM "
		+ "sqlite_master WHERE tbl_name = '%s'";
	private static final String STATEMENT_COPY_TABLE = "INSERT INTO %s (%s) SELECT %s FROM %s";
	private static final String STATEMENT_RENAME_TABLE = "ALTER TABLE %s RENAME TO %s";
	private static final String STATEMENT_DROP_TABLE = "DROP TABLE %s";
	
	private static final String JOIN_CROSS_STRING = " CROSS JOIN ";
	private static final String JOIN_INNER_STRING = " INNER JOIN ";
	private static final String JOIN_LEFT_OUTER_STRING = " LEFT OUTER JOIN ";
	
	private DatabaseHelper myDBOpenHelper;
	private String myName;
	private int myCurrentVersion;

	public AndroidDBConnection(Context context, ISchemaCreator schemaImporter) {
		DatabaseSchema schema = schemaImporter.getSchema();
		myName = schema.getName();
		myCurrentVersion = schema.getVersion();
		myDBOpenHelper = new DatabaseHelper(context, myName, myCurrentVersion,
			this, schema);
		schemaImporter.postCreation();
	}

	public String getName() {
		return myName;
	}

	public int getVersion() {
		return myCurrentVersion;
	}

	public void startTransaction() throws DatabaseException {
		myDBOpenHelper.getReadableDatabase().beginTransaction();

	}

	public void commit() throws DatabaseException {
		myDBOpenHelper.getReadableDatabase().setTransactionSuccessful();
		myDBOpenHelper.getReadableDatabase().endTransaction();
	}

	public void rollBack() throws DatabaseException {
		myDBOpenHelper.getReadableDatabase().endTransaction();

	}

	public void close() throws DatabaseException {
		myDBOpenHelper.getReadableDatabase().close();
	}

	public boolean isOpen() throws DatabaseException {
		return myDBOpenHelper.getReadableDatabase().isOpen();
	}

	public RSData query(Query query) throws DatabaseException {
		try {
			Cursor cursor = null;
			if(query.getJoins().isEmpty()){
				cursor = myDBOpenHelper.getReadableDatabase().query(
					query.isdistinct(), query.getTable(), query.getColumns(),
					query.getWhereClause(),
					getArguments(query.getSelectionArgs()), query.getGroupBy(),
					query.getHaving(), query.getOrderBy(), query.getLimit());
			} else {
				String queryString = getSQLJoinQuery(query);
				cursor = myDBOpenHelper.getReadableDatabase().rawQuery(queryString, null);
			}

			return new QueryRSData(cursor);
		} catch (SQLiteException e) {
			throw new DatabaseException(StringUtils.EmptyString, e);
		}
	}

	public int insert(Insert insert) throws DatabaseException {
		List<ContentValues> rows = new ArrayList<ContentValues>();
		List<Map<String, Object>> insertRecordMap = insert.getValues();
		for (Map<String, Object> recordMap : insertRecordMap) {
			rows.add(getContentValues(recordMap.entrySet()));
		}

		//used for error logging
		ContentValues currentValue = null;
		try {
			startTransaction();
			SQLiteDatabase db = myDBOpenHelper.getWritableDatabase();
			for (ContentValues cv : rows) {
				currentValue = cv;
				db.insertOrThrow(insert.getTable(), null, cv);
			}
			commit();
		} catch (SQLiteException e) {
			rollBack();
			throw new DatabaseException("An error occured while attempting "
					+ "to insert records into table " + insert.getTable() 
					+ " and values '" + currentValue + "' [" + e + "]", e);
		}
		return rows.size();
	}

	public int update(Update update) throws DatabaseException {
		int updateCount = myDBOpenHelper.getWritableDatabase().update(
				update.getTable(),
				getContentValues(update.getValues().entrySet()),
				update.getWhereClause(), getArguments(update.getArguments()));

		return updateCount;
	}

	public int delete(Delete delete) throws DatabaseException {
		int deleteCount = myDBOpenHelper.getWritableDatabase().delete(
				delete.getTable(), delete.getWhereClause(),
				getArguments(delete.getArguments()));

		return deleteCount;
	}

	public void executeRawStatement(String stmt) throws DatabaseException {
		myDBOpenHelper.getReadableDatabase().execSQL(stmt);
	}
	
	/* (non-Javadoc)
	 * @see au.com.fairfaxdigital.common.database.interfaces.IDBConnector#executeRawQuery(java.lang.String)
	 */
	@Override
	public RSData executeRawQuery(String stmt) throws DatabaseException {
		Cursor c = myDBOpenHelper.getReadableDatabase().rawQuery(stmt, null);
		return new QueryRSData(c);
	}
	
	/* (non-Javadoc)
	 * @see au.com.fairfaxdigital.common.database.interfaces.IDBConnector#doesTableExist(java.lang.String)
	 */
	@Override
	public int doesTableExist(String tableName) throws DatabaseException {
		String stmt = String.format(QUERY_TABLE_EXISTS, tableName);
		RSData resultSet = executeRawQuery(stmt);
		
		int tableCount = resultSet.getCount();
		resultSet.close();
		
		return tableCount;
	}

	/**
	 * This method is not used in Android as it is handled by the SQLite open
	 * helper
	 */
	public void createConnection() throws DatabaseException {
		if(!isOpen()) {
			myDBOpenHelper.reconnect();
		}
	}
	
	/* (non-Javadoc)
	 * @see au.com.fairfaxdigital.common.database.interfaces.IDBConnector#getDatabaseSchema()
	 */
	@Override
	public DatabaseSchema getDatabaseSchema() {
		return myDBOpenHelper.getDatabaseSchema();
	}

	/**
	 * This method is not used in Android as it is handled by the SQLite open
	 * helper
	 */
	public void createSchema(ISchemaCreator database) throws DatabaseException {
		// DO NOTHING
		// the creation of the schema is done by the DBHelper in the constructor
	}
	
	/* (non-Javadoc)
	 * @see au.com.fairfaxdigital.common.database.interfaces.IDBConnector#getTableObjectFactory()
	 */
	@Override
	public DatabaseViewFactory getTableObjectFactory() {
		return new SqliteDBViewFactory();
	}
	
	/* (non-Javadoc)
	 * @see au.com.fairfaxdigital.common.database.interfaces.IDBConnector#updateTable(au.com.fairfaxdigital.common.database.view.DatabaseTable)
	 */
	@Override
	public void updateTable(DatabaseTable oldTable, DatabaseTable newTable) throws DatabaseException {
		newTable.setTempTable(true);
		//create new table
		executeRawStatement(newTable.getCreateTableStmt());
		
		String columnString = getMapedColumnString(oldTable, newTable);
		//copy content of old table into new table.
		executeRawStatement(String.format(STATEMENT_COPY_TABLE, newTable.getDBName(), 
			columnString, columnString, oldTable.getName()));
		//drop old table
		executeRawStatement(String.format(STATEMENT_DROP_TABLE, oldTable.getName()));
		//rename new table to match the old tables name
		executeRawStatement(String.format(STATEMENT_RENAME_TABLE, newTable.getDBName(), oldTable.getName()));
	}
	
	/**
	 * This method checks the passed in columns if there has been any modification done.
	 * 
	 * If there is a modification it will check if the modification passes the modification
	 * rules. A {@link DatabaseException} is thrown if the modification rules are not met.
	 *
	 * @param column
	 * @param newSchemaColumn
	 * @return
	 * @throws DatabaseException
	 */
	public int checkColumnUpdateRules(DatabaseColumn column,
		DatabaseColumn newSchemaColumn) {
		
		if(column.equals(newSchemaColumn)){
			return CHANGE_NONE;
		}
		else if(checkTypeUpgradeRules(column, newSchemaColumn)){
				return CHANGE_ALLOWED;
		}
		else if(column.getType() == newSchemaColumn.getType() &&
			column.isPrimarykey() == newSchemaColumn.isPrimarykey()) {
			if(column.isNullable() == !newSchemaColumn.isNullable()) {
				return CHANGE_EXCEPTION;
			}
			return CHANGE_ALLOWED;
		}
		
		return CHANGE_EXCEPTION;
	}
	
	private boolean checkTypeUpgradeRules(DatabaseColumn column,
		DatabaseColumn newSchemaColumn) {
		if(column.getType() != newSchemaColumn.getType()) {
			if(newSchemaColumn.getType() == DBConstants.DATATYPE_INT_STRING){
				return true;
			} else if((column.getType() == DBConstants.DATATYPE_INT_INTEGER ||
				column.getType() == DBConstants.DATATYPE_INT_BOOLEAN ||
				column.getType() == DBConstants.DATATYPE_INT_DATETIME) &&
				(newSchemaColumn.getType() == DBConstants.DATATYPE_INT_INTEGER ||
					newSchemaColumn.getType() == DBConstants.DATATYPE_INT_BOOLEAN ||
						newSchemaColumn.getType() == DBConstants.DATATYPE_INT_DATETIME)){
				return true;
			}
		}
		return false;
	}

	/**
	 * This method compares the oldtable with the new table and creates a delimited string
	 * containing the columns that are to be copied across to the new table
	 *
	 * @param oldTable
	 * @param newTable
	 * @return
	 */
	private String getMapedColumnString(DatabaseTable oldTable,
		DatabaseTable newTable) {
		
		List<String>columns = new ArrayList<String>();
		for(Entry<String, DatabaseColumn> entry : oldTable.getColumns().entrySet()){
			if(newTable.getColumns().containsKey(entry.getKey()))
			columns.add(entry.getValue().getName());
		}
		
		return StringUtils.convertListToDelimitedString(columns, StringUtils.COMMA);
	}

	/**
	 * This method creates the content values from an EntrySet
	 * 
	 * The key of the set should be the column name with the value being the
	 * value for that row
	 * 
	 * @param row
	 * @return
	 */
	private ContentValues getContentValues(Set<Entry<String, Object>> row)
			throws DatabaseException {
		ContentValues cv = new ContentValues();
		for (Entry<String, Object> entrySet : row) {
			Object value = entrySet.getValue();
			if (value != null) {
				if (value instanceof Integer) {
					cv.put(entrySet.getKey(), (Integer) value);
				} else if (value instanceof Boolean) {
					cv.put(entrySet.getKey(), (Boolean) value);
				} else if (value instanceof Double) {
					cv.put(entrySet.getKey(), (Double) value);
				} else if (value instanceof Float) {
					cv.put(entrySet.getKey(), (Float) value);
				} else if (value instanceof Long) {
					cv.put(entrySet.getKey(), (Long) value);
				} else if (value instanceof String) {
					cv.put(entrySet.getKey(), (String) value);
				} else if (value instanceof Date) {
					long timeInMillis = ((Date) value).getTime();
					cv.put(entrySet.getKey(), timeInMillis);
				} else if (value instanceof Calendar) {
					long timeInMillis = ((Calendar) value).getTimeInMillis();
					cv.put(entrySet.getKey(), timeInMillis);
				}
				else if(value instanceof byte[]) {
					cv.put(entrySet.getKey(), (byte[]) value);
				}
				else {
					throw new DatabaseException("Datatype "
							+ value.getClass().getName() + " is not a valid "
							+ "datatype");
				}
			}
		}

		return cv;
	}

	/**
	 * This method converts a nameValuePair array into a string array.
	 * 
	 * @param args
	 * @return
	 */
	private String[] getArguments(Pair<String, String>[] args) {
		if (args == null) {
			return null;
		}

		String[] returnArgs = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			returnArgs[i] = args[i].second;
		}

		return returnArgs;
	}
	
	/**
	 * This method
	 *
	 * @param query
	 * @return
	 */
	private String getSQLJoinQuery(Query query) {
		StringBuilder statementBuilder = new StringBuilder();
		
		statementBuilder.append(DBConstants.SELECT);
		StringBuilder colBuilder = new StringBuilder();
		if(query.getColumns() == null || query.getColumns().length == 0){
			colBuilder.append("*");
		} else {
			
			for(String column : query.getColumns()){
				if(colBuilder.length() > 0){
					colBuilder.append(",");
				}
				colBuilder.append(column);
			}
			statementBuilder.append(" ").append(colBuilder);
		}
		
		if(query.isdistinct()){
			statementBuilder.append(" " + DBConstants.DISTINCT).append(DBConstants.OPEN_BRACKET);
			statementBuilder.append(colBuilder).append(DBConstants.CLOSE_BRACKET + " ");
		} else {
			statementBuilder.append(" " + colBuilder + " ");
		}
		statementBuilder.append(DBConstants.FROM).append(getJoinClause(query.getTable(), query));
		
		if(!StringUtils.isEmpty(query.getWhereClause())){
			statementBuilder.append(" " + DBConstants.WHERE + " " + query.getWhereClause());
		}
		if(!StringUtils.isEmpty(query.getOrderBy())){
			statementBuilder.append(" " + DBConstants.ORDER_BY + " " + query.getOrderBy());
		}
		if(!StringUtils.isEmpty(query.getGroupBy())){
			statementBuilder.append(" " + DBConstants.GROUP_BY + " " + query.getGroupBy());
		}
		if(!StringUtils.isEmpty(query.getHaving())){
			statementBuilder.append(" " + DBConstants.HAVING + " " + query.getHaving());
		}
		if(!StringUtils.isEmpty(query.getLimit())){
			statementBuilder.append(" " + DBConstants.LIMIT + " " + query.getLimit());
		}
		
		return statementBuilder.toString();
	}
	
	private String getJoinClause(String table, Statement statement) {
		StringBuilder joinBuilder = new StringBuilder(" ");
		joinBuilder.append(table);
		
		for(Join join : statement.getJoins()){
			switch(join.getJoinType()) {
				case Join.JOIN_CROSS : {
					joinBuilder.append(JOIN_CROSS_STRING);
					break;
				}
				case Join.JOIN_INNER : {
					joinBuilder.append(JOIN_INNER_STRING);
					break;
				}
				case Join.JOIN_LEFT_OUTER : {
					joinBuilder.append(JOIN_LEFT_OUTER_STRING);
					break;
				}
			}
			joinBuilder.append(join.getTable());
			
			if(join.getJoinType() != Join.JOIN_CROSS){
				joinBuilder.append(" ").append(DBConstants.ON).append(" ");
				
				StringBuilder joinExpressionBuilder = new StringBuilder();
				for(JoinExpression expression : join.getJoinExpressions()){
					if(joinExpressionBuilder.length() > 0){
						joinExpressionBuilder.append(" ").append(DBConstants.AND).append(" ");
					}
					
					joinExpressionBuilder.append(expression.getLeftColumn().first).append(".");
					joinExpressionBuilder.append(expression.getLeftColumn().second);
					
					joinExpressionBuilder.append(" = ");
					
					joinExpressionBuilder.append(expression.getRightColumn().first).append(".");
					joinExpressionBuilder.append(expression.getRightColumn().second);
				}
				
				joinBuilder.append(joinExpressionBuilder);
			}
		}
		
		return joinBuilder.toString();
	}

	/**
	 * This class is the android implementation of RSData
	 * 
	 * @author mdelaney
	 * @author Author: michael.delaney
	 * @created March 16, 2012
	 * @date Date: 16/03/2012 01:50:39
	 * @version Revision: 1.0
	 */
	public class QueryRSData implements RSData {
		private static final int COLUMN_NOT_FOUND = -1;
		private Cursor myCursor;

		QueryRSData(Cursor cursor) {
			myCursor = cursor;
		}

		@Override
		public boolean hasNext() {
			// remove one from count to signify that it is checking the current
			// position is the last element as opposed to isAfterLast() element
			return myCursor.getPosition() < myCursor.getCount() - 1;
		}

		@Override
		public boolean isAfterLast() {
			return myCursor.getPosition() >= myCursor.getCount();

		}

		@Override
		public RSData next() {
			// move the cursor to the next position before returning this
			// object.
			myCursor.moveToNext();
			return this;
		}

		@Override
		public void remove() {
			// cannot remove items from the cursor
			throw new UnsupportedOperationException();

		}

		@Override
		public int getCount() {
			return myCursor.getCount();
		}

		@Override
		public String getStringValue(String column) throws DatabaseException {
			int colIndex = getCursorIndex(column);
			return myCursor.getString(colIndex);
		}

		@Override
		public boolean getBooleanValue(String column) throws DatabaseException {
			int intValue = getIntValue(column);
			return intValue != 0;
		}

		@Override
		public int getIntValue(String column) throws DatabaseException {
			int colIndex = getCursorIndex(column);
			boolean isNull = myCursor.isNull(colIndex);
			return myCursor.getInt(colIndex);
		}

		@Override
		public long getLongValue(String column) throws DatabaseException {
			int colIndex = getCursorIndex(column);
			return myCursor.getLong(colIndex);
		}

		@Override
		public double getDoubleValue(String column) throws DatabaseException {
			int colIndex = getCursorIndex(column);
			return myCursor.getDouble(colIndex);
		}

		@Override
		public Date getDateValue(String column)
				throws DatabaseException {
			int colIndex = getCursorIndex(column);
			long timeInMillis = myCursor.getLong(colIndex);
			return new Date(timeInMillis);
		}
		
		/* (non-Javadoc)
		 * @see au.com.fairfaxdigital.common.database.RSData#getCalendarValue(java.lang.String)
		 */
		@Override
		public Calendar getCalendarValue(String column)
				throws DatabaseException {

			Date date = getDateValue(column);
			
			if(date != null) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				
				return calendar;
			}
			return null;
		}
		
		/* (non-Javadoc)
		 * @see au.com.fairfaxdigital.common.database.RSData#containsNull(java.lang.String)
		 */
		@Override
		public boolean containsNull(String column) throws DatabaseException {
			int colIndex = getCursorIndex(column);
			return myCursor.isNull(colIndex);
		}
		
		/* (non-Javadoc)
		 * @see au.com.fairfaxdigital.common.database.RSData#getBlobData(java.lang.String)
		 */
		@Override
		public byte[] getBlobData(String column) throws DatabaseException {
			int colIndex = getCursorIndex(column);
			return myCursor.getBlob(colIndex);
		}

		@Override
		public void moveToFirst() {
			myCursor.moveToFirst();
		}

		@Override
		public void close() {
			myCursor.close();
		}		

		private int getCursorIndex(String column) throws DatabaseException {
			int colIndex = myCursor.getColumnIndex(column);

			if (colIndex == COLUMN_NOT_FOUND) {
				throw new DatabaseException("The column " + column
						+ " is not a valid column in this result set");
			}

			return colIndex;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see au.com.delaney.datastore.RSData#moveToPosition(int)
		 */
		@Override
		public boolean moveToPosition(int postion) {
			return myCursor.moveToPosition(postion);
		}

		/* (non-Javadoc)
		 * @see au.com.fairfaxdigital.common.database.RSData#hasColumn(java.lang.String)
		 */
		@Override
		public boolean hasColumn(String columnName) {
			return myCursor.getColumnIndex(columnName) >= 0;
		}
	}
}
