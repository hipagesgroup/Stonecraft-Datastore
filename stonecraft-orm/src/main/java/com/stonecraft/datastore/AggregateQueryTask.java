package com.stonecraft.datastore;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interfaces.IDBConnector;

/**
 * This class is used to execute aggregate functions. the result type will depend on the type
 * of aggregation being done.
 * eg. If a RowCountQuery is being done a Long will be returned. If a FirstValueQuery is executed
 * a string will be returned.
 *
 * <p/>
 * Author: michaeldelaney
 * Created: 20/01/16
 */
public class AggregateQueryTask extends DatabaseTask {

    private AggregateQuery myQuery;
    private OnUnparsedQueryComplete myQueryListener;
    private Object myResult;

    public AggregateQueryTask(int taskId, int token, Datastore datastore,
            AggregateQuery query) {
        super(taskId, token, datastore);
        myQuery = query;
    }

    @Override
    public void startTask() throws DatabaseException {
        myResult = run();
    }

    public Object run() throws DatabaseException {
        IDBConnector connector = myDatastore.getActiveDatabase();
        if(myQuery instanceof RowCountQuery) {
            return connector.queryNumEntries((RowCountQuery)myQuery);
        } else {
            throw new DatabaseException("Unknown statement type "
                    + myQuery.getClass().getSimpleName());
        }
    }

    /**
     * This method adds a listener that will be notified when this statement has
     * completed.
     *
     * @param listener
     */
    public void setOnQueryCompleteListener(OnUnparsedQueryComplete listener) {
        if(listener instanceof QueryComplete) {
            throw new RuntimeException("QueryComplete interface should not be used directly. " +
                    "Instead use OnQueryComplete or OnUnparsedQueryComplete");
        }
        myQueryListener = listener;
    }

    /**
     * This method notifys any listeners that the task has completed.
     */
    @Override
    void notifyStmtListeners(DatabaseException e) {
        if(myQueryListener != null) {
            if (e != null) {
                myQueryListener.onQueryFailed(myToken, e);
            } else {
                myQueryListener.onQueryComplete(
                        myToken, (RSData) myResult);
            }
        }
    }
}
