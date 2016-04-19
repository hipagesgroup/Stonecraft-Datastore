package com.stonecraft.datastore.interaction;

/**
 * Created by michaeldelaney on 18/11/15.
 */
public class CreateViewStatement extends IRawStatement {

    private static final String STATEMENT_CREATE_VIEW_WITH_COLS = "CREATE VIEW %s (%s) AS %s";
    private static final String STATEMENT_CREATE_VIEW = "CREATE VIEW IF NOT EXISTS %s AS %s";

    private String myViewName;
    private String myQuery;
    private String[] myColumns;

    public CreateViewStatement(String viewName, String query) {
        super(viewName);
        myViewName = viewName;
        myQuery = query;
    }

    public String[] getColumns() {
        return myColumns;
    }

    public CreateViewStatement setColumns(String[] columns) {
        myColumns = columns;
        return this;
    }

    public String getRawStatement() {
        // TODO This statement should use a Query object to generate the statement using
        // TODO AndroidQueryCreator

        return String.format(STATEMENT_CREATE_VIEW, myViewName, myQuery);
    }
}
