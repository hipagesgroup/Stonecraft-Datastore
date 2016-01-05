package com.stonecraft.datastore;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.Delete;
import com.stonecraft.datastore.interaction.Insert;
import com.stonecraft.datastore.interaction.RawStatement;
import com.stonecraft.datastore.interaction.Statement;
import com.stonecraft.datastore.interaction.Update;
import com.stonecraft.datastore.interaction.UpdateTableStatement;
import com.stonecraft.datastore.interfaces.IDBConnector;
import com.stonecraft.datastore.interfaces.OnNonQueryComplete;

import java.util.ArrayList;
import java.util.List;
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
        myConnection = ds.getActiveDatabase();
        int taskId = new AtomicInteger().incrementAndGet();
        DatabaseNonQueryTask task = new DatabaseNonQueryTask(taskId,
                DEFAULT_TOKEN, ds, this);
        task.addOnStmtCompleteListener(listener);
        task.execute();
    }

    /**
     * This method executes this transaction on the current thread.
     *
     * @param ds
     * @return
     * @throws DatabaseException
     */
    public int executeOnCurrentThread(Datastore ds) throws DatabaseException {
        myConnection = ds.getActiveDatabase();
        return run();
    }

    void setConnection(IDBConnector connection) {
        myConnection = connection;
    }

    int run() throws DatabaseException {
        try {
            int result = 0;
            myConnection.startTransaction();
            for(Statement stmt : myStatementList) {
                if(stmt instanceof Insert) {
                    myConnection.insert((Insert) stmt);
                    result += 1;
                } else if(stmt instanceof Update) {
                    result += myConnection.update((Update) stmt);
                } else if(stmt instanceof Delete) {
                    result += myConnection.delete((Delete) stmt);
                } else if(stmt instanceof RawStatement) {
                    myConnection.executeRawStatement(((RawStatement) stmt).getRawStatement());
                } else if(stmt instanceof UpdateTableStatement) {
                    UpdateTableStatement updateTableStatement = (UpdateTableStatement)stmt;
                    myConnection.updateTable(
                            updateTableStatement.getOldTable(), updateTableStatement.getNewTable());
                } else {
                    result += myConnection.doesTableExist(stmt.getTable());
                }
            }

            myConnection.commit();
            return result;
        }
        catch (DatabaseException e) {
            myConnection.rollBack();
            throw new DatabaseException("Failed to execute the transaction", e);
        }
    }
}
