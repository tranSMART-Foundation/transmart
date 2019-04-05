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

package com.recomdata.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVGenerator {
	
    private static final Logger logger = LoggerFactory.getLogger(CSVGenerator.class);

    public static byte[] generateCSV(List<String> headers, List<List> values) {
        FileWriter csvWriter=null;
        StringBuilder export = new StringBuilder();
        try{
            csvWriter=new FileWriter("export.csv");
			
            StringBuilder columnNames = new StringBuilder();
            for (int i = 0; i < headers.size(); i++) {
                if (i > 0) {
                    columnNames.append(',');
                }
                columnNames.append(headers.get(i));
            }

            export.append(columnNames).append('\n');

            for(int i=0;i<values.size();i++){
                StringBuilder rowValues = new StringBuilder();
                for (int j = 0; j < values.get(i).size(); j++) {
                    if (j > 0) {
                        rowValues.append(',');
                    }
                    rowValues.append('"');
                    rowValues.append(values.get(i).get(j).toString().replace("\"", "\"\""));
                    rowValues.append('"');
                }
                export.append(rowValues).append('\n');
            }
        }
        catch (IOException | RuntimeException e) {
            logger.error(e.getMessage(), e);
        }
        finally {
            try{
                csvWriter.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        return export.toString().getBytes();
    }
}
