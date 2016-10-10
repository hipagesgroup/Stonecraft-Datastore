package com.stonecraft.datastore;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.os.CancellationSignal;
import android.support.v4.os.OperationCanceledException;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.Query;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by michaeldelaney on 18/11/15.
 */
public class DbDataLoader<T> extends AsyncTaskLoader<T> {

    private volatile CancellationSignal myCancellationSignal;
    private Map<Uri, ForceLoadContentObserver> myObservers;
    private T myResult;
    private final String myDbName;
    private final Query myQuery;
    private final Class myLoaderResultType;
    private Calendar myTableUpdateTime;
    private boolean myIsIgnoringUpdates;
    private AsyncTask.Status myStatus = AsyncTask.Status.PENDING;

    public DbDataLoader(Context context, String dbName, Query query, Class loaderResultType) {
        super(context);
        myObservers = new HashMap<>();
        Datastore ds = Datastore.getDataStore(dbName);
        addWatchUri(ds.getTableUri(query.getTable()));
        myDbName = dbName;
        myQuery = query;
        myLoaderResultType = loaderResultType;
    }

    public AsyncTask.Status getStatus() {
        return myStatus;
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
        myStatus = AsyncTask.Status.FINISHED;
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
        myIsIgnoringUpdates = true;
        unregisterContentObservers();

    }

    public void receiveUpdates(){
        myIsIgnoringUpdates = false;
        if(isStarted()) {
            ContentResolver cr = getContext().getContentResolver();
            for(Map.Entry<Uri, ForceLoadContentObserver> entry : myObservers.entrySet()) {
                cr.registerContentObserver(entry.getKey(), false, entry.getValue());
            }
        }
    }

    /**
     * This method adds a uri that this loader will receive updates and restart should any changes
     * occur.
     *
     * By default this loader will listen to changes to the query this loader is based on. If this
     * loader should not listen to these changes removeWatchUri should be called with the uri
     * that matches the main tables uri found in the query that was passed in when this class was
     * instantiated
     *
     * @param uri
     */
    public void addWatchUri(@Nullable Uri uri){
        if(uri != null && !myObservers.containsKey(uri)){
            ForceLoadContentObserver observer = new ForceLoadContentObserver();
            myObservers.put(uri, observer);
            if(isStarted()) {
                ContentResolver cr = getContext().getContentResolver();
                cr.registerContentObserver(uri, false, observer);
            }
        }
    }

    /**
     * This method removes the uri from this class so that it no longer hears changes that occur on
     * this uri.
     *
     * @param uri
     */
    public void removeWatchUri(Uri uri){
        if(myObservers.containsKey(uri)){
            if(isStarted()) {
                ContentResolver cr = getContext().getContentResolver();
                cr.unregisterContentObserver(myObservers.get(uri));
            }

            myObservers.remove(uri);
        }
    }

    @Override
    protected void onStartLoading() {
        if(!myIsIgnoringUpdates) {
            receiveUpdates();
        }

        Datastore ds = Datastore.getDataStore(myDbName);
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
        unregisterContentObservers();
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        myStatus = AsyncTask.Status.RUNNING;
    }

    @Override
    protected boolean onCancelLoad() {
        myStatus = AsyncTask.Status.FINISHED;
        return super.onCancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();
    }

    private void unregisterContentObservers() {
        ContentResolver cr = getContext().getContentResolver();
        for(ForceLoadContentObserver observer : myObservers.values()) {
            cr.unregisterContentObserver(observer);
        }
    }
}
