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
     * To use built in deserialization and process data further afterwards, use the objectInjector
     * as follows:
     * <pre>MyType[] myTypes = objectInjector.inject(data, MyType.class);</pre>
     *
     * @param data
     * @param objectInjector
     * @return
     */
    public T[] parseData(RSData data, ObjectInjector objectInjector);
}
