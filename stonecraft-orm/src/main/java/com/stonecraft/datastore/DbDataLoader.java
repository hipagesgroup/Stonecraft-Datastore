package com.stonecraft.datastore;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.os.CancellationSignal;
import android.support.v4.os.OperationCanceledException;
import android.text.TextUtils;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.Query;

import java.util.Calendar;

/**
 * Created by michaeldelaney on 18/11/15.
 */
public class DbDataLoader<T> extends AsyncTaskLoader<T> {

    private volatile CancellationSignal myCancellationSignal;
    private final ForceLoadContentObserver myObserver;
    private T myResult;
    private final String myDbName;
    private final Query myQuery;
    private final Class myLoaderResultType;
    private Calendar myTableUpdateTime;

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

    public void ignoreUpdates(){
        getContext().getContentResolver().unregisterContentObserver(myObserver);
    }

    public void receiveUpdates(){
        Datastore ds = Datastore.getDataStore(myDbName);
        if(isStarted() && !TextUtils.isEmpty(ds.getTableUri(
                myQuery.getTable()).toString())) {
            getContext().getContentResolver().registerContentObserver(ds.getTableUri(
                    myQuery.getTable()), false, myObserver);
        }
    }

    @Override
    protected void onStartLoading() {
        Datastore ds = Datastore.getDataStore(myDbName);
        Uri tableUri = ds.getTableUri(
                myQuery.getTable());
        if(tableUri != null && !TextUtils.isEmpty(tableUri.toString())) {
            getContext().getContentResolver().registerContentObserver(ds.getTableUri(
                    myQuery.getTable()), false, myObserver);
        }

        Calendar tableUpdateTime = ds.getLastTableUpdateTime(myQuery.getTable());
        if (takeContentChanged() || myResult == null || tableUpdateTime != myTableUpdateTime ||
                (myResult !=null && tableUpdateTime != null &&
                        tableUpdateTime.compareTo(myTableUpdateTime) > 0)) {
            myTableUpdateTime = tableUpdateTime;
            forceLoad();
            return;
        }

        if (myResult != null) {
            deliverResult(myResult);
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
