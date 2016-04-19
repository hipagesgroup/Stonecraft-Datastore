package com.stonecraft.datastore.interaction;

/**
 * Created by michaeldelaney on 18/11/15.
 */
public class RawStatement extends IRawStatement {
    private String myRawStatement;

    public RawStatement(String table, String rawStatement) {
        super(table);
        myRawStatement = rawStatement;
    }

    @Override
    public String getRawStatement() {
        return myRawStatement;
    }
}
