/**
 *
 */
package com.stonecraft.datastore;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.Query;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * This class injects the data into the passed in class
 *
 * @author mdelaney
 * @author Author: michael.delaney
 * @created May 2, 2014
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public final class QueryObjectInjector extends ObjectInjector {

    public QueryObjectInjector(Query query) {
        super(query);
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
     * @param <T>
     * @return
     * @throws DatabaseException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    private <T> T getInjectedClass(RSData data, Class<T> classOfT)
            throws DatabaseException {
        try {
            Field[] fields = getFields(classOfT);

            T rowClass = classOfT.getConstructor().newInstance();

            for(Field field : fields) {
                DbColumnName annotation = (DbColumnName)myAnnotations.get(field);
                if(annotation == null) {
                    annotation = field.getAnnotation(DbColumnName.class);
                    myAnnotations.put(field, annotation);
                }
                if(annotation != null){
                    DbColumnName InjectAnnotation = (DbColumnName)annotation;
                    String column = getColumnKey(null, InjectAnnotation.value());
                    injectValue(data, rowClass, field, column);
                }
            }

            return rowClass;
        } catch (Throwable e) {
            throw new DatabaseException("Failed to create an instance of the class to be injected " +
                    "with the data for this query", e);
        }
    }
}
