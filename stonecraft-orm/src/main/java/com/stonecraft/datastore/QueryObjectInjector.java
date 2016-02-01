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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class QueryObjectInjector extends ObjectInjector {

    private Map<String, InjectedValue> myJoinData;
    private Map<Object, Object> myUniqueData;

    public QueryObjectInjector(Query query) {
        super(query);
        myJoinData = new HashMap<>();
        myUniqueData = new HashMap<>();
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
        List<T> returnDataList = new ArrayList<>();
		
		data.moveToFirst();
		while(!data.isAfterLast()) {
			T injectedRow = null;

            InjectedValue<T> injectedValue =  getInjectedClass(data, classOfT);
            injectedRow = injectedValue.rowData;
            if(!myUniqueData.containsKey(injectedRow)) {
                myUniqueData.put(injectedRow, injectedRow);
                returnDataList.add(injectedRow);
            }

			data.next();
		}
		
		return returnDataList.toArray((T[]) Array.newInstance(classOfT, returnDataList.size()));
	}

    /**
     *  This method is used
     * @param data
     * @param classOfT
     * @param tableName
     * @param <T>
     * @return
     * @throws DatabaseException
     */
	private <T> InjectedValue<T> getInjectedClass(RSData data, Class<T> classOfT,
            @Nullable String tableName, @Nullable String foreignKey)
            throws DatabaseException {
        try {
            Field[] fields = getFields(classOfT);

            InjectedValue<T> injectedValue = new InjectedValue();
            injectedValue.rowData = classOfT.getConstructor().newInstance();

            for(Field field : fields) {
                Annotation annotation = getAnnotation(field);
                if(injectedValue.rowKey != null && myJoinData.containsKey(injectedValue.rowKey.toString())) {
                    injectedValue = myJoinData.get(injectedValue.rowKey);
                }

                if (annotation instanceof DbJoin) {
                    field.setAccessible(true);
                    injectSubset(data, classOfT, injectedValue, injectedValue, field,
                            (DbJoin) annotation);

                } else if (annotation instanceof DbTableName) {
                    field.setAccessible(true);
                    field.set(injectedValue.rowData, getInjectedClass(data, field.getType(),
                            ((DbTableName) annotation).value(), null));

                } else if (annotation instanceof DbColumnName) {
                    DbColumnName injectAnnotation = (DbColumnName) annotation;
                    String column = getColumnKey(tableName, injectAnnotation.value());
                    injectValue(data, injectedValue.rowData, field, column);
                    if (!TextUtils.isEmpty(foreignKey) && injectAnnotation.value().equals(
                            foreignKey)) {
                        injectedValue.rowKey = field.get(injectedValue.rowData);
                    }
                }
            }

            return injectedValue;
        } catch (Throwable e) {
            throw new DatabaseException("Failed to create an instance of the class to be injected " +
                    "with the data for this query", e);
        }
	}

    private <T> void injectSubset(RSData data, Class<T> classOfT, InjectedValue<T> rowClass,
            InjectedValue<T> injectedValue, Field field,
            DbJoin annotation) throws DatabaseException {

        Class innerClassType = getTypeOfList(field);
        InjectedValue joinRow =  getInjectedClass(data, innerClassType,
                annotation.table(), annotation.foreignKey());
        try {
            if(joinRow.rowKey == null) {
                throw new DatabaseException("There is no field in the enclosing class that " +
                        "matches the forign key assigned to the DbJoin annotation");
            }

            InjectedValue joinMapItem;
            if(myJoinData.containsKey(joinRow.rowKey.toString())) {
                joinMapItem = myJoinData.get(joinRow.rowKey.toString());
            } else {
                joinMapItem = rowClass;
                myJoinData.put(joinRow.rowKey.toString(), rowClass);
            }

            List list = (List)field.get(joinMapItem.rowData);
            if(list == null) {
                list = new ArrayList();
                field.set(joinMapItem.rowData, list);
            }
            list.add(joinRow.rowData);
        } catch (ClassCastException e) {
            throw new DatabaseException("The field " + field.getName() + " in " +
                    classOfT.getSimpleName() + " needs to be of type List");
        } catch (Throwable e) {
            throw new DatabaseException(e.getMessage(), e);
        }
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
        return getInjectedClass(data, classOfT, null, null);
	}

    private static class InjectedValue <T> {
        T rowData;
        Object rowKey;
    }

    private static class InjectionCache <T> {
        Class classType;
        Map<String, String> foreignkeysForTable = new HashMap<>();
        T injectedObject;
    }

    private static class InjectionCacheFilter<T> {
        private Map<Class, Map<String, InjectionCache>> myClassFilterMap = new HashMap<>();
        private Map<InjectionCache, Field> myInjectedField = new HashMap<>();
    }
}
