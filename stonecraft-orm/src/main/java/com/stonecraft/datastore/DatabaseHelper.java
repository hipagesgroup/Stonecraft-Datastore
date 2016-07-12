package com.stonecraft.datastore;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.utils.StringUtils;
import com.stonecraft.datastore.view.DatabaseColumn;
import com.stonecraft.datastore.view.DatabaseTable;
import com.stonecraft.datastore.view.SQLiteColumn;
import com.stonecraft.datastore.view.SQLiteTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is the android database helper used to create and upgraded a
 * database. It also manages the use of the SQLiteDatabase object
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created Jun 27, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	private SQLiteDatabase myDBInUse;
	private final int myVersion;
	private DbSchemaModel myDBSchema;
	private AndroidDBConnection myConnection;
	private OnConnectionCreated myConnectionCreatedListener;

	protected DatabaseHelper(Context context,
		AndroidDBConnection connection, DbSchemaModel schema, final OnConnectionCreated listener) {
		super(context, schema.getName() + Datastore.DB_EXTENSION, null, schema.getVersion());
		myVersion = schema.getVersion();
		myDBSchema = schema;
		myConnection = connection;
		myConnectionCreatedListener = listener;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// stops the illegal state exception caused by a recursive call to
		// getWritableDatabase if the db is needed in postCreation()
		myDBInUse = db;
		createDatabase(db);
		myDBInUse = null;

		if(myConnectionCreatedListener != null) {
			myConnectionCreatedListener.OnConnectionCreated(
					Datastore.getDataStore(myDBSchema.getName()));
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try{
			myDBInUse = db;
			if(myConnection.doesTableExist(DBConstants.TABLE_MAP) == 0) {
				DatabaseUpdater.createDataMapTables(myConnection, myDBSchema);
				if(myConnection.doesTableExist(AndroidDBConnection.TABLE_SQLITE_MASTER) == 0){
					populateInitialDatamapTable(myDBInUse, oldVersion);
				}
			}
			DatabaseUpdater updater = new DatabaseUpdater(myConnection);
			myDBSchema = updater.update(myDBSchema);
		}
		catch(DatabaseException e) {
			throw new RuntimeException("Failed to update database", e);
		}
		myDBInUse = null;
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onOpen(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		try {
			myDBInUse = db;
			if(myConnection.doesTableExist(DBConstants.TABLE_MAP) == 0) {
				DatabaseUpdater.createDataMapTables(myConnection, myDBSchema);
				if(myConnection.doesTableExist(AndroidDBConnection.TABLE_SQLITE_MASTER) == 0){
					//version is not important at this point so can be one less than the new schema
					populateInitialDatamapTable(myDBInUse, myDBSchema.getVersion() - 1);
				}
			}
			myDBInUse = null;
		}
		catch(DatabaseException e){
			throw new RuntimeException("Could not create db schema from database");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#getWritableDatabase()
	 */
	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		if (myDBInUse == null) {
			myDBInUse = super.getWritableDatabase();
			return myDBInUse;
		}

		return myDBInUse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#getReadableDatabase()
	 */
	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		if (myDBInUse == null) {
			myDBInUse = super.getReadableDatabase();
			return myDBInUse;
		}

		return myDBInUse;
	}

	/**
	 * This method returns the current version of this database
	 *
	 * @return
	 */
	protected int getVersion() {
		return myVersion;
	}
	
	/**
	 * This method attempts to get a connection to a database that may have
	 * had the connection closed
	 *
	 */
	void reconnect() {
		if(!myDBInUse.isOpen()){
			myDBInUse = SQLiteDatabase.openDatabase(
				myDBInUse.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
		}
	}

	/**
	 * This method creates the database tables and columns based on the 
	 * passed in SQLiteDatabase object.
	 *
	 * @param db
	 */
	private void createDatabase(SQLiteDatabase db) {
		try {
			List<String> createStatements = myDBSchema.getTableCreateStmts();
			db.beginTransaction();
			for (String statement : createStatements) {
				db.execSQL(statement);
			}
			new DatabaseUpdater(myConnection).update(myDBSchema);
			db.setTransactionSuccessful();
			Log.i("Databasestore Helper",
					"The Database has been created successfully");
		} catch (Exception e) {
			Log.e("Databasestore Helper", "Could not create the database. ["
					+ e + "]");
			Log.e("Databasestore Helper", Log.getStackTraceString(e));
		} finally {
			db.endTransaction();
		}
	}
	
	/**
	 * This method
	 *
	 */
	private void populateInitialDatamapTable(SQLiteDatabase db, int oldVersion) {
		Cursor cursor = db.query(
			AndroidDBConnection.TABLE_SQLITE_MASTER,
			new String[]{AndroidDBConnection.COL_SQL}, "type = 'table'", null, null, null, null);
		cursor.moveToFirst();
		DbSchemaModel schema = new DbSchemaModel();
		while(!cursor.isAfterLast()){
			String createStatement = cursor.getString(0);
			DatabaseTable table = parseCreateStatement(createStatement);
			if(!table.getName().equals(AndroidDBConnection.TABLE_ANDROID_METADATA) &&
				!table.getName().equals(AndroidDBConnection.TABLE_SQLITE_SEQUENCE)){
				schema.addTable(table);
			}
			cursor.moveToNext();
		}
		
		if(!schema.getTables().isEmpty()){
			try{
				schema.setName(myDBSchema.getName());
				schema.setVersion(oldVersion);
				DatastoreTransaction txn = new DatastoreTransaction();
				txn.setConnection(myConnection);
				DatabaseUpdater.populateDatamapTables(txn, schema);
				txn.run();
			} catch (DatabaseException e) {
				throw new RuntimeException("Failed to populate the datamap table with the tables" +
					" parsed from the master table [" + e + "]");
			}
		}
	}

	/**
	 * This method
	 *
	 * @param createStatement
	 * @return
	 */
	private DatabaseTable parseCreateStatement(String createStatement) {
		
		//add 1 for the space between CREATE_TABLE and the table name
		int tableNameStartIndex = DatabaseTable.CREATE_TABLE.length() + 1;
		String tableName = createStatement.substring(tableNameStartIndex,
			createStatement.indexOf(DBConstants.OPEN_BRACKET, tableNameStartIndex)).trim();
		DatabaseTable table = new SQLiteTable(tableName, null);

		Map<String, TempColumn> colsMap = new HashMap<String, TempColumn>();
		int firstBraketIndex = createStatement.indexOf(DBConstants.OPEN_BRACKET) + 1;
		int lastBracketIndex = createStatement.lastIndexOf(DBConstants.CLOSE_BRACKET);
		List<String> columns = StringUtils.convertDelimitedStringToList(
				createStatement.substring(firstBraketIndex, lastBracketIndex),
				StringUtils.CHAR_COMMA, StringUtils.ESCAPE_CHAR);
		
		for(String columnString : columns){
			String[] columnAttribs = columnString.split(" ");
			if(columnAttribs[0].equals("PRIMARY")){
				int primaryKeyBraketIndex = createStatement.indexOf(
					DBConstants.OPEN_BRACKET,firstBraketIndex) + 1;
				int primaryKeyCloseBraketIndex = createStatement.indexOf(
					DBConstants.CLOSE_BRACKET, primaryKeyBraketIndex);
				String compositeKeys = createStatement.substring(
					primaryKeyBraketIndex, primaryKeyCloseBraketIndex);
				
				List<String> compositeKeyList = StringUtils.convertDelimitedStringToList(
					compositeKeys, StringUtils.CHAR_COMMA, StringUtils.ESCAPE_CHAR);
				for(String colName : compositeKeyList){
					if(colsMap.containsKey(colName)){
						TempColumn col = colsMap.get(colName);
						col.myIsNullable = false;
						col.myIsPrimarykey = true;
					}
				}
				break;
			} else {
				TempColumn col = new TempColumn();
				//nullable by default
				col.myIsNullable = true;
				col.myName = columnAttribs[0].trim();
				//start at 1 as the table name as already been taken out
				for(int i = 1; i < columnAttribs.length; i++){
					String attrib = columnAttribs[i].trim();
					if(attrib.equals(SQLiteColumn.DATATYPE_INTEGER)){
						col.myType = DBConstants.DATATYPE_INT_INTEGER;
					} else if (attrib.equals(SQLiteColumn.DATATYPE_REAL)) {
						col.myType = DBConstants.DATATYPE_INT_DOUBLE;
					} else if (attrib.equals(SQLiteColumn.DATATYPE_TEXT)) {
						col.myType = DBConstants.DATATYPE_INT_STRING;
					} else if (attrib.equals(SQLiteColumn.DATATYPE_BLOB)) {
						col.myType = DBConstants.DATATYPE_INT_BLOB;
					} else if (attrib.equals("PRIMARY")) {
						col.myIsNullable = false;
						col.myIsPrimarykey = true;
					} else if (attrib.equals(DatabaseColumn.AUTO_INCREMENT)) {
						col.myIsAutoIncrement = true;
					} else if (attrib.equals("NULL")) {
						col.myIsNullable = false;
					}
				}
				colsMap.put(col.myName, col);
			}
		}
		
		for(Map.Entry<String, TempColumn> entry : colsMap.entrySet()){
			TempColumn col = entry.getValue();
			//size can be left as 0 as it's not used in sqlite databases
			table.addColumn(new SQLiteColumn(col.myName, col.myType, 0, col.myIsPrimarykey,
				col.myIsNullable, col.myIsAutoIncrement));
			
		}
		
		return table;
	}
	
	private static class TempColumn {
		String myName;
		int myType;
		boolean myIsPrimarykey;
		boolean myIsAutoIncrement;
		boolean myIsNullable;
	}
}
