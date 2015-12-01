package au.com.stonecraft.common.database;

import au.com.stonecraft.common.database.exceptions.DatabaseException;
import au.com.stonecraft.common.database.interaction.Query;
import au.com.stonecraft.common.database.interaction.RawSQLQuery;
import au.com.stonecraft.common.database.interaction.Statement;
import au.com.stonecraft.common.database.interfaces.IDBConnector;
import au.com.stonecraft.common.database.interfaces.OnQueryComplete;

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
	private RSData myResult;
	private Class myInjectorClass;

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
	public void executeTask() {

        try {
            Object[] result = executeTask(myInjectorClass);
            myQueryListener.onQueryComplete(myToken, result);
        } catch (DatabaseException e) {
            myQueryListener.onQueryFailed(myToken, e);
        }
	}

    /**
     * This method queries the data base and parses the data into an instance of the class that
     * is passed in.
     * @param classOfT
     * @param <T>
     * @return
     * @throws DatabaseException
     */
    public <T> T[] executeTask(Class<T> classOfT) throws DatabaseException {

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

	private Object parseQuery(RSData data, Class classOfT) throws DatabaseException {

		if(classOfT.getName().equals(RSData.class.getName())) {
			return new RSData[] {data};
		}

		Object[] result = null;
		if(myQueryListener != null) {
			myQueryListener.parseData(myResult);
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

	/**
	 * This method returns the number of records that were
	 * updated/deleted/inserted in this task. executeTask() should be called
	 * before this method is called. null will be returned otherwise.
	 * 
	 * @return
	 */
	public RSData getTaskResult() {
		return myResult;
	}

	private <T> T[] getInjectedObjects(Class<T> classToInject) throws DatabaseException {
		RSData data = null;
		if (myQuery instanceof Query) {
			data = myConnection.query((Query)myQuery);
		} else if (myQuery instanceof RawSQLQuery) {
			data = myConnection.executeRawQuery((((RawSQLQuery)myQuery)).getQuery());
		}
		else {
			throw new DatabaseException("Unknown statement type "
					+ myQuery.getClass().getSimpleName()
					+ ". Must be either " + Query.class.getSimpleName()
					+ " or " + RawSQLQuery.class.getSimpleName());
		}


		T[] returnList = new DatabaseObjectInjector().inject(data, classToInject);
		data.close();
		return returnList;
	}

}
