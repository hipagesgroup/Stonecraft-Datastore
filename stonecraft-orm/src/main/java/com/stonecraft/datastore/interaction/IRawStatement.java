package com.stonecraft.datastore.interaction;

/**
 * Created by michaeldelaney on 18/11/15.
 */
public abstract class IRawStatement extends Statement {
    /**
     * Constructor
     *
     * @param table
     */
    public IRawStatement(String table) {
        super(table);
    }

    public abstract String getRawStatement();
}
