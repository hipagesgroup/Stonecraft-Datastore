package au.com.fairfaxdigital.common.database.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains utilities for working with Strings. This class is a
 * utilities class and cannot be instantiated.
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class StringUtils {
	public static final String EmptyString = "";
	public static final int NO_CHAR_FOUND = -1;
	
	//common characters
	public static final String URL_ENCODED_COMMA = "%2c";
	public static final String URL_ENCODED_GREATER_THAN = "%3e";
	public static final String URL_ENCODED_LESS_THAN = "%3c";
	public static final String COMMA = ",";
	public static final String MORE_THAN = ">";
	public static final String LESS_THAN = "<";
	public static final char ESCAPE_CHAR = '\\';
	public static final char CHAR_COMMA = ',';

	/**
	 * The constructor has been made private so that you cannot make an instance
	 * of this class.
	 */
	private StringUtils() {
	}

	/**
	 * This method checks if a String is null or empty string and returns true
	 * or false otherwise.
	 * 
	 * @param val
	 * @return
	 */
	public static boolean isEmpty(String val) {
		if (val == null || val.equals(EmptyString)) {
			return true;
		}
		return false;
	}

	/**
	 * This method removes the last character of a given string.
	 * 
	 * @param val
	 * @return
	 */
	public static String removeLastChar(String val) {
		if (isEmpty(val)) {
			return val;
		}
		return val.substring(0, val.length() - 1);
	}

	/**
	 * This method checks if the suffix of a string matches the string that
	 * should be removed and returns the string minus the suffix.
	 * 
	 * @param val
	 * @param suffix
	 * @return
	 */
	public static String removeStringSuffix(String val, String suffix) {
		if (val.length() < suffix.length()) {
			return val;
		}
		String lastChar = val.substring(val.length() - suffix.length(),
				val.length());
		if (lastChar.equals(suffix)) {
			return val.substring(0, val.length() - suffix.length());
		}

		return val;
	}

	/**
	 * This method returns a delimited list based on the passed in parameters
	 * 
	 * eg. if a list is passed in contain the strings "A" "B" and "C" with a ','
	 * as the separator and '"' as the escape char the return result will be
	 * "A","B","C"
	 * 
	 * The separator can be left as an empty char if no separator and/or escape
	 * char is required
	 * 
	 * @param list
	 * @param delim
	 * @param escapeChar
	 * @return
	 */
	public static String convertListToDelimitedString(List<String> list,
			String delim, String escapeChar) {
		StringBuilder builder = new StringBuilder();
		for(String item : list)
		{
			if(StringUtils.isEmpty(escapeChar)) {
				builder.append(item + delim);
			}
			else {
				builder.append(escapeChar + item + escapeChar + delim);
			}
		}

		// remove the last seperator before returning string
		return removeStringSuffix(builder.toString(), delim);
	}
	
	public static String convertListToDelimitedString(List<String> list, String delim)
	{
		return convertListToDelimitedString(list, delim, null);
	}

	/**
	 * This method converts a delimited string into a list. If the element is
	 * enclosed with an escape char this char will be removed.
	 * 
	 * For example this string "element1","element2","element"3" with ',' being
	 * the delimiter and '"' being the escape char will return this list
	 * [element1, element2, element"3]
	 * 
	 * @param data
	 * @param delim
	 * @param escapeChar
	 * @return
	 */
	public static List<String> convertDelimitedStringToList(String data,
			char delim, char escapeChar) {
		List<String> returnList = new ArrayList<String>();
		if (data == null || isEmpty(data)) {
			return returnList;
		}

		int lastIndex = 0;
		while (true) {
			int index = data.indexOf(delim, lastIndex);
			int start = lastIndex;
			
			//this occurs if the last char is the delimiter
			if(start >= data.length()){
				break;
			}
			
			// check if first char is a escape char and remove
			if (data.charAt(start) == escapeChar) {
				start += 1;
			}

			int end = index;
			// delim not found. index will be last char in string
			if (index == -1) {
				end = data.length();
			}

			// check if the end index is a escape char and remove
			if (data.charAt(end - 1) == escapeChar) {
				end -= 1;
			}

			returnList.add(data.substring(start, end));

			// break out of loop if the last element has been found
			if (index == -1) {
				break;
			}

			lastIndex = index + 1;
		}

		return returnList;
	}

	/**
	 * This method is used to ensure there is never a null value. If a null
	 * value is passed in an Empty string will be returned.
	 * 
	 * @param val
	 * @return
	 */
	public static String getStringNotNull(String val) {
		if (val == null) {
			val = EmptyString;
		}

		return val;
	}


	/**
	 * This method formats a float into a currency string that includes the
	 * thousands separator
	 * 
	 * @param value
	 * @return
	 */
	public static String formatIntToCurrency(float value) {
		DecimalFormat format = new DecimalFormat("#,##0");
		return "$" + format.format(value);
	}

	/**
	 * This method checks the passed in string is a valid email address
	 *
	 * @param address
	 * @return
	 */
	public static boolean isValidEmail(String address) {
		if (isEmpty(address)) {
	        return false;
	    }
		
		return android.util.Patterns.EMAIL_ADDRESS.matcher(address).matches();
	}
}
