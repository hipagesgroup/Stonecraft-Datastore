package com.stonecraft.datastore;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;

import com.stonecraft.datastore.exceptions.DatabaseException;

import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class
 * <p/>
 * Author: michaeldelaney
 * Created: 1/02/16
 */
public class ContentValueCreator {
    private Field[] myClassFields;
    private Map<Field, Annotation> myAnnotations;

    public ContentValueCreator() {
        myAnnotations = new HashMap<>();
    }

    public ContentValues getContentValues(Object object) throws DatabaseException {
        ContentValues cv = new ContentValues();
        try {
            if(myClassFields == null) {
                myClassFields = object.getClass().getDeclaredFields();
            }

            for(Field field : myClassFields) {
                DbColumnName annotation = (DbColumnName)myAnnotations.get(field);
                if(annotation == null) {
                    annotation = field.getAnnotation(DbColumnName.class);
                    myAnnotations.put(field, annotation);
                }
                if(annotation instanceof DbColumnName) {
                    field.setAccessible(true);
                    String column = annotation.value();
                    Object value = field.get(object);
                    if(value != null) {
                        addToContentValues(cv, column, value);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new DatabaseException("The field that are to annotated to with the database " +
                    "column name so that their value can be inserted into the database must " +
                    "be accessible", e);
        }

        return cv;
    }

    /**
     * This method creates the content values from an EntrySet
     *
     * The key of the set should be the column name with the value being the
     * value for that row
     *
     * @param row
     * @return
     */
    public ContentValues getContentValues(Set<Map.Entry<String, Object>> row)
            throws DatabaseException {
        ContentValues cv = new ContentValues();
        for (Map.Entry<String, Object> entrySet : row) {
            Object value = entrySet.getValue();
            addToContentValues(cv, entrySet.getKey(), value);
        }

        return cv;
    }

    private void addToContentValues(ContentValues cv, String columnName, Object value) throws DatabaseException {
        if(value == null) {
            cv.putNull(columnName);
        } else if (value instanceof Integer) {
            cv.put(columnName, (Integer) value);
        } else if (value instanceof Boolean) {
            cv.put(columnName, ((Boolean) value) ? 1 : 0);
        } else if (value instanceof Double) {
            cv.put(columnName, (Double) value);
        } else if (value instanceof Float) {
            cv.put(columnName, (Float) value);
        } else if (value instanceof Long) {
            cv.put(columnName, (Long) value);
        } else if (value instanceof String) {
            cv.put(columnName, (String) value);
        } else if (value instanceof Date) {
            long timeInMillis = ((Date) value).getTime();
            cv.put(columnName, timeInMillis);
        } else if (value instanceof Calendar) {
            long timeInMillis = ((Calendar) value).getTimeInMillis();
            cv.put(columnName, timeInMillis);
        } else if(value instanceof byte[]) {
            cv.put(columnName, (byte[]) value);
        } else if(value instanceof Bitmap) {
            Bitmap bmp = (Bitmap)value;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            cv.put(columnName, byteArray);
        }  else if(value instanceof Uri) {
            cv.put(columnName, value.toString());
        } else {
            throw new DatabaseException("Datatype "
                    + value.getClass().getName() + " is not a valid "
                    + "datatype");
        }
    }
}
