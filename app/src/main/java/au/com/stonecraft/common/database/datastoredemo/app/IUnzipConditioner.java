/**
 * 
 */
package au.com.stonecraft.common.database.datastoredemo.app;

/**
 * This class is used to get the condition of which files are to be saved in a
 * zip file
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created Jun 28, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public interface IUnzipConditioner {
	public boolean getCondition(String fileName);
}
