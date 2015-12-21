package com.stonecraft.datastore.datastoredemo.app;

import android.app.Application;
import android.util.Log;

import java.io.IOException;

import com.stonecraft.datastore.android.AndroidDBConnection;
import com.stonecraft.datastore.DatabaseSchema;
import com.stonecraft.datastore.Datastore;
import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interfaces.ISchemaCreator;
import com.stonecraft.datastore.parser.DatabaseParser;

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
        try
        {
            ISchemaCreator db = new ISchemaCreator()
            {
                @Override
                public void postCreation() {

                }

                @Override
                public DatabaseSchema getSchema() {
                    try
                    {
                        DatabaseParser parser = new DatabaseParser();
                        return parser.parse(getAssets().open("database.xml"));
                    }
                    catch(IOException e)
                    {
                        Log.e("DB connection/creation", "Failed to create DB schema [" + e + "]");
                    }

                    return null;
                }
            };

            Datastore.createConnection(new AndroidDBConnection(this, db));
        }
        catch(DatabaseException e)
        {
            Log.e("DB connection/creation", "Failed to create Database [" + e + "]");
        }
    }
}
