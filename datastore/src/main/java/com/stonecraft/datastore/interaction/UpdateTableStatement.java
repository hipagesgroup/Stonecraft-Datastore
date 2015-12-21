package com.stonecraft.datastore.interaction;

import com.stonecraft.datastore.view.DatabaseTable;

/**
 * Created by michaeldelaney on 18/11/15.
 */
public class UpdateTableStatement extends Statement {
    private DatabaseTable myOldTable;
    private DatabaseTable myNewTable;

    public UpdateTableStatement(String table, DatabaseTable oldTable, DatabaseTable newTable) {
        super(table);
        myOldTable = oldTable;
        myNewTable = newTable;
    }

    public DatabaseTable getOldTable() {
        return myOldTable;
    }

    public DatabaseTable getNewTable() {
        return myNewTable;
    }
}
