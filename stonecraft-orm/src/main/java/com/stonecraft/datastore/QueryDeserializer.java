package com.stonecraft.datastore;

/**
 * This class
 * <p/>
 * Author: michaeldelaney
 * Created: 20/04/16
 */
public interface QueryDeserializer <T> {

    /**
     * This method is used to parse the data that has been returned from the database query.
     *
     * @param data
     * @return
     */
    public T[] parseData(RSData data);
}
