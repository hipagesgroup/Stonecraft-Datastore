package au.com.stonecraft.android.database;

import android.net.Uri;

import au.com.stonecraft.common.database.DbColumnName;
import au.com.stonecraft.common.database.DBConstants;

/**
 * Created by michaeldelaney on 18/11/15.
 */
public class DataMap {
    @DbColumnName(DBConstants.COLUMN_TABLE_NAME)
    private String myTableName;
    @DbColumnName(DBConstants.COLUMN_COLUMN_NAME)
    private String myColunnName;
    @DbColumnName(DBConstants.COLUMN_DATA_TYPE)
    private int myType;
    @DbColumnName(DBConstants.COLUMN_DATA_LENGTH)
    private int myLength;
    @DbColumnName(DBConstants.COLUMN_IS_PRIMARY_KEY)
    private boolean myIsPrimarykey;
    @DbColumnName(DBConstants.COLUMN_IS_AUTOINCREMENTING)
    private boolean myIsAutoIncrement;
    @DbColumnName(DBConstants.COLUMN_IS_NULLABLE)
    private boolean myIsNullable;
    @DbColumnName(DBConstants.COLUMN_URI)
    private Uri myUri;

    public String getTableName() {
        return myTableName;
    }

    public void setTableName(String tableName) {
        myTableName = tableName;
    }

    public String getColunnName() {
        return myColunnName;
    }

    public void setColunnName(String colunnName) {
        myColunnName = colunnName;
    }

    public int getType() {
        return myType;
    }

    public void setType(int type) {
        myType = type;
    }

    public int getLength() {
        return myLength;
    }

    public void setLength(int length) {
        myLength = length;
    }

    public boolean isPrimarykey() {
        return myIsPrimarykey;
    }

    public void setIsPrimarykey(boolean isPrimarykey) {
        myIsPrimarykey = isPrimarykey;
    }

    public boolean isAutoIncrement() {
        return myIsAutoIncrement;
    }

    public void setIsAutoIncrement(boolean isAutoIncrement) {
        myIsAutoIncrement = isAutoIncrement;
    }

    public boolean isNullable() {
        return myIsNullable;
    }

    public void setIsNullable(boolean isNullable) {
        myIsNullable = isNullable;
    }

    public Uri getUri() {
        return myUri;
    }

    public void setUri(Uri uri) {
        myUri = uri;
    }
}
