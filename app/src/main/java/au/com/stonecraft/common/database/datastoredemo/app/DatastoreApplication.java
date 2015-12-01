package au.com.stonecraft.common.database.datastoredemo.app;

import android.app.Application;
import android.util.Log;

import java.io.IOException;

import au.com.stonecraft.android.database.AndroidDBConnection;
import au.com.stonecraft.common.database.DatabaseSchema;
import au.com.stonecraft.common.database.Datastore;
import au.com.stonecraft.common.database.exceptions.DatabaseException;
import au.com.stonecraft.common.database.interfaces.ISchemaCreator;
import au.com.stonecraft.common.database.parser.DatabaseParser;

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
