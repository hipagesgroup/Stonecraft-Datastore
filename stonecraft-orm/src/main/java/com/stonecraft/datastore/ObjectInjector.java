/**
 * 
 */
package com.stonecraft.datastore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.Query;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class injects the data into the passed in class
 *
 * @author mdelaney
 * @author Author: michael.delaney
 * @created May 2, 2014
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public abstract class ObjectInjector {

    private Query myQuery;
    private Map<Class, Field[]> myClassFields;
    private Map<Field, Annotation> myAnnotations;
    private Map<Field, Class> myListTypeClass;

    public ObjectInjector(Query query) {
        myQuery = query;
        myClassFields = new HashMap<>();
        myAnnotations = new HashMap<>();
        myListTypeClass = new HashMap<>();
    }

    protected Class getTypeOfList(Field field) {
        if(myListTypeClass.containsKey(field)) {
            return myListTypeClass.get(field);
        }

        ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
        Class klass = (Class)stringListType.getActualTypeArguments()[0];
        myListTypeClass.put(field, klass);

        return klass;
    }

    /**
     * This method attempts to get the annotation from a field in memory so that the annotation
     * doesn't have to continually be read from the field using reflection.
     *
     * This is for performance improvements.
     * @param field
     * @return
     */
    protected Annotation getAnnotation(Field field) {
        if(myAnnotations.containsKey(field)) {
            return myAnnotations.get(field);
        }

        Annotation[] annotations = field.getAnnotations();
        for(Annotation annotation : annotations){
            if(annotation instanceof DbJoin ||
                    annotation instanceof DbTableName ||
                    annotation instanceof DbColumnName) {
                myAnnotations.put(field, annotation);
                return annotation;
            }
        }

        return null;
    }

    /**
     * This method returns the fields found in the passed in class.
     *
     * This method should be used to get the fields as it ensures the most efficient
     * way to return the fields as it will cache the fields for later use.
     * @param klass
     * @return
     */
    protected Field[] getFields(Class klass) {
        Field[] fields = null;
        if(myClassFields.containsKey(klass.getName())) {
            fields = myClassFields.get(klass.getName());
        } else {
            fields = klass.getDeclaredFields();
            myClassFields.put(klass, fields);
        }

        return fields;
    }

    protected String getColumnKey(String table, String column) {
        String columnKey = null;
        if(column.contains(DatabaseUtils.getTableColumnSeparator())) {
            columnKey = DatabaseUtils.normaliseTableColumnAsName(column);
        } else if(!myQuery.getJoins().isEmpty() && TextUtils.isEmpty(table)) {
            columnKey = DatabaseUtils.getDatabaseAsName(myQuery.getTable(), column);
        }
        else if(!TextUtils.isEmpty(table)) {
            columnKey = DatabaseUtils.getDatabaseAsName(table, column);
        }else {
            columnKey = column;
        }

        return columnKey;
    }

    protected <T> void injectValue(RSData data, T rowClass, Field field,
            String column) throws IllegalAccessException, DatabaseException {
        field.setAccessible(true);
        Class fieldType = field.getType();

        if(data.hasColumn(column) && !data.containsNull(column)) {
            if(fieldType == Integer.TYPE || fieldType == Integer.class) {
                field.set(rowClass, data.getIntValue(column));
            } else if (fieldType == Boolean.TYPE || fieldType == Boolean.class) {
                field.set(rowClass, data.getBooleanValue(column));
            } else if (fieldType == Double.TYPE || fieldType == Double.class) {
                field.set(rowClass, data.getDoubleValue(column));
            } else if (fieldType == Float.TYPE || fieldType == Float.class) {
                    field.set(rowClass, data.getFloatValue(column));
            } else if (fieldType == String.class) {
                field.set(rowClass, data.getStringValue(column));
            } else if (fieldType == Calendar.class) {
                field.set(rowClass, data.getCalendarValue(column));
            } else if (fieldType == Date.class) {
                field.set(rowClass, data.getDateValue(column));
            } else if (fieldType == Byte[].class) {
                field.set(rowClass, data.getBlobData(column));
            } else if (fieldType == Bitmap.class) {
                byte[] bmpData = data.getBlobData(column);
                Bitmap bmp = BitmapFactory.decodeByteArray(bmpData, 0, bmpData.length);
                field.set(rowClass, bmp);
            }
        }
    }

    public abstract <T> T[] inject(RSData data, Class<T> classOfT) throws DatabaseException;
}
