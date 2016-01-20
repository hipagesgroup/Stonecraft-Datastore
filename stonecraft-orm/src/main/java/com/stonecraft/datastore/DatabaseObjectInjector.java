/**
 * 
 */
package com.stonecraft.datastore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.stonecraft.datastore.exceptions.DatabaseException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
public class DatabaseObjectInjector {

    private boolean myIsJoinedQuery;
    private Map<String, Field[]> myClassFields;
    private Map<Field, Annotation> myAnnotations;

    public DatabaseObjectInjector(QueryBase query) {
        myIsJoinedQuery = query.getJoins().isEmpty();
        myClassFields = new HashMap<>();
        myAnnotations = new HashMap<>();
    }

    /**
	 * This method returns a list of injected classes that contains the data from the passed in
	 * data.
	 *
	 * @param data
	 * @param classOfT
	 * @return
	 * @throws DatabaseException
	 */
	public <T> T[] inject(RSData data, Class<T> classOfT) throws DatabaseException{
		final T[] returnClasses = (T[]) Array.newInstance(classOfT, data.getCount());
		
		data.moveToFirst();
		int count = 0;
		while(!data.isAfterLast()) {
			T injectedRow = null;
            injectedRow = getInjectedClass(data, classOfT);

			if(injectedRow != null) {
				returnClasses[count] = injectedRow;
			}
			data.next();
			count++;
		}
		
		return returnClasses;
	}

    /**
     *  This method is used
     * @param data
     * @param classOfT
     * @param tableName
     * @param <T>
     * @return
     * @throws DatabaseException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
	private <T> T getInjectedClass(RSData data, Class<T> classOfT, @Nullable String tableName)
            throws DatabaseException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        try {
            Field[] fields = null;
            if(myClassFields.containsKey(classOfT.getName())) {
                fields = myClassFields.get(classOfT.getName());
            } else {
                fields = classOfT.getDeclaredFields();
                myClassFields.put(classOfT.getName(), fields);
            }

            T rowClass = classOfT.getConstructor().newInstance();

            for(Field field : fields) {
                Annotation annotation = getAnnotation(field);
                if(annotation instanceof DbTableName){
                    field.setAccessible(true);
                    field.set(rowClass, getInjectedClass(data, field.getType(),
                            ((DbTableName) annotation).value()));

                } else if(annotation instanceof DbColumnName){
                    DbColumnName InjectAnnotation = (DbColumnName)annotation;
                    String column = getColumnKey(tableName, InjectAnnotation.value());
                    injectValue(data, rowClass, field, column);
                }
            }

            return rowClass;
        } catch (Throwable e) {
            throw new DatabaseException("Failed to create an instance of the class to be injected " +
                    "with the data for this query", e);
        }
	}

    private Annotation getAnnotation(Field field) {
        if(myAnnotations.containsKey(field)) {
            return myAnnotations.get(field);
        }

        Annotation annotation = field.getAnnotation(DbTableName.class);
        if(annotation != null) {
            myAnnotations.put(field, annotation);
            return annotation;
        }

        annotation = field.getAnnotation(DbColumnName.class);
        //it's ok to add a null to myAnnotations so that we don't try to get it second time around
        myAnnotations.put(field, annotation);

        return annotation;
    }

	/**
	 * This method creates an instance of the passed in class and populates it with the data
	 * from the passed in {@link RSData} object. The data will come from the row that the pass in
	 * data is currently at. This method does not change the data objects position 
	 *
	 * @param data
	 * @param classOfT
	 * @return
	 * @throws DatabaseException
	 */
	private <T> T getInjectedClass(RSData data, Class<T> classOfT) throws DatabaseException {
		try {
			return getInjectedClass(data, classOfT, null);
		} catch (Throwable e) {
			throw new DatabaseException("Failed to create an instance of the class to be injected " +
				"with the data for this query", e);
		}
	}

    private String getColumnKey(String table, String column) {
        String columnKey = null;
        if(column.contains(DatabaseUtils.getTableColumnSeparator())) {
            columnKey = DatabaseUtils.normaliseTableColumnAsName(column);
        } else if(myIsJoinedQuery) {
            columnKey = column;
        } else {
            //joins have had their column reference set as 'tablenameColumnname'
            columnKey = DatabaseUtils.getDatabaseAsName(table, column);
        }

        return columnKey;
    }

    private <T> void injectValue(RSData data, T rowClass, Field field,
            String column) throws IllegalAccessException, DatabaseException {
        field.setAccessible(true);
        Class fieldType = field.getType();

        if(data.hasColumn(column)) {
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
}
