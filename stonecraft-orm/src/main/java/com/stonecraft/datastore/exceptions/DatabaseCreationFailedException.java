package com.stonecraft.datastore.exceptions;

/**
 * This class is the exception that is thrown when an exception occurs in
 * datastore. All exceptions will thrown will be wrapped in an instance of this
 * class
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class DatabaseCreationFailedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DatabaseCreationFailedException(String message) {
		super(message);
	}

	public DatabaseCreationFailedException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public String toString() {
		String message = getMessage();

		Throwable cause = getCause();
		if (cause != null) {
			message += " [" + cause.getMessage();
		}
		return message;
	}
}
