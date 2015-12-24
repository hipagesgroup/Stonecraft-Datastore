package com.stonecraft.datastore;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.Delete;
import com.stonecraft.datastore.interaction.Insert;
import com.stonecraft.datastore.interaction.RawStatement;
import com.stonecraft.datastore.interaction.Statement;
import com.stonecraft.datastore.interaction.Update;
import com.stonecraft.datastore.interaction.UpdateTableStatement;
import com.stonecraft.datastore.interfaces.IDBConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michaeldelaney on 18/11/15.
 */
public class DatastoreTransaction {
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

    void setConnection(IDBConnector connection) {
        myConnection = connection;
    }

    int run() throws DatabaseException{
        try {
            int result = 0;
            myConnection.startTransaction();
            for(Statement stmt : myStatementList) {
                if(stmt instanceof Insert) {
                    myConnection.insert((Insert) stmt);
                    result = 1;
                } else if(stmt instanceof Update) {
                    result = myConnection.update((Update) stmt);
                } else if(stmt instanceof Delete) {
                    result = myConnection.delete((Delete) stmt);
                } else if(stmt instanceof RawStatement) {
                    myConnection.executeRawStatement(((RawStatement) stmt).getRawStatement());
                } else if(stmt instanceof UpdateTableStatement) {
                    UpdateTableStatement updateTableStatement = (UpdateTableStatement)stmt;
                    myConnection.updateTable(
                            updateTableStatement.getOldTable(), updateTableStatement.getNewTable());
                } else {
                    result = myConnection.doesTableExist(stmt.getTable());
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
