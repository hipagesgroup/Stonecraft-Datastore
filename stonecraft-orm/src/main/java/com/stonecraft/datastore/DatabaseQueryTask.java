package com.stonecraft.datastore;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.Query;
import com.stonecraft.datastore.interaction.RawSQLQuery;
import com.stonecraft.datastore.interaction.Statement;
import com.stonecraft.datastore.interfaces.IDBConnector;
import com.stonecraft.datastore.interfaces.OnQueryComplete;

/**
 * This class is used for tasks that are to be run on a database. It handles
 * whether the task is to be run on a separate thread and notifies any listeners
 * on completion.
 * 
 * @author Michael Delaney
 * @author $Author: michael.delaney $
 * @created 16/03/2012
 * @date $Date: 16/03/2012 01:50:39 $
 * @version $Revision: 1.0 $
 */
class DatabaseQueryTask extends DatabaseTask {
	private Statement myQuery;
	private OnQueryComplete myQueryListener;
	private Class myInjectorClass;
	private Object[] myResult;

	public DatabaseQueryTask(int taskId, int token, IDBConnector conn,
		Statement query) {
		super(taskId, token, conn);
		myQuery = query;
	}

	public void setInjectorClass(Class injectorClass) {
		myInjectorClass = injectorClass;
	}

	/**
	 * This method executes the task, and returns the result to a listener that has been
     * set using setOnQueryCompleteListener(). This method also expects that you have set
     * the Injector class via setInjectorClass().
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	@Override
	public void startTask() throws DatabaseException {

		myResult = startTask(myInjectorClass);
	}

    /**
     * This method queries the data base and parses the data into an instance of the class that
     * is passed in.
     * @param classOfT
     * @param <T>
     * @return
     * @throws DatabaseException
     */
    public <T> T[] startTask(Class<T> classOfT) throws DatabaseException {

		RSData data = null;
        try {
            if (myQuery instanceof Query) {
                data = myConnection.query((Query)myQuery);
                return (T[])parseQuery(data, classOfT);
            } else if (myQuery instanceof RawSQLQuery) {
                data = myConnection.executeRawQuery((((RawSQLQuery)myQuery)).getQuery());
                return (T[])parseQuery(data, classOfT);
            }
            else {
                throw new DatabaseException("Unknown statement type "
                        + myQuery.getClass().getSimpleName()
                        + ". Must be either " + Query.class.getSimpleName()
                        + " or " + RawSQLQuery.class.getSimpleName());
            }
        } catch (DatabaseException e) {
			if(data != null) {
				data.close();
			}
            myQueryListener.onQueryFailed(myToken, e);
        } finally {
            notifyTaskListeners();
        }

        return null;
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
				myQueryListener.onQueryComplete(myToken, myResult);
			}
		}
	}

	private Object parseQuery(RSData data, Class classOfT) throws DatabaseException {

		if(classOfT.getName().equals(RSData.class.getName())) {
			return new RSData[] {data};
		}

		Object[] result = null;
		if(myQueryListener != null) {
			result = myQueryListener.parseData(data);
		}
		if(result == null) {
            return new DatabaseObjectInjector().inject(data, classOfT);
		}

		data.close();
        return result;
	}


	/**
	 * This method adds a listener that will be notified when this statement has
	 * completed.
	 * 
	 * @param listener
	 */
	public void setOnQueryCompleteListener(OnQueryComplete listener) {
		myQueryListener = listener;
	}
}
