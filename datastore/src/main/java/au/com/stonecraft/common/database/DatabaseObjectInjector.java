/**
 * 
 */
package au.com.stonecraft.common.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

import au.com.stonecraft.common.database.exceptions.DatabaseException;

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
			T injectedRow = getInjectedClass(data, classOfT);
			if(injectedRow != null) {
				returnClasses[count] = injectedRow;
			}
			data.next();
			count++;
		}
		
		return returnClasses;
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
	private <T> T getInjectedClass(RSData data, Class<T> classOfT) throws DatabaseException{
		try {
			Field[] fields = classOfT.getDeclaredFields();
			T rowClass = classOfT.getConstructor().newInstance();
			
			for(Field field : fields) {
				Annotation annotation = field.getAnnotation(DbColumnName.class);
				if(annotation instanceof DbColumnName){
					field.setAccessible(true);
					DbColumnName InjectAnnotation = (DbColumnName) annotation;
					String column = InjectAnnotation.value();
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
			
			return rowClass;
		} catch (Throwable e) {
			throw new DatabaseException("Failed to create an instance of the class to be injected " +
				"with the data for this query", e);
		}
	}
}
