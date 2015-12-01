package au.com.stonecraft.common.database.datastoredemo.app;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;

import au.com.stonecraft.common.database.exceptions.CannotCompleteException;

/**
 * This class contains utilities for working with files. This class is a
 * utilities class and cannot be instantiated.
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class FileUtils {
    // have a max download chunk so that no outOfMemery errors occur
    // when downloading multiple files.
    private static final int MAX_DOWNLOAD_CHUNK = 1024 * 10; // 10kb

    // can't make an instance of this class
    FileUtils() {
    }

    /**
     * This method downloads a file from the given inputstream to the given save
     * directory and file name.
     * 
     * If the directory doesn't exist it will be created.
     * 
     * 
     * @param is
     * @param saveDir
     * @param fileName
     * @return
     * @throws CannotCompleteException
     */
    public static String downloadFile(InputStream is, String saveDir, String fileName) throws CannotCompleteException {
        return downloadFile(is, new File(saveDir), fileName);
    }

    /**
     * This method downloads a file from the given inputstream to the given save
     * directory and file name.
     * 
     * If the directory doesn't exist it will be created.
     * 
     * @param is
     * @param saveDir
     * @param fileName
     * @return
     * @throws CannotCompleteException
     */
    public static String downloadFile(InputStream is, File saveDir, String fileName) throws CannotCompleteException {
        OutputStream out = null;
        BufferedInputStream inStream = null;
        File outputLocation = null;
        try {
            if (!saveDir.exists()) {
                if (!saveDir.mkdirs()) {
                    throw new CannotCompleteException("Could not create directory file '" + saveDir + "'");
                }
            }

            outputLocation = new File(saveDir, fileName);
            out = new FileOutputStream(outputLocation);
            inStream = new BufferedInputStream(is);

            int actual = 0;
            byte[] baf = new byte[MAX_DOWNLOAD_CHUNK];
            while ((actual = inStream.read(baf, 0, MAX_DOWNLOAD_CHUNK)) > 0) {
                out.write(baf, 0, actual);
            }
        } catch (IOException e) {
            throw new CannotCompleteException("Could not download file " + "to " + saveDir + File.separatorChar
                    + fileName + " [" + e + "]");
        } finally {
            try {
                // clean up
                if (out != null) {
                    out.flush();
                    out.close();
                }

                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }

        return outputLocation.getAbsolutePath();
    }

    /**
     * This method unzips a file and saves only the files in the zip that meets
     * the conditions met in the IUnzipConditioner
     * 
     * @param zis
     * @param outDir
     * @param conditioner
     * @return
     * @throws CannotCompleteException
     */
    public static boolean unzip(ZipInputStream zis, File outDir, IUnzipConditioner conditioner)
            throws CannotCompleteException {
        try {
            ZipEntry ze = null;
            while ((ze = zis.getNextEntry()) != null) {
                String fileName = ze.getName();
                // ensure only plists are added to the input streams
                if (conditioner.getCondition(fileName)) {
                    int actual = 0;
                    byte[] baf = new byte[MAX_DOWNLOAD_CHUNK];
                    FileOutputStream out = new FileOutputStream(new File(outDir, fileName));
                    while ((actual = zis.read(baf, 0, MAX_DOWNLOAD_CHUNK)) > 0) {
                        out.write(baf, 0, actual);
                    }

                    out.flush();
                    out.close();
                    zis.closeEntry();
                }
            }
        } catch (IOException e) {
            throw new CannotCompleteException("Failed to unzip files", e);
        } finally {
            try {
                zis.close();
            } catch (IOException e) {
                // file is already closed. ignore
            }
        }

        return true;
    }

    /**
     * This method adds extra text to the end of a file name and retains the
     * same file type. For example, If foo is to be added to bar.xml the result
     * would be foobar.xml
     * 
     * If no file type is found on the file name the suffix will simply be added
     * to the end of the file name
     * 
     * @param fileName
     * @param suffix
     * @return
     */
    public static String appendTextToFileName(String fileName, String suffix) {
        int periodIndex = fileName.lastIndexOf('.');
        if (periodIndex < 0) {
            return fileName + suffix;
        }
        String fileType = fileName.substring(periodIndex + 1);
        return fileName.subSequence(0, periodIndex) + suffix + "." + fileType;
    }


    /**
     * Load resource file as string format
     * @param context
     * @param resource
     * @return
     * @throws IOException
     */
    public static String loadResource(Context context, int resource) throws IOException {
        InputStream is = context.getResources().openRawResource(resource);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }

        return writer.toString();
    }
}
