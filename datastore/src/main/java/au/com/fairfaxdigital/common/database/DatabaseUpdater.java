/**
 * 
 */
package au.com.fairfaxdigital.common.database;

import static au.com.fairfaxdigital.common.database.DBConstants.COLUMN_COLUMN_NAME;
import static au.com.fairfaxdigital.common.database.DBConstants.COLUMN_DATA_LENGTH;
import static au.com.fairfaxdigital.common.database.DBConstants.COLUMN_DATA_TYPE;
import static au.com.fairfaxdigital.common.database.DBConstants.COLUMN_IS_AUTOINCREMENTING;
import static au.com.fairfaxdigital.common.database.DBConstants.COLUMN_IS_NULLABLE;
import static au.com.fairfaxdigital.common.database.DBConstants.COLUMN_IS_PRIMARY_KEY;
import static au.com.fairfaxdigital.common.database.DBConstants.COLUMN_TABLE_NAME;
import static au.com.fairfaxdigital.common.database.DBConstants.COLUMN_TYPE;
import static au.com.fairfaxdigital.common.database.DBConstants.COLUMN_VALUE;
import static au.com.fairfaxdigital.common.database.DBConstants.DATATYPE_INT_BOOLEAN;
import static au.com.fairfaxdigital.common.database.DBConstants.DATATYPE_INT_INTEGER;
import static au.com.fairfaxdigital.common.database.DBConstants.DATATYPE_INT_STRING;
import static au.com.fairfaxdigital.common.database.DBConstants.SCHEMA_SETTINGS_TYPE_DB_NAME;
import static au.com.fairfaxdigital.common.database.DBConstants.SCHEMA_SETTINGS_TYPE_DB_VERSION;
import static au.com.fairfaxdigital.common.database.DBConstants.TABLE_MAP;
import static au.com.fairfaxdigital.common.database.DBConstants.TABLE_SCHEMA_SETTINGS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import au.com.fairfaxdigital.common.database.exceptions.DatabaseException;
import au.com.fairfaxdigital.common.database.interaction.Delete;
import au.com.fairfaxdigital.common.database.interaction.Insert;
import au.com.fairfaxdigital.common.database.interaction.Query;
import au.com.fairfaxdigital.common.database.interfaces.IDBConnector;
import au.com.fairfaxdigital.common.database.view.DatabaseColumn;
import au.com.fairfaxdigital.common.database.view.DatabaseTable;
import au.com.fairfaxdigital.common.database.view.DatabaseViewFactory;
import au.com.fairfaxdigital.common.database.view.SQLiteColumn;
import au.com.fairfaxdigital.common.database.view.SQLiteTable;

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
	public static void createDataMapTables(IDBConnector connector, DatabaseSchema schema) 
		throws DatabaseException {
		try{
			SQLiteTable datamapTable = new SQLiteTable(TABLE_MAP);
			SQLiteTable schemaSettingsTable = new SQLiteTable(TABLE_SCHEMA_SETTINGS);
			
			datamapTable.addColumn(new SQLiteColumn(
				COLUMN_TABLE_NAME, DATATYPE_INT_STRING, COLUMN_LENGTH_TEXT, true));
			datamapTable.addColumn(new SQLiteColumn(
				COLUMN_COLUMN_NAME, DATATYPE_INT_STRING, COLUMN_LENGTH_TEXT, true));
			datamapTable.addColumn(new SQLiteColumn(
				COLUMN_DATA_TYPE, DATATYPE_INT_INTEGER, COLUMN_LENGTH_INTEGER, false));
			datamapTable.addColumn(new SQLiteColumn(
				COLUMN_DATA_LENGTH, DATATYPE_INT_INTEGER, COLUMN_LENGTH_INTEGER, false));
			datamapTable.addColumn(new SQLiteColumn(
				COLUMN_IS_PRIMARY_KEY, DATATYPE_INT_BOOLEAN, COLUMN_LENGTH_INTEGER, false));
			datamapTable.addColumn(new SQLiteColumn(
				COLUMN_IS_AUTOINCREMENTING, DATATYPE_INT_BOOLEAN, COLUMN_LENGTH_INTEGER, false));
			datamapTable.addColumn(new SQLiteColumn(
				COLUMN_IS_NULLABLE, DATATYPE_INT_BOOLEAN, COLUMN_LENGTH_INTEGER, false));
			
			schemaSettingsTable.addColumn(new SQLiteColumn(
				COLUMN_TYPE, DATATYPE_INT_STRING, COLUMN_LENGTH_TEXT, false));
			schemaSettingsTable.addColumn(new SQLiteColumn(
				COLUMN_VALUE, DATATYPE_INT_STRING, COLUMN_LENGTH_TEXT, false));
			
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
	 * with the data from the passed in DatabaseSchema.
	 *
	 * @param schema
	 */
	public static void populateDatamapTables(IDBConnector connector, DatabaseSchema schema) 
		throws DatabaseException {
		Collection<DatabaseTable> tables = schema.getTables().values();
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
		for(DatabaseTable table : tables){
			for(DatabaseColumn column : table.getColumns().values()){
				Map<String, Object> row = new HashMap<String, Object>();
				row.put(COLUMN_TABLE_NAME, table.getName());
				row.put(COLUMN_COLUMN_NAME, column.getName());
				row.put(COLUMN_DATA_TYPE, column.getType());
				row.put(COLUMN_DATA_LENGTH, column.getLength());
				row.put(COLUMN_IS_PRIMARY_KEY, column.isPrimarykey());
				row.put(COLUMN_IS_AUTOINCREMENTING, column.isAutoIncrement());
				row.put(COLUMN_IS_NULLABLE, column.isNullable());
				rows.add(row);
			}
		}
		
		try {
			connector.startTransaction();
			connector.delete(new Delete(TABLE_MAP, null, null));
			
			Insert insert = new Insert(TABLE_MAP, rows);
			connector.insert(insert);
			
			rows = new ArrayList<Map<String, Object>>();
			Map<String, Object> row = new HashMap<String, Object>();
			row.put(COLUMN_TYPE, SCHEMA_SETTINGS_TYPE_DB_NAME);
			row.put(COLUMN_VALUE, schema.getName());
			rows.add(row);
			
			StringBuilder whereClause = new StringBuilder();
			whereClause.append(COLUMN_TYPE).append(
				" = '" + SCHEMA_SETTINGS_TYPE_DB_NAME);
			whereClause.append("' OR " + COLUMN_TYPE).append(
				" = '" + SCHEMA_SETTINGS_TYPE_DB_VERSION + "'");
			connector.delete(new Delete(TABLE_SCHEMA_SETTINGS, whereClause.toString(), null));
			row = new HashMap<String, Object>();
			row.put(COLUMN_TYPE, SCHEMA_SETTINGS_TYPE_DB_VERSION);
			row.put(COLUMN_VALUE, schema.getVersion());
			rows.add(row);
			
			//add schema settings to schema object
			insert = new Insert(TABLE_SCHEMA_SETTINGS, rows);
			connector.insert(insert);
			connector.commit();
		}
		catch (DatabaseException e) {
			connector.rollBack();
			throw new DatabaseException("Failed to populate table datamap table [" + e + "]");
		}
	}
	
	public DatabaseSchema update(DatabaseSchema newSchema) throws DatabaseException {
		int tableCount = myDBConnector.doesTableExist(TABLE_MAP);
		if(tableCount == 0) {
			DatabaseUpdater.createDataMapTables(myDBConnector, newSchema);
		}
		
		DatabaseSchema currentSchema = getCurrentSchema(myDBConnector);
		
		compareSchemas(currentSchema, newSchema);
		
		myDBConnector.startTransaction();
		if(!myNewTables.isEmpty()){
			for(DatabaseTable table : myNewTables.values()){
				myDBConnector.executeRawStatement(table.getCreateTableStmt());
				Log.i(DatabaseUpdater.class.getSimpleName(), "Created new table " + table.getName());
			}
		}
		if(!myUpdatedTables.isEmpty()){
			for(DatabaseTable table : myUpdatedTables.values()){
				myDBConnector.updateTable(currentSchema.getTable(table.getName()), table);
				Log.i(DatabaseUpdater.class.getSimpleName(), "updated table " + table.getName());
			}
		}
		
		populateDatamapTables(myDBConnector, newSchema);
		myDBConnector.commit();
		return newSchema;
	}
	
	/**
	 * This method builds a DatabaseSchema based on the current db schema
	 *
	 * @return
	 */
	public static DatabaseSchema getCurrentSchema(IDBConnector connector) throws DatabaseException {
		DatabaseSchema schema = new DatabaseSchema();
		DatabaseViewFactory viewFactory = connector.getTableObjectFactory();
		Query query = new Query(TABLE_MAP, null);
		RSData data = connector.query(query);
		
		//build schema
		data.moveToFirst();
		while(!data.isAfterLast()){
			
			String tableName = data.getStringValue(COLUMN_TABLE_NAME);
			
			DatabaseTable dbTable = schema.getTable(tableName);
			if(dbTable == null){
				dbTable = viewFactory.getNewTable(tableName);
				schema.addTable(dbTable);
			}
			
			String colName = data.getStringValue(COLUMN_COLUMN_NAME);
			int dataType = data.getIntValue(COLUMN_DATA_TYPE);
			int colLength = data.getIntValue(COLUMN_DATA_LENGTH);
			boolean isPrimaryKey = data.getBooleanValue(COLUMN_IS_PRIMARY_KEY);
			boolean isAutoIncrement = data.getBooleanValue(COLUMN_IS_AUTOINCREMENTING);
			boolean isNullable = data.getBooleanValue(COLUMN_IS_NULLABLE);
			dbTable.addColumn(viewFactory.getNewColumn(
				colName, dataType, colLength, isPrimaryKey, isNullable, isAutoIncrement));
			
			data.next();
		}
		data.close();
		
		//add schema settings to schema object
		StringBuilder whereClause = new StringBuilder();
		whereClause.append(COLUMN_TYPE).append(
			" = '" + SCHEMA_SETTINGS_TYPE_DB_NAME);
		whereClause.append("' OR " + COLUMN_TYPE).append(
			" = '" + SCHEMA_SETTINGS_TYPE_DB_VERSION + "'");
		query = new Query(TABLE_SCHEMA_SETTINGS, whereClause.toString());
		data = connector.query(query);
		data.moveToFirst();
		while(!data.isAfterLast()){
			String settingsType = data.getStringValue(COLUMN_TYPE);
			
			if(settingsType.equals(SCHEMA_SETTINGS_TYPE_DB_NAME)){
				schema.setName(data.getStringValue(COLUMN_VALUE));
			}
			else if(settingsType.equals(SCHEMA_SETTINGS_TYPE_DB_VERSION)){
				schema.setVersion(data.getIntValue(COLUMN_VALUE));
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
	private void compareSchemas(DatabaseSchema currentSchema,
		DatabaseSchema newSchema) throws DatabaseException {
		
		Map<String, DatabaseTable> obsoleteTables = new HashMap<String, DatabaseTable>();
		Map<String, DatabaseTable> newSchemaTables = newSchema.getTables(); 
		for(DatabaseTable table : currentSchema.getTables().values()){
			if(!isSystemTable(table)){
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
	 * This method checks if the passed in table is a system table
	 *
	 * @param table
	 * @return
	 */
	private boolean isSystemTable(DatabaseTable table) {
		return table.getName().equals(TABLE_MAP) ||
			table.getName().equals(TABLE_SCHEMA_SETTINGS);
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
