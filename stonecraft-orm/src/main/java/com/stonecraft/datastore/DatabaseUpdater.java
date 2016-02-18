/**
 * 
 */
package com.stonecraft.datastore;

import android.net.Uri;
import android.util.Log;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.Delete;
import com.stonecraft.datastore.interaction.Insert;
import com.stonecraft.datastore.interaction.Query;
import com.stonecraft.datastore.interaction.RawStatement;
import com.stonecraft.datastore.interaction.UpdateTableStatement;
import com.stonecraft.datastore.interfaces.IDBConnector;
import com.stonecraft.datastore.view.DatabaseColumn;
import com.stonecraft.datastore.view.DatabaseTable;
import com.stonecraft.datastore.view.DatabaseViewFactory;
import com.stonecraft.datastore.view.SQLiteColumn;
import com.stonecraft.datastore.view.SQLiteTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class checks if there has been any updates between the current version of the db
 * and the new version then attempts to update the db accordingly.
 *
 * @author mdelaney
 * @author Author: michael.delaney
 * @created Oct 22, 2013
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class DatabaseUpdater {
	
	private static final int COLUMN_LENGTH_TEXT = 64;
	private static final int COLUMN_LENGTH_INTEGER = 9;

	private IDBConnector myDBConnector;
	Map<String, DatabaseTable> myUpdatedTables;
	Map<String, DatabaseTable> myNewTables;
	
	public DatabaseUpdater(IDBConnector connector){
		myDBConnector = connector;
		myUpdatedTables = new HashMap<String, DatabaseTable>();
		myNewTables = new HashMap<String, DatabaseTable>();
	}
	
	/**
	 * This method checks if the table contains the datamap tables and creates it if it is not found
	 *
	 */
	public static void createDataMapTables(IDBConnector connector, DbSchemaModel schema)
		throws DatabaseException {
		try{
			SQLiteTable datamapTable = new SQLiteTable(DBConstants.TABLE_MAP, null);
			SQLiteTable schemaSettingsTable = new SQLiteTable(DBConstants.TABLE_SCHEMA_SETTINGS, null);
			
			datamapTable.addColumn(new SQLiteColumn(
				DBConstants.COLUMN_TABLE_NAME, DBConstants.DATATYPE_INT_STRING, COLUMN_LENGTH_TEXT, true));
			datamapTable.addColumn(new SQLiteColumn(
				DBConstants.COLUMN_COLUMN_NAME, DBConstants.DATATYPE_INT_STRING, COLUMN_LENGTH_TEXT, true));
			datamapTable.addColumn(new SQLiteColumn(
				DBConstants.COLUMN_DATA_TYPE, DBConstants.DATATYPE_INT_INTEGER, COLUMN_LENGTH_INTEGER, false));
			datamapTable.addColumn(new SQLiteColumn(
				DBConstants.COLUMN_DATA_LENGTH, DBConstants.DATATYPE_INT_INTEGER, COLUMN_LENGTH_INTEGER, false));
			datamapTable.addColumn(new SQLiteColumn(
				DBConstants.COLUMN_IS_PRIMARY_KEY, DBConstants.DATATYPE_INT_BOOLEAN, COLUMN_LENGTH_INTEGER, false));
			datamapTable.addColumn(new SQLiteColumn(
				DBConstants.COLUMN_IS_AUTOINCREMENTING, DBConstants.DATATYPE_INT_BOOLEAN, COLUMN_LENGTH_INTEGER, false));
			datamapTable.addColumn(new SQLiteColumn(
				DBConstants.COLUMN_IS_NULLABLE, DBConstants.DATATYPE_INT_BOOLEAN, COLUMN_LENGTH_INTEGER, false));
			datamapTable.addColumn(new SQLiteColumn(
					DBConstants.COLUMN_URI, DBConstants.DATATYPE_INT_STRING, COLUMN_LENGTH_TEXT, false, true));
			
			schemaSettingsTable.addColumn(new SQLiteColumn(
				DBConstants.COLUMN_TYPE, DBConstants.DATATYPE_INT_STRING, COLUMN_LENGTH_TEXT, false));
			schemaSettingsTable.addColumn(new SQLiteColumn(
				DBConstants.COLUMN_VALUE, DBConstants.DATATYPE_INT_STRING, COLUMN_LENGTH_TEXT, false));
			
			connector.startTransaction();
			connector.executeRawStatement(datamapTable.getCreateTableStmt());
			connector.executeRawStatement(schemaSettingsTable.getCreateTableStmt());
			connector.commit();
		}
		catch (DatabaseException e) {
			connector.rollBack();
			throw new DatabaseException("Failed to create table datamap table [" + e + "]");
		}
	}

	/**
	 * This method clear all data in the datamap table and re-populates 
	 * with the data from the passed in DbSchemaModel.
	 *
	 * @param schema
	 */
	public static void populateDatamapTables(DatastoreTransaction txn, DbSchemaModel schema)
		throws DatabaseException {
		Collection<DatabaseTable> tables = schema.getTables().values();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		txn.addStatement(new Delete(DBConstants.TABLE_MAP, null, null));
		for(DatabaseTable table : tables){
			for(DatabaseColumn column : table.getColumns().values()){
				DataMap datamapRecord = new DataMap();
				datamapRecord.setTableName(table.getName());
				datamapRecord.setColunnName(column.getName());
				datamapRecord.setType(column.getType());
				datamapRecord.setLength(column.getLength());
				datamapRecord.setIsPrimarykey(column.isPrimarykey());
				datamapRecord.setIsAutoIncrement(column.isAutoIncrement());
				datamapRecord.setIsNullable(column.isNullable());
				datamapRecord.setUri(table.getUri());

				Insert<DataMap> insert = new Insert<DataMap>(DBConstants.TABLE_MAP, datamapRecord);
				txn.addStatement(insert);
			}
		}

		StringBuilder whereClause = new StringBuilder();
		whereClause.append(DBConstants.COLUMN_TYPE).append(
				" = '" + DBConstants.SCHEMA_SETTINGS_TYPE_DB_NAME);
		whereClause.append("' OR " + DBConstants.COLUMN_TYPE).append(
				" = '" + DBConstants.SCHEMA_SETTINGS_TYPE_DB_VERSION + "'");
		txn.addStatement(new Delete(DBConstants.TABLE_SCHEMA_SETTINGS, whereClause.toString(), null));

		SettingsTable settingsTable = new SettingsTable();
		settingsTable.setType(DBConstants.SCHEMA_SETTINGS_TYPE_DB_NAME);
		settingsTable.setValue(schema.getName());
		Insert<SettingsTable> insert = new Insert<SettingsTable>(DBConstants.TABLE_SCHEMA_SETTINGS, settingsTable);
		txn.addStatement(insert);

		settingsTable = new SettingsTable();
		settingsTable.setType(DBConstants.SCHEMA_SETTINGS_TYPE_DB_VERSION);
		settingsTable.setValue(schema.getVersion());
		insert = new Insert<SettingsTable>(DBConstants.TABLE_SCHEMA_SETTINGS, settingsTable);
		txn.addStatement(insert);
	}
	
	public DbSchemaModel update(DbSchemaModel newSchema) throws DatabaseException {
		int tableCount = myDBConnector.doesTableExist(DBConstants.TABLE_MAP);
		if(tableCount == 0) {
			DatabaseUpdater.createDataMapTables(myDBConnector, newSchema);
		}
		
		DbSchemaModel currentSchema = getCurrentSchema(myDBConnector);
		
		compareSchemas(currentSchema, newSchema);


		DatastoreTransaction txn = new DatastoreTransaction();
		txn.setConnection(myDBConnector);
		if(!myNewTables.isEmpty()){
			for(DatabaseTable table : myNewTables.values()){
				txn.addStatement(new RawStatement(table.getName(), table.getCreateTableStmt()));
				Log.i(DatabaseUpdater.class.getSimpleName(), "Created new table " + table.getName());
			}
		}
		if(!myUpdatedTables.isEmpty()){
			for(DatabaseTable table : myUpdatedTables.values()){
				UpdateTableStatement updateTableStatement = new UpdateTableStatement(table.getName(),
						currentSchema.getTable(table.getName()), table);
				txn.addStatement(updateTableStatement);
				Log.i(DatabaseUpdater.class.getSimpleName(), "updated table " + table.getName());
			}
		}
		
		populateDatamapTables(txn, newSchema);
		try {
			txn.run();
		}
		catch (DatabaseException e) {
			throw new DatabaseException("Failed to update database", e);
		}
		return newSchema;
	}
	
	/**
	 * This method builds a DbSchemaModel based on the current db schema
	 *
	 * @return
	 */
	public static DbSchemaModel getCurrentSchema(IDBConnector connector) throws DatabaseException {
		DbSchemaModel schema = new DbSchemaModel();
		DatabaseViewFactory viewFactory = connector.getTableObjectFactory();
		Query query = new Query(DBConstants.TABLE_MAP);
		RSData data = connector.query(query);
		
		//build schema
		data.moveToFirst();
		while(!data.isAfterLast()){
			
			String tableName = data.getStringValue(DBConstants.COLUMN_TABLE_NAME);
			String uri = data.getStringValue(DBConstants.COLUMN_URI);

			DatabaseTable dbTable = schema.getTable(tableName);
			if (dbTable == null){
				dbTable = viewFactory.getNewTable(tableName, Uri.parse(uri));
				schema.addTable(dbTable);
			}
			
			String colName = data.getStringValue(DBConstants.COLUMN_COLUMN_NAME);
			int dataType = data.getIntValue(DBConstants.COLUMN_DATA_TYPE);
			int colLength = data.getIntValue(DBConstants.COLUMN_DATA_LENGTH);
			boolean isPrimaryKey = data.getBooleanValue(DBConstants.COLUMN_IS_PRIMARY_KEY);
			boolean isAutoIncrement = data.getBooleanValue(DBConstants.COLUMN_IS_AUTOINCREMENTING);
			boolean isNullable = data.getBooleanValue(DBConstants.COLUMN_IS_NULLABLE);
			dbTable.addColumn(viewFactory.getNewColumn(
				colName, dataType, colLength, isPrimaryKey, isNullable, isAutoIncrement));
			
			data.next();
		}
		data.close();
		
		//add schema settings to schema object
		StringBuilder whereClause = new StringBuilder();
		whereClause.append(DBConstants.COLUMN_TYPE).append(
			" = '" + DBConstants.SCHEMA_SETTINGS_TYPE_DB_NAME);
		whereClause.append("' OR " + DBConstants.COLUMN_TYPE).append(
			" = '" + DBConstants.SCHEMA_SETTINGS_TYPE_DB_VERSION + "'");
		query = new Query(DBConstants.TABLE_SCHEMA_SETTINGS).
			whereClause(whereClause.toString());
		data = connector.query(query);
		data.moveToFirst();
		while(!data.isAfterLast()){
			String settingsType = data.getStringValue(DBConstants.COLUMN_TYPE);
			
			if(settingsType.equals(DBConstants.SCHEMA_SETTINGS_TYPE_DB_NAME)){
				schema.setName(data.getStringValue(DBConstants.COLUMN_VALUE));
			}
			else if(settingsType.equals(DBConstants.SCHEMA_SETTINGS_TYPE_DB_VERSION)){
				schema.setVersion(data.getIntValue(DBConstants.COLUMN_VALUE));
			}
			data.next();
		}
		
		return schema;
	}

	/**
	 * This method compares the two passed in schema to find an alterations that
	 * have been made to the new schema.
	 *
	 * @param currentSchema
	 * @param newSchema
	 */
	private void compareSchemas(DbSchemaModel currentSchema,
		DbSchemaModel newSchema) throws DatabaseException {
		
		Map<String, DatabaseTable> obsoleteTables = new HashMap<String, DatabaseTable>();
		Map<String, DatabaseTable> newSchemaTables = newSchema.getTables(); 
		for(DatabaseTable table : currentSchema.getTables().values()){
			if(!DatabaseUtils.isSystemTable(table.getName())){
				if(newSchemaTables.containsKey(table.getName())){
					compareTableColumns(table, newSchemaTables.get(table.getName()));
				}
				else{
					//TODO this needs to drop the table as it's no longer required
					obsoleteTables.put(table.getName(), table);
				}
			}
		}
		
		//check for any new tables
		for(DatabaseTable newSchemaTable : newSchemaTables.values()){			
			if(!currentSchema.getTables().containsKey(newSchemaTable.getName())){
				//ensure the table hasn't been created programmatically
				if(myDBConnector.doesTableExist(newSchemaTable.getName()) == 0) {
					myNewTables.put(newSchemaTable.getName(), newSchemaTable);
				}
			}
		}
	}

	/**
	 * This method compares two tables to check if any columns have been altered
	 *
	 * @param table
	 * @param newSchemaTable
	 */
	private void compareTableColumns(DatabaseTable table,
		DatabaseTable newSchemaTable) throws DatabaseException {
		
		Map<String, DatabaseColumn> newSchemaColumns = newSchemaTable.getColumns();
		for(DatabaseColumn column : table.getColumns().values()){
			if(newSchemaColumns.containsKey(column.getName())){
				int isUpdateAllowed = myDBConnector.checkColumnUpdateRules(column,
					newSchemaColumns.get(column.getName()));
				switch(isUpdateAllowed){
					case IDBConnector.CHANGE_NONE : {
						break;
					}
					case IDBConnector.CHANGE_ALLOWED : {
						myUpdatedTables.put(newSchemaTable.getName(), newSchemaTable);
						break;
					}
					case IDBConnector.CHANGE_EXCEPTION : {
						throw new DatabaseException("The changes to column " + column.getName() +
							" in table " + table.getName() + " are not vaild");
					}
				}
			}
			else{
				//column not found. column should be deleted
				myUpdatedTables.put(newSchemaTable.getName(), newSchemaTable);
				return;
			}
		}
		
		//check for any new columns		
		for(DatabaseColumn newSchemaColumn : newSchemaColumns.values()){			
			if(!table.getColumns().containsKey(newSchemaColumn.getName())){
				myUpdatedTables.put(newSchemaTable.getName(), newSchemaTable);
				return;
			}
		}
	}
}
