package com.stonecraft.datastore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.Delete;
import com.stonecraft.datastore.interaction.Insert;
import com.stonecraft.datastore.interaction.Join;
import com.stonecraft.datastore.interaction.Query;
import com.stonecraft.datastore.interaction.Update;
import com.stonecraft.datastore.interfaces.IDBConnector;
import com.stonecraft.datastore.interfaces.ISchemaCreator;
import com.stonecraft.datastore.utils.StringUtils;
import com.stonecraft.datastore.view.DatabaseColumn;
import com.stonecraft.datastore.view.DatabaseTable;
import com.stonecraft.datastore.view.DatabaseViewFactory;
import com.stonecraft.datastore.view.SqliteDBViewFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	private DbSchemaModel myDbSchema;
	private Context myAppContext;
	private Map<Class, ContentValueCreator> myContentValueCreator;

	public AndroidDBConnection(Context context, DbSchemaModel dbSchema,
			OnConnectionCreated listener) {
		myAppContext = context;
		myDbSchema = dbSchema;
		myDBOpenHelper = new DatabaseHelper(context, this, myDbSchema, listener);
        myContentValueCreator = new HashMap<>();
	}

	public String getName() {
		return myDbSchema.getName();
	}

    @Override
    public Context getContext() {
        return myAppContext;
    }

    public int getVersion() {
		return myDbSchema.getVersion();
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

	public void close() {
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

	@Override
	public long queryNumEntries(RowCountQuery query) {
		if(query.getSelectionArgs() != null) {
			List<String> args = query.getSelectionArgs();
			return android.database.DatabaseUtils.queryNumEntries(
					myDBOpenHelper.getReadableDatabase(), query.getTable(), query.getWhereClause(),
					args.toArray(new String[args.size()]));
		}
		return android.database.DatabaseUtils.queryNumEntries(
				myDBOpenHelper.getReadableDatabase(), query.getTable(), query.getWhereClause());
	}

	public void insert(Insert insert) throws DatabaseException {
		ContentValues cv;
		if(insert.getInsertRowClasses() != null) {
            ContentValueCreator cvCreator = myContentValueCreator.get(
                    insert.getInsertRowClasses().getClass());
            if(cvCreator == null) {
                cvCreator = new ContentValueCreator();
                myContentValueCreator.put(insert.getInsertRowClasses().getClass(),
                        cvCreator);
            }
			cv = cvCreator.getContentValues(insert.getInsertRowClasses());
		} else {
			cv = new ContentValueCreator().getContentValues(
                    insert.getValues().entrySet());
		}

		SQLiteDatabase db = myDBOpenHelper.getWritableDatabase();
		db.insertOrThrow(insert.getTable(), null, cv);
	}

    public int update(Update update) throws DatabaseException {
		ContentValues cv;
		if(update.getUpdateClass() != null) {
            ContentValueCreator cvCreator = myContentValueCreator.get(
                    update.getUpdateClass());
            if(cvCreator == null) {
                cvCreator = new ContentValueCreator();
                myContentValueCreator.put(update.getUpdateClass().getClass(),
                        cvCreator);
            }
			cv = cvCreator.getContentValues(update.getUpdateClass());
		} else {
			cv = new ContentValueCreator().getContentValues(update.getValues().entrySet());
		}

		int updateCount = myDBOpenHelper.getWritableDatabase().update(
                update.getTable(), cv, update.getWhereClause(), getArguments(update.getArguments()));
		DatabaseTable table = myDbSchema.getTable(update.getTable());
		if(table != null) {
			myAppContext.getContentResolver().notifyChange(table.getUri(), null, false);
		}
		return updateCount;
	}

	public int delete(Delete delete) throws DatabaseException {
		int deleteCount = myDBOpenHelper.getWritableDatabase().delete(
				delete.getTable(), delete.getWhereClause(),
				getArguments(delete.getArguments()));
		DatabaseTable table = myDbSchema.getTable(delete.getTable());
		if(table != null) {
			myAppContext.getContentResolver().notifyChange(table.getUri(), null, false);
		}

		return deleteCount;
	}

	public void executeRawStatement(String stmt) throws DatabaseException {
		myDBOpenHelper.getReadableDatabase().execSQL(stmt);
	}
	
	/* (non-Javadoc)
	 * @see IDBConnector#executeRawQuery(java.lang.String)
	 */
	@Override
	public RSData executeRawQuery(String stmt) throws DatabaseException {
		Cursor c = myDBOpenHelper.getReadableDatabase().rawQuery(stmt, null);
		return new QueryRSData(c);
	}
	
	/* (non-Javadoc)
	 * @see IDBConnector#doesTableExist(java.lang.String)
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
	 * @see IDBConnector#getDatabaseSchema()
	 */
	@Override
	public DbSchemaModel getDatabaseSchema() {
		return myDbSchema;
	}

	@Override
	public Uri getTableUri(String tableName) {
		try {
            if(DatabaseUtils.isSystemTable(tableName)){
                return null;
            }
			return myDbSchema.getTable(tableName).getUri();
		} catch (NullPointerException e) {
			NullPointerException exception = new NullPointerException(
					"The table " + tableName + " does not exist. Please check the correct name " +
							"in the database xml");
			exception.setStackTrace(e.getStackTrace());
			throw exception;
		}
	}

	@Override
	public Calendar getTableChangeDate(String tableName) {
		DatabaseTable table = myDbSchema.getTable(tableName);
		if(table != null) {
			return table.getLastTableUpdate();
		}
		return null;
	}

	@Override
	public void sendTableUpdateNotification(String tableName) {
		DatabaseTable table = myDbSchema.getTable(tableName);
        if(table != null) {
            table.notifyTableUpdate();
            myAppContext.getContentResolver().notifyChange(
                    table.getUri(), null, false);
        }
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
	 * @see IDBConnector#getTableObjectFactory()
	 */
	@Override
	public DatabaseViewFactory getTableObjectFactory() {
		return new SqliteDBViewFactory();
	}
	
	/* (non-Javadoc)
	 * @see IDBConnector#updateTable(DatabaseTable)
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
			} else if(column.getType() == newSchemaColumn.getType()){
				if(!column.isNullable() && newSchemaColumn.isNullable()) {
					return true;
				}
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
	 * This method converts a nameValuePair array into a string array.
	 * 
	 * @param args
	 * @return
	 */
	private String[] getArguments(List<String> args) {
		if (args == null) {
			return null;
		}

		String[] returnArgs = new String[args.size()];
		for (int i = 0; i < args.size(); i++) {
			returnArgs[i] = args.get(i);
		}

		return returnArgs;
	}
	
	/**
	 * This method
	 *
	 * @param query
	 * @return
	 */
	private String getSQLJoinQuery(Query query) throws DatabaseException {
		StringBuilder statementBuilder = new StringBuilder();
		
		statementBuilder.append(DBConstants.SELECT);
		StringBuilder colBuilder = new StringBuilder();
		if(query.getColumns() == null || query.getColumns().length == 0){
			colBuilder.append(getColumnClause(query));
//            colBuilder.append("*");
		} else {
			
			for(String column : query.getColumns()){
				if(colBuilder.length() > 0){
					colBuilder.append(",");
				}
				colBuilder.append(getColumnClause(column));
			}
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
	
	private String getJoinClause(String table, Query statement) {
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
				for(Join.JoinExpression expression : join.getJoinExpressions()){
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
     * This method returns the column clause in the format of "table.columnName AS table.columnName"
	 *
	 * This method will also add the joined table columns if the query contains any joins.
	 *
	 * NOTE: If the query contains it's own columns it is expected the query will have the columns
	 * in the correct format of table.column name for joins.
	 *
     * @param query
     * @return
     */
    public String getColumnClause(Query query) {
		StringBuilder columnClause = new StringBuilder();

		String[] columns = query.getColumns();
		if(columns != null && columns.length > 0) {
			return getColumnClause(query.getTable(), columns);
		}

		String mainTable = getColumnClause(query.getTable(), null);
		if(!query.getJoins().isEmpty()) {
			for(Join join : query.getJoins()){
				columnClause.append(", ");
				columnClause.append(getColumnClause(join.getTable(), null));
			}
		}
		return mainTable + " " + columnClause.toString();
    }

	public String getColumnClause(String delimitedTableColumnName) throws DatabaseException {
		String[] tableColumnPair = delimitedTableColumnName.split("\\.");
        int count = tableColumnPair.length;
		try {
			return delimitedTableColumnName + " AS " +
					DatabaseUtils.getDatabaseAsName(tableColumnPair[0], tableColumnPair[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new DatabaseException("Column names must be delimited with the table name in " +
					"a joined query");
		}
	}

    /**
     * This method returns the column clause in the format of "table.columnName AS table.columnName
	 *
	 * This method is mainly used for raw sql queries where a join is present so that the column can
	 * be referenced after the query has been executed.
     *
     * @param tableName
     * @return
     */
    public String getColumnClause(String tableName, String[] columns) {
        DatabaseTable table = myDbSchema.getTable(tableName);
        StringBuilder columnClause = new StringBuilder();
		if(columns != null && columns.length > 0) {
			for(String column : columns) {
				columnClause.append(column + " AS " + column);
			}
		}
        for(Entry<String, DatabaseColumn> entry : table.getColumns().entrySet()) {
            if(columnClause.length() > 0) {
                columnClause.append(", ");
            }

            String key = tableName + "." + entry.getKey();
            columnClause.append(key + " AS " +
					DatabaseUtils.getDatabaseAsName(tableName, entry.getKey()));
        }

        return columnClause.toString();
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
			return myCursor.getInt(colIndex);
		}

        public int getColumnCount() {
            return myCursor.getColumnCount();
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
		public float getFloatValue(String column) throws DatabaseException {
			int colIndex = getCursorIndex(column);
			return myCursor.getFloat(colIndex);
		}

		@Override
		public Date getDateValue(String column)
				throws DatabaseException {
			int colIndex = getCursorIndex(column);
			long timeInMillis = myCursor.getLong(colIndex);
			return new Date(timeInMillis);
		}
		
		/* (non-Javadoc)
		 * @see RSData#getCalendarValue(java.lang.String)
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
		 * @see RSData#containsNull(java.lang.String)
		 */
		@Override
		public boolean containsNull(String column) throws DatabaseException {
			int colIndex = getCursorIndex(column);
			return myCursor.isNull(colIndex);
		}
		
		/* (non-Javadoc)
		 * @see RSData#getBlobData(java.lang.String)
		 */
		@Override
		public byte[] getBlobData(String column) throws DatabaseException {
			try {
				int colIndex = getCursorIndex(column);
				return myCursor.getBlob(colIndex);
			} catch( IllegalStateException e) {
				throw new IllegalStateException("This could be caused by a limitation in the size " +
						"of data that can be stored in a sqlite database cell.", e);
			}
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
		 * @see RSData#hasColumn(java.lang.String)
		 */
		@Override
		public boolean hasColumn(String columnName) {
            return myCursor.getColumnIndex(columnName) >= 0;
		}
	}
}
