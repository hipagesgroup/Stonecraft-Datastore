package au.com.stonecraft.android.database;

import au.com.stonecraft.common.database.DbColumnName;
import au.com.stonecraft.common.database.DBConstants;

/**
 * Created by michaeldelaney on 18/11/15.
 */
public class SettingsTable {
    @DbColumnName(DBConstants.COLUMN_TYPE)
    private String myType;
    @DbColumnName(DBConstants.COLUMN_VALUE)
    private Object myValue;

    public String getType() {
        return myType;
    }

    public void setType(String type) {
        myType = type;
    }

    public Object getValue() {
        return myValue;
    }

    public void setValue(Object value) {
        myValue = value;
    }
}
