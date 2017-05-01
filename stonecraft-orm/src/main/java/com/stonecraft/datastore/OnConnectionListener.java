package com.stonecraft.datastore;

/**
 * This interface is used to when a notification is required when a db connection
 * has been successful
 * <p/>
 * Author: michaeldelaney
 * Created: 23/12/15
 */
public interface OnConnectionListener {
    void OnConnectionCreated(Datastore datastore);

    void onUpgrade(Datastore datastore);

    void onOpen(Datastore datastore);

    void onClose();
}
