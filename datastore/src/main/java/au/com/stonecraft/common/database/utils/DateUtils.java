package au.com.stonecraft.common.database.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import au.com.stonecraft.common.database.exceptions.CannotCompleteException;

/**
 * This class contains utilities for working with dates. This class is a
 * utilities class and cannot be instantiated.
 * 
 * @author Michael Delaney
 * @author $Author: michael.delaney $
 * @created 16/03/2012
 * @date $Date: 16/03/2012 01:50:39 $
 * @version $Revision: 1.0 $
 */
public class DateUtils {
	/** The date format in iso. */
	public static String FORMAT_DATE_ISO = "yyyy-MM-dd HH:mm:ss";
	public static String FORMAT_FILE_FRIENDLY_DATE_ISO = "yyyy-MM-dd HHmmss";
	public static String FORMAT_CACHE_BUSTER = "yyyyMMddHHmmss";
	public static String FORMAT_DAY_NTH = "dddd";
	public static String DAY_SUFFIX_ST = "st";
	public static String DAY_SUFFIX_RD = "rd";
	public static String DAY_SUFFIX_TH = "th";
	public static String DAY_SUFFIX_ND = "nd";
	

	public static Date getDateFromString(String dateString, String format)
			throws CannotCompleteException {
		try {
			SimpleDateFormat f = new SimpleDateFormat(format);
			return f.parse(dateString);
		} catch (ParseException e) {
			throw new CannotCompleteException("The date " + dateString
					+ " with format " + format
					+ " could not be made into a date object" + " [" + e + "]");
		}
	}

	/**
	 * Takes in an ISO date string of the following format: yyyy-MM-dd HH:mm:ss
	 * 
	 * @param isoDateString
	 *            the iso date string
	 * @return the date
	 * @throws Exception
	 *             the exception
	 */
	public static Date fromISODateString(String isoDateString)
			throws CannotCompleteException {
		return getDateFromString(isoDateString, FORMAT_DATE_ISO);
	}

	/**
	 * This method returns the iso string for the passed in Date object
	 * 
	 * @param date
	 * @return
	 */
	public static String toIsoString(Date date) {
		return formatDate(date, FORMAT_DATE_ISO);
	}

	/**
	 * This method gets the iso string for the current date and time
	 * 
	 * @return
	 */
	public static String getIsoStringForNow() {
		Date date = new Date();
		return toIsoString(date);
	}

	
	@Deprecated
	public static String formatDate(Date date, String format) {
		SimpleDateFormat f = new SimpleDateFormat(format);
		return f.format(date);
	}
	
	/**
	 * This method returns the date string in the format passed in. dddd can be used in
	 * the date string in order to get the nth day (eg 1st, 2nd, 3rd, 4th, 5th etc)
	 *
	 * @param cal
	 * @param format
	 * @return
	 */
	public static String formatDate(Calendar cal, String format) {
		Date date = new Date(cal.getTimeInMillis());
		if(format.contains(FORMAT_DAY_NTH)){
			
			String startStringFormat = format.substring(0, format.indexOf(FORMAT_DAY_NTH));
			String endStringFormat = format.substring(startStringFormat.length() 
				+ FORMAT_DAY_NTH.length(), format.length());
			String dayString = cal.get(Calendar.DAY_OF_MONTH) +
				getDayOfMonthSuffix(cal.get(Calendar.DAY_OF_MONTH));
			String startFormat = formatDate(date, startStringFormat);
			String endFormat = formatDate(date, endStringFormat);
			return startFormat + dayString + endFormat;
		}
		SimpleDateFormat f = new SimpleDateFormat(format);
		return f.format(date);
	}
	
	public static String getDayOfMonthSuffix(final int n) {
	    if (n >= 11 && n <= 13) {
	        return DAY_SUFFIX_TH;
	    }
	    switch (n % 10) {
	        case 1:  return DAY_SUFFIX_ST;
	        case 2:  return DAY_SUFFIX_ND;
	        case 3:  return DAY_SUFFIX_RD;
	        default: return DAY_SUFFIX_TH;
	    }
	}
}
