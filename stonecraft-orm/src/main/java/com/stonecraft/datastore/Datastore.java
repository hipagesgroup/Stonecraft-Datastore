package com.stonecraft.datastore;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.Insert;
import com.stonecraft.datastore.interaction.Query;
import com.stonecraft.datastore.interaction.Statement;
import com.stonecraft.datastore.interaction.Update;
import com.stonecraft.datastore.interfaces.IDBConnector;
import com.stonecraft.datastore.interfaces.ISchemaCreator;
import com.stonecraft.datastore.interfaces.OnNonQueryComplete;
import com.stonecraft.datastore.interfaces.OnQueryComplete;
import com.stonecraft.datastore.interfaces.OnTaskCompleteListener;
import com.stonecraft.datastore.interfaces.Tasker;
import com.stonecraft.datastore.parser.DatabaseParser;
import com.stonecraft.datastore.view.DatabaseTable;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * All Database interaction occurs through this class
 * 
 * It contains the functionality to insert/modify/query and delete records in
 * the database. It can support multiple db connections across mutiple database
 * types and connection types. This class is a multi-threaded class allowing db
 * interaction to be done without interfering with the main thread. Tasks that
 * need to be run in succession can be queued.
 * 
 * It has the ability to block the main thread as well if required returning the
 * data back to a listener or directly to the calling function.
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class Datastore implements OnTaskCompleteListener {
	public static final int DEFAULT_TOKEN = -1;
	public static final String DB_EXTENSION = ".sqlite";
	public static final int FAIL_TYPE_QUERY_FAILED = -1;

	private volatile static Map<String, IDBConnector> myDBConnections;
	private volatile static List<DatabaseTask> myQueuedTasks;
	private IDBConnector myActiveDatabase;
	private boolean myTasksQueued = true;
	private boolean myBlockingCall = false;
	private boolean myIsAttemptReconnect = true;

	/**
	 * This constructor is only used internally by createDatabase to ensure
	 * static fields have been intialised properly.
	 */
	private Datastore() {
		myActiveDatabase = null;
		if (myDBConnections == null) {
			myDBConnections = new HashMap<String, IDBConnector>();
		}
		if (myQueuedTasks == null) {
			myQueuedTasks = new ArrayList<DatabaseTask>();
		}
	}

	private Datastore(IDBConnector connection) {
		this();
		myActiveDatabase = connection;
	}

	/**
	 * This method returns an instance of Datastore to be used for any database
	 * interaction.
	 * 
	 * @param connection
	 * @return
	 */
	public static Datastore getDataStore(String connection) {
		if (myDBConnections != null && myDBConnections.containsKey(connection)) {
			return new Datastore(myDBConnections.get(connection));
		}

		return null;
	}

	/**
	 * @return the blockingCall
	 */
	public boolean isBlockingCall() {
		return myBlockingCall;
	}

	/**
	 * @param blockingCall
	 *            the blockingCall to set
	 */
	public Datastore setBlockingCall(boolean blockingCall) {
		myBlockingCall = blockingCall;
		return this;
	}

	/**
	 * @return the isTasksQueued
	 */
	public boolean isTasksQueued() {
		return myTasksQueued;
	}

	/**
	 * This method set whether the db tasks should be queued or run straight
	 * away. If more then one task is being done from within the same method it
	 * should be queued so that the tasks don't run out of order
	 * 
	 * @param queuedTasks the queuedTasks to set
	 */
	public Datastore setTasksQueued(boolean queuedTasks) {
		myTasksQueued = queuedTasks;
		return this;
	}

	/**
	 * @return the isAttemptReconnect
	 */
	public boolean isAttemptReconnect() {
		return myIsAttemptReconnect;
	}

	/**
	 * @param isAttemptReconnect the isAttemptReconnect to set
	 */
	public void setAttemptReconnect(boolean isAttemptReconnect) {
		myIsAttemptReconnect = isAttemptReconnect;
	}

    /**
     * This method creates a database based on the given name and version found in the database
     * xml. It will check if the database has been created previously.
     *
     * This method should be called in the apps Application class to ensure there is always a
     * valid connection throughout the application.
     *
     * Once this method is called use getDatastore to get an instance of this
     * class.
     *
     * if a connection is passed in and a connection already exists with the
     * same name it will be closed and replaced with this one.
     *
     * @param context
     * @param databaseXml
     * @param listener
     * @throws DatabaseException
     */
	public static void createConnection(final Context context, final InputStream databaseXml,
			@Nullable final OnConnectionCreated listener)
			throws DatabaseException {
		new DatabaseParser(new DatabaseParser.OnSchemaModelCreated() {
			@Override
			public void OnSchemaModelCreated(DbSchemaModel schema) {
				IDBConnector connector = new AndroidDBConnection(context, schema, listener);
				setConnection(connector);
			}
		}).execute(databaseXml);
	}

	private static void setConnection(IDBConnector connection) {
		// ensure static fields are initialised;
		new Datastore();
		synchronized (Datastore.class) {
			//User is opening another connection under a name that already has a connection.
			//close this connection before open a new one under the same name
			if(myDBConnections.containsKey(connection.getName())){
				myDBConnections.get(connection.getName()).close();
				myDBConnections.remove(connection.getName());
			}

			if (!myDBConnections.containsKey(connection.getName())) {
				// This double checking ensures the same connection can't
				// be created twice across threads
				synchronized (Datastore.class) {
					if (!myDBConnections.containsKey(connection.getName())) {
						myDBConnections.put(connection.getName(), connection);
					}
				}
			}
		}
	}

	/**
	 * This method creates a schema on the database that currently active for an
	 * instance of this class.
	 * 
	 * If a schema already exists it will be deleted and recreated with the
	 * passed in schema.
	 * 
	 * @param schema
	 * @throws DatabaseException
	 */
	public void createSchema(ISchemaCreator schema) throws DatabaseException {
		myActiveDatabase.createSchema(schema);
	}

	/**
	 * This method closes all databases that are currently open. It is
	 * recommended this method is called before the application is closed to
	 * clean up and open connections
	 */
	public static void closeAll() throws DatabaseException {
		for (Map.Entry<String, IDBConnector> entry : myDBConnections.entrySet()) {
			IDBConnector conn = entry.getValue();
			if (conn.isOpen()) {
				conn.close();
			}
		}
	}

	/**
	 * This method closes the database of the current datastore instance if it
	 * is currently open.
	 */
	public void close() throws DatabaseException {
		if (myActiveDatabase != null && myActiveDatabase.isOpen()) {
			myActiveDatabase.close();
		}
	}

	/**
	 * This method executes a query on the database and returns the resultant
	 * RSData object. If an error occurs a DatabaseException is passed back in
	 * the listener. The listener can be null, but if an exception occurs this
	 * statement will fail silently.
	 * 
	 * @param token
	 * @param stmt
	 * @param listener
	 */
	public void executeQuery(int token, Query stmt, OnQueryComplete listener) {

        Method[] methods = listener.getClass().getMethods();
		Class injectorClass = Object.class;
        for(Method method : methods) {
            if(method.getName().equals("onQueryComplete")) {
                Class[] clazzes = method.getParameterTypes();
                for(Class clazz : clazzes) {
					if(clazz.getComponentType() != null) {
						Class testClass = clazz.getComponentType();
						if(!clazz.getComponentType().getName().equals(Object.class.getName())) {
							Log.d("TEST", clazz.getComponentType().getName());
							injectorClass = clazz.getComponentType();
						}
					}
                }
            }
        }
		try{
			if(!validateDBConnection()){
				throw new DatabaseException(
					"Attempt to reopen an already closed database object. "
					+ "Ensure a connection to the database is currently valid and open");
			}
		}
		catch (DatabaseException e) {
			listener.onQueryFailed(token, e);
		}
		
		int taskId = new AtomicInteger().incrementAndGet();
		DatabaseQueryTask task = new DatabaseQueryTask(taskId, token,
				myActiveDatabase, stmt);
		task.setOnQueryCompleteListener(listener);
		task.setInjectorClass(injectorClass);

		try {
			executeStmt(task);
		} catch (DatabaseException e) {
			listener.onQueryFailed(token, e);
		}
	}

    /**
     * This method executes a query on the database and returns the resultant
     * RSData object. This method will always block the calling thread
     * regardless if this object has been set to block or not.
     *
     * @param stmt
     * @return
     * @throws DatabaseException
     */
	public <T> T[] executeQuery(Query stmt, Class<T> classToInject) throws DatabaseException {
		if(!validateDBConnection()){
			throw new DatabaseException(
				"Attempt to reopen an already closed database object. "
				+ "Ensure a connection to the database is currently valid and open");
		}
		
		int taskId = new AtomicInteger().incrementAndGet();
		DatabaseQueryTask task = new DatabaseQueryTask(taskId, DEFAULT_TOKEN,
			myActiveDatabase, stmt);
		
		return task.startTask(classToInject);
	}

	/**
	 * This method executes a non query (eg delete, update, insert) on the
	 * database and returns the resultant RSData object. If an error occurs a
	 * DatabaseException is passed back in the listener. The listener can be
	 * null, but if an exception occurs this statement will fail silently.
	 * 
	 * @param token
	 * @param stmt
	 * @param listener
	 */
	public void executeNonQuery(int token, Statement stmt,
			final OnNonQueryComplete listener) {
		try{
			if(!validateDBConnection()){
				throw new DatabaseException(
					"Attempt to reopen an already closed database object. "
					+ "Ensure a connection to the database is currently valid and open");
			}
		}
		catch (DatabaseException e) {
			listener.onNonQueryFailed(token, e);
		}
		
		int taskId = new AtomicInteger().incrementAndGet();
		DatastoreTransaction dt = new DatastoreTransaction();
		dt.addStatement(stmt);
		DatabaseNonQueryTask task = new DatabaseNonQueryTask(taskId, token,
				myActiveDatabase, dt);
		task.addOnStmtCompleteListener(listener);

		try {
			executeStmt(task);
		} catch (DatabaseException e) {
			listener.onNonQueryComplete(token, DBConstants.NO_RECORDS_UPDATED);
		}
	}

	/**
	 * This method executes a non query on the database and returns the
	 * resultant RSData object. This method will always block the calling thread
	 * regardless if this object has been set to block or not.
	 * 
	 * @param stmt
	 * @return
	 * @throws DatabaseException
	 */
	public int executeNonQuery(Statement stmt) throws DatabaseException {
		if(!validateDBConnection()){
			throw new DatabaseException(
				"Attempt to reopen an already closed database object. "
				+ "Ensure a connection to the database is currently valid and open");
		}
		
		int taskId = new AtomicInteger().incrementAndGet();
		DatastoreTransaction dt = new DatastoreTransaction();
		dt.addStatement(stmt);
		DatabaseNonQueryTask task = new DatabaseNonQueryTask(taskId,
				DEFAULT_TOKEN, myActiveDatabase, dt);
		task.startTask();
		return task.getTaskResult();
	}
	
	/**
	 * This method will check if records exist with the given whereclause. If they
	 * exist an update of this records will be executed based on the values in the
	 * given insert. If no records exist the insert will be executed.
	 * 
	 * NOTE: If multiple rows are given in the passed in insert only the first row
	 * will be used when executing an update
	 *
	 * @param token
	 * @param whereClause
	 * @param insert
	 * @param listener
	 */
	public void executeAddOrUpdate(int token, final String whereClause, final Insert insert,
		final OnNonQueryComplete listener) {
		Query query = new Query(insert.getTable());
		query.whereClause(whereClause);
		executeQuery(token, query, new OnQueryComplete<RSData>() {

			@Override
			public void onQueryComplete(int token, RSData[] resultSets) {
				RSData resultSet = resultSets[0];
				resultSet.moveToFirst();

				//do an insert if no records are found or an update otherwise.
				Statement updateOrAddStmt = null;
				if (resultSet.getCount() <= 0) {
					updateOrAddStmt = insert;
				} else {
					Map<String, Object> values = insert.getValues();

					if (!values.isEmpty()) {
						Update update = new Update(insert.getTable(),
								values, whereClause, null);
						updateOrAddStmt = update;

					}
				}

				resultSet.close();

				if (updateOrAddStmt != null) {
					executeNonQuery(token, updateOrAddStmt, listener);
				} else {
					listener.onNonQueryFailed(token, new DatabaseException(
							"No values were in the insert object when a "
									+ "executeAddOrUpdate() was attempted"));
				}
			}

			@Override
			public void onQueryFailed(int token, DatabaseException e) {
				listener.onNonQueryFailed(token, e);
			}
		});
	}
	
	/**
	 * This method checks if the current database connection is available. If the 
	 * connection is not available no interaction can be made until the connection
	 * is re-established.
	 *
	 * @return
	 */
	public boolean isConnectionAvail(){
		try{
			return myActiveDatabase.isOpen();
		}
		catch(DatabaseException e){
			Log.e(Datastore.class.getSimpleName(), "Failed to check if database is open ["
				+ e + "] false will be returned from isConnectionAvail()");
			return false;
		}
	}
	
	/**
	 * This method checks whether a table exists or not. It will return a boolean to the passed
	 * in listener
	 *
	 * @param token
	 * @param tableName
	 * @param listener
	 */
	public void doesTableExist(int token, String tableName, final OnNonQueryComplete listener){
		try{
			if(!validateDBConnection()){
				throw new DatabaseException(
					"Attempt to reopen an already closed database object. "
					+ "Ensure a connection to the database is currently valid and open");
			}
		}
		catch (DatabaseException e) {
			listener.onNonQueryFailed(token, e);
			return;
		}
		
		int taskId = new AtomicInteger().incrementAndGet();
		DatastoreTransaction dt = new DatastoreTransaction();
		dt.addStatement(new Statement(tableName));
		DatabaseNonQueryTask task = new DatabaseNonQueryTask(taskId, token,
				myActiveDatabase, dt);
		task.addOnStmtCompleteListener(listener);

		try {
			executeStmt(task);
		} catch (DatabaseException e) {
			listener.onNonQueryFailed(token, e);
		}
	}
	
	/**
	 * This method creates a new table in the database. It will return the number of tables
	 * created in the listener (eg 1 will mean the table was created successfully. 0 means
	 * it failed to be created.)
	 *
	 * @param token
	 * @param table
	 * @param listener
	 * @throws DatabaseException
	 */
	public void createTable(int token, DatabaseTable table, OnNonQueryComplete listener) {
		try{
			if(!validateDBConnection()){
				throw new DatabaseException(
					"Attempt to reopen an already closed database object. "
					+ "Ensure a connection to the database is currently valid and open");
			}
		}
		catch (DatabaseException e) {
			listener.onNonQueryFailed(token, e);
		}
		
		int taskId = new AtomicInteger().incrementAndGet();
		CreateTableTask task = new CreateTableTask(taskId, token,
				myActiveDatabase, table);
		task.addOnStmtCompleteListener(listener);

		try {
			executeStmt(task);
		} catch (DatabaseException e) {
			listener.onNonQueryFailed(token, e);
		}
	}

	public Uri getTableUri(String tableName) {
		return myActiveDatabase.getTableUri(tableName);
	}

	IDBConnector getActiveDatabase() {
		return myActiveDatabase;
	}
	
	private boolean validateDBConnection() throws DatabaseException {
		if(isConnectionAvail()){
			return true;
		}
		
		if(myIsAttemptReconnect){
			try {
				myActiveDatabase.createConnection();
			}
			catch(DatabaseException e) {
				Log.e(Datastore.class.getSimpleName(), "Failed to recreate a connection "
					+ "to the database [" + e + "]");
				return false;
			}
			
			return true;
		}
		
		return false;
	}

	private void executeStmt(DatabaseTask task) throws DatabaseException {
				
		if (isBlockingCall()) {
			task.startTask();
		} else if (isTasksQueued()) {
			task.addOnTaskCompleteListener(this);
			addTaskToQueue(task);
		} else {
			task.execute();
		}
	}

	/**
	 * This method adds a task to the queue. If this task is the first task to
	 * be added it will be start immediately
	 * 
	 * @param dt
	 * @throws DatabaseException
	 */
	private synchronized void addTaskToQueue(DatabaseTask dt)
			throws DatabaseException {
		synchronized (myQueuedTasks) {
			myQueuedTasks.add(dt);

			if (myQueuedTasks.size() == 1) {
				dt.execute();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * au.com.delaney.listeners.OnTaskCompleteListener#onTaskComplete(au.com
	 * .delaney.listeners.Tasker)
	 */
	@Override
	public void onTaskComplete(Tasker task) {
		synchronized (myQueuedTasks) {
			myQueuedTasks.remove(task);

			if (myQueuedTasks.size() > 0) {
				myQueuedTasks.get(0).execute();
			}
		}
	}
}
