/**
 * 
 */
package com.stonecraft.datastore;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.Query;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class injects the data into the passed in class. It is used to inject queries that
 * contain a join. If the query does not contain a join {@link QueryObjectInjector} should
 * be used instead as it is  more efficient as it doesn't contain the overhead of processing
 * the join.
 *
 * @author mdelaney
 * @author Author: michael.delaney
 * @created May 2, 2014
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class JoinObjectInjector extends ObjectInjector{

    // This is used to track the fields in class that are used for a subset of data
    private Map<Class, List<Field>> mySubsetFields;
    //This is used to store a map of the objects by class and searched by the concatenation of
    // all subset fields foreign keys.
    private Map<Class, Map<String, InjectedValue>> myObjectsByClass;
    //This is used to monitor the fields that have been injected so that they aren't injected
    //into the same onject multiple times.
    private FieldMonitor myFieldMonitor;
    private Map<InjectedValue, Boolean> myUniqueObjects;

    public JoinObjectInjector(Query query) {
        super(query);
        mySubsetFields = new HashMap<>();
        myObjectsByClass = new HashMap<>();
        myFieldMonitor = new FieldMonitor();
        myUniqueObjects = new HashMap<>();
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
    @Override
	public <T> T[] inject(RSData data, Class<T> classOfT) throws DatabaseException {
        List<T> returnDataList = new ArrayList<>();
		
		data.moveToFirst();
		while(!data.isAfterLast()) {
            InjectedValue<T> injectedValue = getInjectedClass(data, classOfT);
            if(!myUniqueObjects.containsKey(injectedValue)) {
                myUniqueObjects.put(injectedValue, true);
                returnDataList.add(injectedValue.rowData);
            }
			data.next();
		}
		
		return returnDataList.toArray((T[]) Array.newInstance(classOfT, returnDataList.size()));
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
    private <T> InjectedValue<T>  getInjectedClass(RSData data, Class<T> classOfT) throws DatabaseException {
        return injectObject(data, classOfT, null, null);
    }

    /**
     * This method injects
     * @param data
     * @param classOfT
     * @param tableName
     * @param foreignKey
     * @param <T>
     * @return
     * @throws DatabaseException
     */
    private <T> InjectedValue<T> injectObject(RSData data, Class<T> classOfT,
            @Nullable String tableName, @Nullable String foreignKey) throws DatabaseException {
        try {
            Field[] fields = getFields(classOfT);
            InjectedValue<T> injectObject = injectSubset(data, classOfT);
            boolean subsetsInjected = true;
            if(injectObject == null) {
                subsetsInjected = false;
                injectObject = new InjectedValue();
                injectObject.rowData = classOfT.getConstructor().newInstance();
            }

            List<String> foreignKeyValues = new ArrayList<>();
            for(Field field : fields) {
                if(!myFieldMonitor.wasFieldInjectedPreviously(injectObject, field)) {
                    Annotation annotation = getAnnotation(field);
                    T rowData = injectObject.rowData;
                    //TODO for DbJoin you need to check if the field was already injected via injectSubset above
                    //or if it still needs to be injected as this is the first time the passed in class is
                    // being injected.
                    if (!subsetsInjected && annotation instanceof DbJoin) {
                        DbJoin dbJoinAnnotation = (DbJoin)getAnnotation(field);
                        field.setAccessible(true);
                        Class listType = getTypeOfList(field);
                        InjectedValue<T> subsetObject = injectObject(data, listType,
                                dbJoinAnnotation.table(), dbJoinAnnotation.foreignKey());
                        if(subsetObject.foreignKeyValue != null) {
                            foreignKeyValues.add(subsetObject.foreignKeyValue);
                            List list = new ArrayList();
                            list.add(subsetObject.rowData);
                            field.set(injectObject.rowData, list);

                            List<Field> subsetFields = mySubsetFields.get(classOfT);
                            if(subsetFields == null) {
                                subsetFields = new ArrayList<>();
                                mySubsetFields.put(classOfT, subsetFields);
                            }
                            subsetFields.add(field);
                        }

                    } else if (annotation instanceof DbTableName) {
                        field.setAccessible(true);
                        InjectedValue subsetObject = injectObject(data, field.getType(),
                                ((DbTableName) annotation).value(), null);
                        field.set(injectObject.rowData, subsetObject.rowData);

                    } else if (annotation instanceof DbColumnName) {
                        DbColumnName injectAnnotation = (DbColumnName) annotation;
                        String column = getColumnKey(tableName, injectAnnotation.value());
                        injectValue(data, rowData, field, column);
                        if (!TextUtils.isEmpty(foreignKey) && injectAnnotation.value().equals(
                                foreignKey)) {
                            Object fieldValue = field.get(injectObject.rowData);
                            if(fieldValue != null){
                                injectObject.foreignKeyValue = fieldValue.toString();
                            }

                        }

                        myFieldMonitor.setFieldInjected(injectObject, field);
                    }
                }
            }

            trackObjectByClass(classOfT, injectObject, foreignKeyValues);

            return injectObject;
        } catch (InstantiationException e) {
            throw new DatabaseException("Failed to create an instance of the class to be injected " +
                    "with the data for this query for class type \" + classOfT.getName()", e);
        } catch (IllegalAccessException e) {
            throw new DatabaseException("Failed to create an instance of the class to be injected " +
                    "with the data for this query for class type \" + classOfT.getName()", e);
        } catch (InvocationTargetException e) {
            throw new DatabaseException("Failed to create an instance of the class to be injected " +
                    "with the data for this query for class type \" + classOfT.getName()", e);
        } catch (NoSuchMethodException e) {
            throw new DatabaseException("Failed to create an instance of the class to be injected " +
                    "with the data for this query for class type " + classOfT.getName(), e);
        }


    }

    private <T> void trackObjectByClass(Class<T> classOfT,
            InjectedValue<T> injectObject, List<String> foreignKeyValues) {
        String mapForeignKey = getForeignKey(foreignKeyValues);
        if(!TextUtils.isEmpty(mapForeignKey)) {
            Map<String, InjectedValue> objectsByClass = myObjectsByClass.get(classOfT);
            if(objectsByClass == null) {
                objectsByClass = new HashMap<>();
            }
            objectsByClass.put(mapForeignKey, injectObject);
            myObjectsByClass.put(classOfT, objectsByClass);
        }
    }

    /**
     * This method gets the subset fields for the passed in root class and injects the subset data.
     *
     * This method will return the object that has had the subset data injected into it.
     *
     * If null is returned it means either the passed in rootclass hasn't got any Join fields or
     * the class has not had it's first pass to inject it's data to find the fields that are join
     * fields.
     *
     * @param data
     * @param rootClass
     * @param <T>
     * @return
     * @throws DatabaseException
     * @throws IllegalAccessException
     */
    private <T> InjectedValue<T> injectSubset(RSData data, Class<T> rootClass)
            throws DatabaseException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {

        List<String> foreignKeyValues = new ArrayList<>();
        Map<Field, InjectedValue> subsets = new HashMap<>();

        List<Field> subsetFields = mySubsetFields.get(rootClass);
        //if subsetFields is null it means either this class has no join fields or the rootclass
        //has not yet been injected with data to find the join fields.
        if(subsetFields == null) {
            return null;
        }
        for(Field field : subsetFields) {
            DbJoin dbJoinAnnotation = (DbJoin)getAnnotation(field);
            Class innerClassType = getTypeOfList(field);
            InjectedValue subsetObject = injectObject(data, innerClassType,
                    dbJoinAnnotation.table(), dbJoinAnnotation.foreignKey());
            //only the objects that can be mapped to a foreign key will be kept.
            if(subsetObject.foreignKeyValue != null) {
                foreignKeyValues.add(subsetObject.foreignKeyValue);
                subsets.put(field, subsetObject);
            }
        }

        String foreignKey = getForeignKey(foreignKeyValues);
        InjectedValue rootClassObject = myObjectsByClass.get(rootClass).get(foreignKey);
        //a new object is now required as it will be used for the next group of foreign keys
        if(rootClassObject == null) {
            rootClassObject = new InjectedValue();
            rootClassObject.rowData = rootClass.getConstructor().newInstance();
            trackObjectByClass(rootClass, rootClassObject, foreignKeyValues);
        }

        for(Field field : subsetFields) {
            List list = null;
            try {
                list = (List)field.get(rootClassObject.rowData);
                if(list == null) {
                    list = new ArrayList();
                    field.set(rootClassObject.rowData, list);
                }
            } catch (IllegalAccessException e) {
                throw new DatabaseException("Could not access the field " + field.getName() +
                        " in class " + rootClass.getName() + " while trying to inject data for " +
                        "a join query");
            }
            InjectedValue subset = subsets.get(field);
            if(subset != null) {
                list.add(subset.rowData);
            }
        }

        return rootClassObject;
    }

    /**
     * This method sorts the passed in list into alphabe
     * @param foreignKeyValues
     * @return
     */
    private String getForeignKey(List<String> foreignKeyValues) {
        if(foreignKeyValues == null) {
            String stopHere = "";
        }
        Collections.sort(foreignKeyValues);
        StringBuilder keyBuilder = new StringBuilder();
        for(String foreignKeyValue : foreignKeyValues) {
            keyBuilder.append(foreignKeyValue);
        }

        return keyBuilder.toString();
    }

    /**
     * This class monitors the fields that have had a fields data injected.
     * This will handle each object individually.
     */
    private static class FieldMonitor extends HashMap<InjectedValue, Map<Field, Boolean>>{

        /**
         * This method returns whether this passed in object and it's field was perviously injected.
         * @param object
         * @param field
         * @return
         */
        public boolean wasFieldInjectedPreviously(InjectedValue object, Field field) {
            Map<Field, Boolean> fieldsMap = get(object);
            if(fieldsMap == null || !fieldsMap.containsKey(field)) {
                return false;
            }
            return fieldsMap.get(field);
        }

        /**
         * This method sets the passed in field on the passed in object as having it's field
         * injected.
         *
         * @param object
         * @param field
         */
        public void setFieldInjected(InjectedValue object, Field field) {
            if(containsKey(object)) {
                get(object).put(field, true);
                return;
            }

            Map<Field, Boolean> monitorMap = new HashMap<>();
            monitorMap.put(field, true);
            put(object, monitorMap);
        }
    }

    private static class InjectedValue <T> {
        T rowData;
        String foreignKeyValue;
    }
}
