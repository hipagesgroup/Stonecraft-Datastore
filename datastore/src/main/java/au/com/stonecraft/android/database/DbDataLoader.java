package au.com.stonecraft.android.database;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.os.CancellationSignal;
import android.support.v4.os.OperationCanceledException;

import au.com.stonecraft.common.database.Datastore;
import au.com.stonecraft.common.database.exceptions.DatabaseException;
import au.com.stonecraft.common.database.interaction.Query;

/**
 * Created by michaeldelaney on 18/11/15.
 */
public class DbDataLoader<T> extends AsyncTaskLoader<T> {

    private CancellationSignal myCancellationSignal;
    private final ForceLoadContentObserver myObserver;
    private T myResult;
    private final String myDbName;
    private final Query myQuery;
    private final Class myLoaderResultType;


    public DbDataLoader(Context context, String dbName, Query query, Class loaderResultType) {
        super(context);
        myObserver = new ForceLoadContentObserver();
        myDbName = dbName;
        myQuery = query;
        myLoaderResultType = loaderResultType;
    }

    @Override
    public T loadInBackground() {
        synchronized (this) {
            if (isLoadInBackgroundCanceled()) {
                throw new OperationCanceledException();
            }
            myCancellationSignal = new CancellationSignal();
        }

        Datastore ds = Datastore.getDataStore(myDbName);
        getContext().getContentResolver().registerContentObserver(ds.getTableUri(
                myQuery.getTable()), false, myObserver);

        T result = null;
        try {
            result = (T)ds.executeQuery(myQuery, myLoaderResultType);
        } catch (DatabaseException e) {
            throw new RuntimeException("An error occured in the loader while trying to get data " +
                    "from the database", e);
        } catch (ClassCastException e) {
            throw new ClassCastException("The class type passed into the loader does not " +
                    "match the generic type of the loader. [" + e + "]");
        }

        myCancellationSignal = null;

        return result;
    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();

        synchronized (this) {
            if (myCancellationSignal != null) {
                myCancellationSignal.cancel();
            }
        }
    }

    @Override
    public void deliverResult(T result) {
        if (isReset()) {
            myResult = null;
            return;
        }
        myResult = result;

        if (isStarted()) {
            super.deliverResult(result);
        }
    }

    @Override
    protected void onStartLoading() {
        if (myResult != null) {
            deliverResult(myResult);
        }
        if (takeContentChanged() || myResult == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
        getContext().getContentResolver().unregisterContentObserver(myObserver);
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();
    }


}
