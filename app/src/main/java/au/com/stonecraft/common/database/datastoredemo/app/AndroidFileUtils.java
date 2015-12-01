package au.com.stonecraft.common.database.datastoredemo.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import android.content.Context;

import au.com.stonecraft.common.database.exceptions.CannotCompleteException;

/**
 * This class extends FileUtils and contains the Android specific Utilities.
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created Jun 29, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class AndroidFileUtils extends FileUtils {
	private static final String DATABASE_DIR = "databases";

	/**
	 * This method returns a file pointing at the directory that will contain
	 * the database files.
	 * 
	 * If the directory does not exist it will be created.
	 * 
	 * @param context
	 * @return
	 */
	public static File getDatabaseDir(Context context) {
		// There is no method in context that returns the database directory
		// without
		// having a database already created. creating the _temp folder ensures
		// the
		// applications root is returned even if the structure changes in a
		// future SDK
		File appRoot = context.getDir("_temp", Context.MODE_PRIVATE)
				.getParentFile();
		File databaseDir = new File(appRoot, DATABASE_DIR);

		if (!databaseDir.exists()) {
			databaseDir.mkdir();
		}

		return databaseDir;
	}
	
	/**
	 * This method checks if the database directory is empty.
	 *
	 * @param context
	 * @return
	 */
	public static boolean isDBDirEmpty(Context context) {
		// There is no method in context that returns the database directory
		// without
		// having a database already created. creating the _temp folder ensures
		// the
		// applications root is returned even if the structure changes in a
		// future SDK
		File appRoot = context.getDir("_temp", Context.MODE_PRIVATE)
				.getParentFile();
		File databaseDir = new File(appRoot, DATABASE_DIR);
		
		if(databaseDir.isDirectory() && databaseDir.list().length >0){
			return false;
		}
		
		return true;
	}
	
	/**
	 * This method copies a static database that has been zipped in the assets directory to
	 * the applications database dir
	 *
	 */
	public static void copyDatabaseTo(Context context, int dbZipRes) throws CannotCompleteException {
		
		if(!isDBDirEmpty(context)){
			return;
		}
		
		File dbDir = AndroidFileUtils.getDatabaseDir(context);
		
		ZipInputStream zis = new ZipInputStream(context.getResources().openRawResource(dbZipRes)); 
		FileUtils.unzip(zis, dbDir, new IUnzipConditioner()
		{
			@Override
			public boolean getCondition(String fileName)
			{
				return fileName.endsWith(".db") &&
					!fileName.contains(File.separator);
			}
		});
	}
	
	/**
	 * This method copies a database in the apps directory to a passed in location
	 *
	 * @param context
	 * @param outputLocation
	 * @throws CannotCompleteException
	 */
	public static void copyDatabaseFrom(Context context, File outputLocation, String dbName)
		throws CannotCompleteException {
		try
		{
			File db = new File(getDatabaseDir(context), dbName + ".db");
			
			InputStream dbInput = new FileInputStream(db);
			
			FileUtils.downloadFile(dbInput, outputLocation, db.getName());
		}
		catch (Exception e)
		{
			throw new CannotCompleteException("Failed to copy db from apps directory", e);
		}
	}
	
	/**
	 * This method deletes the database directory and all of it's contents. If there are multiple
	 * databases created in the database directory these will be deleted as well.
	 *
	 * @param context
	 */
	public static void deleteDatabaseDir(Context context) {
		File dbDir = getDatabaseDir(context);
		if (dbDir.isDirectory()) {
	        String[] children = dbDir.list();
	        for (int i = 0; i < children.length; i++) {
	            new File(dbDir, children[i]).delete();
	        }
	        
	        dbDir.delete();
	    }
	}
	
	/**
	 * This method gets the path to a file in the assets directory.
	 * This method will only work for files that are in the root of the assets
	 * directory
	 *
	 * @param fileName
	 * @return
	 */
	public static String getAssetsFilePath(String fileName){
		return "file:///android_asset/" + fileName;
	}
}
