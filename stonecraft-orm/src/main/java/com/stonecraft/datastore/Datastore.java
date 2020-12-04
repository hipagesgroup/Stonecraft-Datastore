package com.stonecraft.datastore;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.stonecraft.datastore.exceptions.DatabaseCreationFailedException;
import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.IRawStatement;
import com.stonecraft.datastore.interaction.Insert;
import com.stonecraft.datastore.interaction.Query;
import com.stonecraft.datastore.interaction.Statement;
import com.stonecraft.datastore.interaction.Update;
import com.stonecraft.datastore.interfaces.IDBConnector;
import com.stonecraft.datastore.interfaces.ISchemaCreator;
import com.stonecraft.datastore.interfaces.OnNonQueryComplete;
import com.stonecraft.datastore.interfaces.OnTaskCompleteListener;
import com.stonecraft.datastore.interfaces.Tasker;
import com.stonecraft.datastore.logs.ConnectionLog;
import com.stonecraft.datastore.logs.ConnectionStatus;
import com.stonecraft.datastore.parser.DatabaseParser;
import com.stonecraft.datastore.view.DatabaseTable;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * All Database interaction occurs through this class
 * <p>
 * It contains the functionality to insert/modify/query and delete records in
 * the database. It can support multiple db connections across mutiple database
 * types and connection types. This class is a multi-threaded class allowing db
 * interaction to be done without interfering with the main thread. Tasks that
 * need to be run in succession can be queued.
 * <p>
 * It has the ability to block the main thread as well if required returning the
 * data back to a listener or directly to the calling function.
 *
 * @author mdelaney
 * @author Author: michael.delaney
 * @version Revision: 1.0
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 */
public class Datastore implements OnTaskCompleteListener {
    public static final int DEFAULT_TOKEN = -1;
    public static final String DB_EXTENSION = ".sqlite";
    public static final int FAIL_TYPE_QUERY_FAILED = -1;

    private volatile static Map<String, IDBConnector> myDBConnections;
    private volatile static List<DatabaseTask> myQueuedTasks;
    private volatile static Map<String, Map<Class, QueryDeserializer>> myDeserializers =
            new HashMap<String, Map<Class, QueryDeserializer>>();
    private volatile static List<ConnectionLog> myLogs = new ArrayList();
    private IDBConnector myActiveDatabase;
    private boolean myTasksQueued = true;
    private boolean myBlockingCall = false;
    private boolean myIsAttemptReconnect = true;
    //This latch ensures that an instance of the datastore can't be returned while the parsing
    //of a db schema is in progress.
    private static volatile CountDownLatch myParsingLatch;
    private final static AtomicInteger myParsingCount = new AtomicInteger(0);

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
        try {
            myParsingLatch.await();

            if (myDBConnections != null && myDBConnections.containsKey(connection)) {
                return new Datastore(myDBConnections.get(connection));
            }

        } catch (InterruptedException e) {
            throw new DatabaseCreationFailedException("The current thread has been interrupted. " +
                    "A datastore object will not be returned", e);
        }

