/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  
package com.recomdata.transmart.data.export.util;

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * @author SMunikuntla
 */
public class FileWriterUtil {
	
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File outputFile;
    private CSVWriter writer;

    // Maximum loop count when creating temp directories
    private static final int TEMP_DIR_ATTEMPTS = 10000;

    /**
     * Atomically creates a new directory somewhere beneath the system's
     * temporary directory (as defined by the {@code java.io.tmpdir} system
     * property), and returns its name.
     * <p>
     * <p>
     * Use this method instead of {@link File#createTempFile(String, String)}
     * when you wish to create a directory, not a regular file. A common pitfall
     * is to call {@code createTempFile}, delete the file and create a directory
     * in its place, but this leads a race condition which can be exploited to
     * create security vulnerabilities, especially when executable files are to
     * be written into the directory.
     * <p>
     * <p>
     * This method assumes that the temporary volume is writable, has free
     * inodes and free blocks, and that it will not be called thousands of times
     * per second.
     * 
     * @return the newly-created directory
     * @throws IllegalStateException if the directory could not be created
     */
    public File createDir(File baseDir, String name) {

        if (null == baseDir) {
            baseDir = new File(System.getProperty("java.io.tmpdir"));
        }

        for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
            File tempDir = new File(baseDir, name);
            if (tempDir.mkdir()) {
                return tempDir;
            }
            if (tempDir.exists()) {
                return tempDir;
            }
        }

        throw new IllegalStateException("Failed to create directory " + name + " within " + TEMP_DIR_ATTEMPTS);
    }

    public FileWriterUtil() {
        //Default constructor
    }
	
    public FileWriterUtil(File studyDir, String fileName, String jobId, String dataTypeName,
                          String dataTypeFolder, char separator) throws IOException {

        File dataTypeNameDir = StringUtils.isNotEmpty(dataTypeName) && null != studyDir ?
            createDir(studyDir, dataTypeName) : null;
        File dataTypeFolderDir = StringUtils.isNotEmpty(dataTypeFolder) && null != dataTypeNameDir ?
            createDir(dataTypeNameDir, dataTypeFolder) : null;

        if (null != studyDir) {
            if (null == dataTypeNameDir) {
                outputFile = new File(studyDir, fileName);
            }
            else {
                outputFile = new File(null == dataTypeFolderDir ? dataTypeNameDir : dataTypeFolderDir, fileName);
            }
        }
        writer = new CSVWriter(new BufferedWriter(new FileWriter(outputFile), 1024 * 64000), separator);
    }

    public FileWriterUtil(String fileName, String jobId, String dataTypeName,
                          String dataTypeFolder, char separator) throws IOException {
        File exportJobsDir = createDir(null, "jobs");
        File jobDir = StringUtils.isNotEmpty(jobId) ? createDir(exportJobsDir, jobId) : null;
        File dataTypeNameDir = StringUtils.isNotEmpty(dataTypeName) && null != jobDir ?
            createDir(jobDir, dataTypeName) : null;
        File dataTypeFolderDir = StringUtils.isNotEmpty(dataTypeFolder) && null != dataTypeNameDir ?
            createDir(dataTypeNameDir, dataTypeFolder) : null;

        if (null != fileName && !fileName.equalsIgnoreCase("")) {
            outputFile = null != jobDir && null != dataTypeNameDir ?
                new File(null == dataTypeFolderDir ? dataTypeNameDir : dataTypeFolderDir, fileName) : null;
	
            writer = new CSVWriter(new BufferedWriter(new FileWriter(outputFile), 1024 * 64000), separator);
        }
    }

    @SuppressWarnings("unused")
    private boolean validate(ResultSet resultSet, String[] headerValues) {
        boolean valid = true;
        try {
            ResultSetMetaData rsmd = resultSet.getMetaData();
            if (rsmd.getColumnCount() <= 0) {
                valid = false;
                logger.error(null != outputFile ? outputFile.getAbsolutePath() : " :: Empty resultSet");
            }

            if (null == outputFile) {
                valid = false;
                logger.error("Invalid outputFile");
            }
        }
        catch (SQLException e) {
            valid = false;
            logger.error(null != outputFile ? outputFile.getAbsolutePath() : " :: Empty resultSet");
        }

        return valid;
    }
	
    /**
     * @param lineValues a string array with each separator element as a separate entry.
     */
    public void writeLine(String[] lineValues) {
        writer.writeNext(lineValues);
    }
	
    /**
     * Closes the writer
     */
    public void finishWriting() {
        try {
            if (null != writer) {
                writer.flush();
                writer.close();
            }
        }
        catch (IOException e) {
            logger.error("Error closing file-writer");
        }
    }
	
    public String getClobAsString(Clob clob) {
        String strVal = "";
        Reader reader = null;
        try {
            if (null != clob) {
                long clobLength = clob.length();
                reader = clob.getCharacterStream();

                if (clobLength > 0 && null != reader) {
                    //Here length of String is being rounded to 5000 * n, this is because the buffer size is 5000
                    //Sometimes the cloblength is less than 5000 * n
                    char[] buffer = new char[(int) clobLength];
                    @SuppressWarnings("unused")
                        int count = 0;
                    StringBuilder strBuf = new StringBuilder();
                    while ((count = reader.read(buffer)) > 0) {
                        strBuf.append(buffer);
                    }
                    strVal = strBuf.toString();
                }
            }
        }
        catch (IOException e) {
            logger.info(e.getMessage());
        }
        catch (SQLException e2) {
            logger.info("SQLException :: {}", e2.getMessage());
        }
        finally {
            try {
                if (null != reader) {
                    reader.close();
                }

                super.finalize();
            }
            catch (IOException e) {
                logger.info("Error closing Reader in getClobAsString");
            }
            catch (Throwable e) {
                logger.info("Error during super.finalize()");
            }
        }
        return strVal;
    }
	
    public void writeFile(String url, File outputURLFile) {
		
        try { 
            new FileOutputStream(outputURLFile).getChannel().transferFrom(
                Channels.newChannel(new URL(url).openStream()), 0, 1 << 24);
        }
        catch (MalformedURLException e) {
            logger.info("Bad URL: {}", url);
        }
        catch (IOException e) {
            logger.info("IO Error:{}", e.getMessage());
        }
    }
}
