package au.com.fairfaxdigital.common.database.exceptions;

/**
 * This exception is a standard exception. It is used when a process could not
 * be completed because of an exception circumstance.
 * 
 * @author Michael Delaney
 * @author $Author: michael.delaney $
 * @created 16/03/2012
 * @date $Date: 16/03/2012 01:50:39 $
 * @version $Revision: 1.0 $
 */
public class CannotCompleteException extends Exception {
	private static final long serialVersionUID = 1L;
	private String myDisplayText;

	public CannotCompleteException(String message) {
		super(message);
	}

	public CannotCompleteException(String message, Throwable throwable) {
		super(message, throwable);
	}

	/**
	 * This method returns user friendly text
	 * 
	 * @return the displayString
	 */
	public String getDisplayText() {
		return myDisplayText;
	}

	/**
	 * @param displayString
	 *            the displayString to set
	 */
	public void setDisplayText(String displayString) {
		myDisplayText = displayString;
	}
	
	public String toString() {
		return getMessage() + " [" + getCause() + "]";
	}
}