        return null;
    }

    public static boolean hasDatastore(String connection) {
        return myDBConnections.containsKey(connection);
    }

    /**
     * This method returns the name of the database that is currently being used in this instance.
     *
     * @return
     */
    public String getDatabaseName() {
        return myActiveDatabase.getName();
    }

    public Calendar getLastTableUpdateTime(String tableName) {
        return myActiveDatabase.getTableChangeDate(tableName);
    }

    /**
     * @return the blockingCall
     */
    public boolean isBlockingCall() {
        return myBlockingCall;
    }

    /**
     * @param blockingCall the blockingCall to set
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

    public static List<ConnectionLog> getConnectionLogs() {
        return new ArrayList(myLogs);
    }

    /**
     * This method creates a database based on the given name and version found in the database
     * xml. It will check if the database has been created previously.
     * <p>
     * This method should be called in the apps Application class to ensure there is always a
     * valid connection throughout the application.
     * <p>
     * Once this method is called use getDatastore to get an instance of this
     * class.
     * <p>
     * if a connection is passed in and a connection already exists with the
     * same name it will be closed and replaced with this one.
     *
     * @param context
     * @param databaseXml
     * @param listener
     * @throws DatabaseException
     */
    public synchronized static void createConnection(final Context context, final InputStream databaseXml,
                                                     final OnConnectionListener listener)
            throws DatabaseException {
        myParsingCount.incrementAndGet();
        if (myParsingLatch == null || myParsingLatch.getCount() == 0) {
            myParsingLatch = new CountDownLatch(1);
        }

        DatabaseParser parser = new DatabaseParser(new DatabaseParser.OnSchemaModelCreated() {
            @Override
            public void OnSchemaModelCreated(DbSchemaModel schema) {
                OnConnectionListener connectionListener = getConnectionInterceptor(schema.getName(),
                        listener);
                IDBConnector connector = new AndroidDBConnection(context, schema, connectionListener);
                setConnection(connector, listener);

                if (myParsingCount.decrementAndGet() == 0) {
                    myParsingLatch.countDown();
                }
            }
        });
        parser.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, databaseXml);
        Log.d("createConnection", "Creating connection");
    }

    private static OnConnectionListener getConnectionInterceptor(final String dbName,
                                                                 final OnConnectionListener listener) {
        OnConnectionListener newListener = new OnConnectionListener() {
            @Override
            public void onConnectionCreated(Datastore datastore) {
                myLogs.add(new ConnectionLog(dbName,
                        ConnectionStatus.CREATED, System.currentTimeMillis()));
                if (listener != null) {
                    listener.onConnectionCreated(datastore);
                }
            }

            @Override
            public void onUpgrade(Datastore datastore) {
                myLogs.add(new ConnectionLog(dbName,
                        ConnectionStatus.UPGRADED, System.currentTimeMillis()));
                if (listener != null) {
                    listener.onUpgrade(datastore);
                }
            }

            @Override
            public void onOpen(Datastore datastore) {
                myLogs.add(new ConnectionLog(dbName,
                        ConnectionStatus.OPENED, System.currentTimeMillis()));
                if (listener != null) {
                    listener.onOpen(datastore);
                }
            }

            @Override
            public void onClose() {
                myLogs.add(new ConnectionLog(dbName,
                        ConnectionStatus.CLOSED, System.currentTimeMillis()));
                if (listener != null) {
                    listener.onClose();
                }
            }

            @Override
            public void closingAndCreatingNewConnection(String dbName) {
                if (listener != null) {
                    listener.closingAndCreatingNewConnection(dbName);
                }
            }
        };

        return newListener;
    }

    private static void setConnection(IDBConnector connection, OnConnectionListener listener) {

        // ensure static fields are initialised;
        new Datastore();
        synchronized (Datastore.class) {
            //User is opening another connection under a name that already has a connection.
            //close this connection before open a new one under the same name
            if (myDBConnections.containsKey(connection.getName())) {
                IDBConnector oldConnection = removeConnection(connection.getName());
                if (listener != null) {
                    listener.closingAndCreatingNewConnection(connection.getName());
                }

                try {
                    if (oldConnection.isOpen()) {
                        oldConnection.close();
                    }
                } catch (DatabaseException e) {
                    Log.w(Datastore.class.getSimpleName(), "Failed to close old connection to " +
                            "datastore with name " + connection.getName() + " [" + e + "]");
                }
            }

            if (!myDBConnections.containsKey(connection.getName())) {
                // This double checking ensures the same connection can't
                // be created twice across threads
                synchronized (Datastore.class) {
                    if (!myDBConnections.containsKey(connection.getName())) {
                        addConnection(connection);
                    }
                }
            }
        }
    }


    /**
     * This method creates a schema on the database that currently active for an
     * instance of this class.
     * <p>
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
        if (myDBConnections != null) {
            for (Map.Entry<String, IDBConnector> entry : myDBConnections.entrySet()) {
                IDBConnector conn = entry.getValue();
                if (conn.isOpen()) {
                    conn.close();
                }
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

    public void closeAndRemoveConnection() throws DatabaseException {
        if (myActiveDatabase != null && myActiveDatabase.isOpen()) {
            close();

            removeConnection(myActiveDatabase.getName());
        }
    }

    /**
     * This method is used to add a manual deserializer that will be used to deserialize a query
     * for any query on the passed in table.
     * <p>
     * Multiple deserilizer's can be passed in for the same table as long as the deserilizer is
     * typed with different classed.
     * <p>
     * This method can increase the performance of queries as well as create more complex data
     * structures.
     *
     * @param tableName
     * @param deserializer
     */
    public static void addQueryDeserializer(String tableName, QueryDeserializer deserializer) {
        Method[] methods = deserializer.getClass().getMethods();
        Class deserializerClass = Object.class;
        for (Method method : methods) {
            if (method.getName().equals("parseData")) {
                Class clazz = method.getReturnType();
                if (!clazz.getComponentType().getName().equals(Object.class.getName())) {
                    deserializerClass = clazz.getComponentType();
                    break;
                }
            }
        }
        if (deserializerClass == null) {
            throw new RuntimeException("The passed in deserializer has not been typed.");
        }
        if (myDeserializers.containsKey(tableName)) {
            Map<Class, QueryDeserializer> deserializerMap = myDeserializers.get(tableName);
            deserializerMap.put(deserializerClass, deserializer);
        } else {
            Map<Class, QueryDeserializer> deserializerMap = new HashMap<>();
            deserializerMap.put(deserializerClass, deserializer);
            myDeserializers.put(tableName, deserializerMap);
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
        for (Method method : methods) {
            if (method.getName().equals("onQueryComplete")) {
                Class[] clazzes = method.getParameterTypes();
                for (Class clazz : clazzes) {
                    if (clazz.getComponentType() != null) {
                        Class testClass = clazz.getComponentType();
                        if (!clazz.getComponentType().getName().equals(Object.class.getName())) {
                            injectorClass = clazz.getComponentType();
                        }
                    }
                }
            }
        }
        try {
            if (!validateDBConnection()) {
                throw new DatabaseException(
                        "Attempt to reopen an already closed database object. "
                                + "Ensure a connection to the database is currently valid and open");
            }
        } catch (DatabaseException e) {
            listener.onQueryFailed(token, e);
        }


        int taskId = new AtomicInteger().incrementAndGet();
        DatabaseQueryTask task = new DatabaseQueryTask(taskId, token,
                this, stmt);
        task.setOnQueryCompleteListener(listener);
        task.setInjectorClass(injectorClass);

        QueryDeserializer queryDeserializer = getQueryDeserializer(stmt, injectorClass);
        task.setQueryDeserializer(queryDeserializer);

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
        if (!validateDBConnection()) {
            throw new DatabaseException(
                    "Attempt to reopen an already closed database object. "
                            + "Ensure a connection to the database is currently valid and open");
        }


        int taskId = new AtomicInteger().incrementAndGet();
        DatabaseQueryTask task = new DatabaseQueryTask(taskId, DEFAULT_TOKEN,
                this, stmt);

        QueryDeserializer queryDeserializer = getQueryDeserializer(stmt, classToInject);
        task.setQueryDeserializer(queryDeserializer);

        return task.startTask(classToInject);
    }

    /**
     * This method executes a Aggregate query on the database and returns the resultant
     * object. If an error occurs a DatabaseException is passed back in
     * the listener. The listener can be null, but if an exception occurs this
     * statement will fail silently.
     *
     * @param token
     * @param stmt
     * @param listener
     */
    public void executeAggregateQuery(int token, AggregateQuery stmt, OnAggregateQueryComplete listener) {
        try {
            if (!validateDBConnection()) {
                throw new DatabaseException(
                        "Attempt to reopen an already closed database object. "
                                + "Ensure a connection to the database is currently valid and open");
            }
        } catch (DatabaseException e) {
            listener.onQueryFailed(token, e);
        }
        int taskId = new AtomicInteger().incrementAndGet();
        AggregateQueryTask task = new AggregateQueryTask(taskId, DEFAULT_TOKEN,
                this, stmt);
        task.setOnQueryCompleteListener(listener);

        try {
            executeStmt(task);
        } catch (DatabaseException e) {
            listener.onQueryFailed(token, e);
        }
    }

    /**
     * This method executes a aggregate query on the database and returns the resultant
     * object. This method will always block the calling thread
     * regardless if this object has been set to block or not.
     *
     * @param stmt
     * @return
     * @throws DatabaseException
     */
    public Object executeAggregateQuery(AggregateQuery stmt) throws DatabaseException {
        if (!validateDBConnection()) {
            throw new DatabaseException(
                    "Attempt to reopen an already closed database object. "
                            + "Ensure a connection to the database is currently valid and open");
        }

        int taskId = new AtomicInteger().incrementAndGet();
        AggregateQueryTask task = new AggregateQueryTask(taskId, DEFAULT_TOKEN,
                this, stmt);
        return task.run();
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

        int taskId = new AtomicInteger().incrementAndGet();
        DatastoreTransaction dt = new DatastoreTransaction();
        dt.setConnection(myActiveDatabase);
        dt.addStatement(stmt);
        DatabaseNonQueryTask task = new DatabaseNonQueryTask(taskId, token,
                this, dt);
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

        int taskId = new AtomicInteger().incrementAndGet();
        DatastoreTransaction dt = new DatastoreTransaction();
        dt.setConnection(myActiveDatabase);
        dt.addStatement(stmt);
        DatabaseNonQueryTask task = new DatabaseNonQueryTask(taskId,
                DEFAULT_TOKEN, this, dt);
        task.startTask();

        return task.getTaskResult();
    }

    /**
     * This method will check if records exist with the given whereclause. If they
     * exist an update of this records will be executed based on the values in the
     * given insert. If no records exist the insert will be executed.
     * <p>
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
        RowCountQuery query = new RowCountQuery(insert.getTable());
        query.whereClause(whereClause);
        executeAggregateQuery(token, query, new OnAggregateQueryComplete() {

            @Override
            public void onQueryComplete(int token, Object result) {
                long rowCount = (long) result;

                //do an insert if no records are found or an update otherwise.
                Statement updateOrAddStmt = null;
                if (rowCount <= 0) {
                    updateOrAddStmt = insert;
                } else {
                    if (insert.getInsertRowClasses() != null) {
                        Update update = new Update(insert.getTable(),
                                insert.getInsertRowClasses());
                        updateOrAddStmt = update;
                    } else {
                        Map<String, Object> values = insert.getValues();

                        if (!values.isEmpty()) {
                            Update update = new Update(insert.getTable(),
                                    values).whereClause(whereClause);
                            updateOrAddStmt = update;

                        }
                    }
                    ((Update) updateOrAddStmt).whereClause(whereClause);
                }

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
                if (listener != null) {
                    listener.onNonQueryFailed(token, e);
                }
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
    public boolean isConnectionAvail() {
        try {
            return myActiveDatabase.isOpen();
        } catch (DatabaseException e) {
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
    public void doesTableExist(int token, String tableName, final OnNonQueryComplete listener) {

        int taskId = new AtomicInteger().incrementAndGet();
        DatastoreTransaction dt = new DatastoreTransaction();
        dt.setConnection(myActiveDatabase);
        dt.addStatement(new Statement(tableName));
        DatabaseNonQueryTask task = new DatabaseNonQueryTask(taskId, token,
                this, dt);
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
        int taskId = new AtomicInteger().incrementAndGet();
        CreateTableTask task = new CreateTableTask(taskId, token,
                this, table);
        task.addOnStmtCompleteListener(listener);

        try {
            executeStmt(task);
        } catch (DatabaseException e) {
            listener.onNonQueryFailed(token, e);
        }
    }

    public void executeRawStatement(int token, IRawStatement stmt) throws DatabaseException {
        int taskId = new AtomicInteger().incrementAndGet();
        DatastoreTransaction dt = new DatastoreTransaction();
        dt.setConnection(myActiveDatabase);
        dt.addStatement(stmt);
        DatabaseNonQueryTask task = new DatabaseNonQueryTask(taskId,
                DEFAULT_TOKEN, this, dt);
        task.startTask();
    }

    public String getRawQuery(Query query) throws DatabaseException {
        AndroidQueryCreator aqc = new AndroidQueryCreator(myActiveDatabase.getDatabaseSchema());

        if (query.getJoins().isEmpty()) {
            //TODO
            throw new UnsupportedOperationException("This method does not support creating a " +
                    " raw query from a Query object that doesn't contain a join at this time");
        } else {
            return aqc.getSQLJoinQuery(query);
        }
    }

    public Uri getTableUri(String tableName) {
        return myActiveDatabase.getTableUri(tableName);
    }

    IDBConnector getActiveDatabase() {
        return myActiveDatabase;
    }

    boolean validateDBConnection() throws DatabaseException {
        if (isConnectionAvail()) {
            return true;
        }

        if (myIsAttemptReconnect) {
            try {
                myLogs.add(new ConnectionLog(myActiveDatabase.getName(),
                        ConnectionStatus.ATTEMPT_RECONNECT, System.currentTimeMillis()));
                myActiveDatabase.createConnection();
                myLogs.add(new ConnectionLog(myActiveDatabase.getName(),
                        ConnectionStatus.OPENED, System.currentTimeMillis()));

            } catch (DatabaseException e) {
                myLogs.add(new ConnectionLog(myActiveDatabase.getName(),
                        ConnectionStatus.RECONNECT_FAILED, System.currentTimeMillis(), e));
                Log.e(Datastore.class.getSimpleName(), "Failed to recreate a connection "
                        + "to the database [" + e + "]");
                return false;
            }

            return true;
        }

        return false;
    }

    private static void addConnection(IDBConnector connection) {

        myDBConnections.put(connection.getName(), connection);
        myLogs.add(new ConnectionLog(connection.getName(),
                ConnectionStatus.ADDED, System.currentTimeMillis()));
    }

    private static IDBConnector removeConnection(String dbName) {
        IDBConnector connector = myDBConnections.remove(dbName);
        if (connector != null) {
            myLogs.add(new ConnectionLog(dbName,
                    ConnectionStatus.REMOVED, System.currentTimeMillis()));
        }

        return connector;
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
                if (myQueuedTasks.get(0).isTaskRunning()) {
                    onTaskComplete(myQueuedTasks.get(0));
                } else {
                    myQueuedTasks.get(0).execute();
                }
            }
        }
    }

    private QueryDeserializer getQueryDeserializer(Query stmt, Class injectorClass) {
        QueryDeserializer queryDeserializer = null;
        if (myDeserializers.containsKey(stmt.getTable())) {
            Map<Class, QueryDeserializer> tableDeserializers = myDeserializers.get(stmt.getTable());
            if (tableDeserializers.containsKey(injectorClass)) {
                queryDeserializer = tableDeserializers.get(injectorClass);
            }
        }
        return queryDeserializer;
    }
}
