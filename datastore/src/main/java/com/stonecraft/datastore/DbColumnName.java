/**
 * 
 */
package com.stonecraft.datastore;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This class
 *
 * @author mdelaney
 * @author Author: michael.delaney
 * @created May 2, 2014
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) 
public @interface DbColumnName {
		String value();
}
