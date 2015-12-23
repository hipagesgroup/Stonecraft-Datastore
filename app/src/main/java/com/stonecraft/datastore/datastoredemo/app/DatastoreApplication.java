package com.stonecraft.datastore.datastoredemo.app;

import android.app.Application;
import android.util.Log;

import com.stonecraft.datastore.Datastore;
import com.stonecraft.datastore.OnConnectionCreated;
import com.stonecraft.datastore.exceptions.DatabaseException;

import java.io.IOException;

/**
 * Created by michaeldelaney on 20/11/15.
 */
public class DatastoreApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        connectToDB();

    }

    private void connectToDB() {
        try {
            Datastore.createConnection(this, getAssets().open("database.xml"),
                    new OnConnectionCreated() {
                        @Override
                        public void OnConnectionCreated(Datastore datastore) {
                            Log.d("TEST", datastore.getTableUri("SHORT_LIST").toString());
                        }
                    });
        } catch(IOException e)
        {
            Log.e("DB connection/creation", "Failed to get database xml [" + e + "]");
        } catch (DatabaseException e) {
            Log.e("DB connection/creation", "Failed to create database [" + e + "]");
        }
    }
}
