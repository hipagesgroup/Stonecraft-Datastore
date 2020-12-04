package com.stonecraft.datastore;

import android.database.SQLException;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.Delete;
import com.stonecraft.datastore.interaction.IRawStatement;
import com.stonecraft.datastore.interaction.Insert;
import com.stonecraft.datastore.interaction.Statement;
import com.stonecraft.datastore.interaction.Update;
import com.stonecraft.datastore.interaction.UpdateTableStatement;
import com.stonecraft.datastore.interfaces.IDBConnector;
import com.stonecraft.datastore.interfaces.OnNonQueryComplete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by michaeldelaney on 18/11/15.
 */
public class DatastoreTransaction {
    public static final int DEFAULT_TOKEN = -1;
    private List<Statement> myStatementList;
    private IDBConnector myConnection;

    public DatastoreTransaction() {
        myStatementList = new ArrayList<Statement>();
    }

    /**
     * This method add the passed in statement to the list of statements that are run. If a single
     * statement fails all statements will be rolled back
     * @param statement
     */
    public void addStatement(Statement statement) {
        myStatementList.add(statement);
    }

    /**
     * This method removes a statement from the list of statements to be run.
     * @param statement
     */
    public void removeStatement(Statement statement) {
        myStatementList.remove(statement);
    }

    public int getStatementCount() {
        return myStatementList.size();
    }

    /**
     * This method executes this transaction on a new thread and returns the result to the
     * passed in listener.
     *
     * @param ds
     */
    public void execute(Datastore ds, OnNonQueryComplete listener) {
        try {
            ds.validateDBConnection();
            myConnection = ds.getActiveDatabase();
            int taskId = new AtomicInteger().incrementAndGet();
            DatabaseNonQueryTask task = new DatabaseNonQueryTask(taskId,
                    DEFAULT_TOKEN, ds, this);
            task.addOnStmtCompleteListener(listener);
            task.execute();
        } catch (DatabaseException e) {
            listener.onNonQueryFailed(DEFAULT_TOKEN, e);
        }

    }

    /**
     * This method executes this transaction on the current thread.
     *
     * @param ds
     * @return
     * @throws DatabaseException
     */
    public int executeOnCurrentThread(Datastore ds) throws DatabaseException {
        ds.validateDBConnection();
        myConnection = ds.getActiveDatabase();
        return run();
    }

    void setConnection(IDBConnector connection) {
        myConnection = connection;
    }

    int run() throws DatabaseException {

        Map<String, String> updatedTableUris = new HashMap<>();
        int result = 0;
        myConnection.startTransaction();
        for(Statement stmt : myStatementList) {
            try {
                updatedTableUris.put(stmt.getTable(), stmt.getTable());

                if (stmt instanceof Insert) {
                    myConnection.insert((Insert) stmt);
                    result += 1;
                } else if (stmt instanceof Update) {
                    result += myConnection.update((Update) stmt);
                } else if (stmt instanceof Delete) {
                    result += myConnection.delete((Delete) stmt);
                } else if (stmt instanceof IRawStatement) {
                    myConnection.executeRawStatement(((IRawStatement) stmt).getRawStatement());
                } else if (stmt instanceof UpdateTableStatement) {
                    UpdateTableStatement updateTableStatement = (UpdateTableStatement) stmt;
                    myConnection.updateTable(
                            updateTableStatement.getOldTable(),
                            updateTableStatement.getNewTable());
                } else {
                    result += myConnection.doesTableExist(stmt.getTable());
                }
            } catch (DatabaseException | SQLException e) {
                    myConnection.rollBack();
                    throw new DatabaseException("Failed to execute the transaction " + stmt.toString(), e);
            }
        }

        for(String tableName : updatedTableUris.keySet()) {
            myConnection.sendTableUpdateNotification(tableName);
        }
            myConnection.commit();
        return result;
    }
}
