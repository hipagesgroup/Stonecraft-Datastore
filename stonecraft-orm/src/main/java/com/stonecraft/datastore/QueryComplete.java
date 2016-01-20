package com.stonecraft.datastore;

import com.stonecraft.datastore.exceptions.DatabaseException;

/**
 * This class
 * <p/>
 * Author: michaeldelaney
 * Created: 20/01/16
 */
interface QueryComplete {

    /**
     * This method is called when the query fails
     * @param token
     * @param e
     */
    public abstract void onQueryFailed(int token, DatabaseException e);
}
