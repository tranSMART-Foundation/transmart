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

package com.rdc.snp.haploview;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
    
public class HMPFormat {

    List<String> HMPbyChromosome(Statement stmt, String chr, String[] header)
        throws SQLException{
        List<String> line = new ArrayList<>();
        String query = "select * from T03_HMP where chrom='" + chr + "'";
        ResultSet rs = stmt.executeQuery(query);        

        while (rs.next()){ 
            StringBuilder SNP_per_line = new StringBuilder();
            for (String aHeader : header) {
                SNP_per_line.append(rs.getString(aHeader)).append(" ");
            }
            line.add(SNP_per_line.toString());
        }
        return line;
    }

    List<String> HMPbyGene(Statement stmt, String gene, String[] header)
        throws SQLException{
        List<String> line = new ArrayList<>();

        String query = "select * from T03_HMP where SNP_ID in " +
            " (select SNP from SNP_Mapping where gene='" + gene + "')";
        ResultSet rs = stmt.executeQuery(query);        

        while (rs.next()){ 
            StringBuilder SNP_per_line = new StringBuilder();
            for (String aHeader : header) {
                SNP_per_line.append(rs.getString(aHeader)).append(" ");
            }

            line.add(SNP_per_line.toString());
        }
        return line;
    }

    String printHMPHeader(String [] header){
        StringBuilder hdr = new StringBuilder();
        int size = header.length;

        for(int i=0; i<size-1; i++){
            if (i < 10) {
                hdr.append(header[i]).append(" ");
            }    
            else {
                hdr.append("NA010").append(i).append(" ");
            }
        }
        return hdr + "NA010" + size;
    }

    String [] setHMPHeader(Statement stmt) throws SQLException{         
        HMPFormat hmp = new HMPFormat();
        int size = hmp.getSubjectCount(stmt);

        String[] HMPHeader = new String[size + 10];
        HMPHeader[0] = "rs#";   
        HMPHeader[1] = "alleles";
        HMPHeader[2] = "chrom";
        HMPHeader[3] = "pos";
        HMPHeader[4] = "strand";
        HMPHeader[5] = "assembly#";
        HMPHeader[6] = "center";
        HMPHeader[7] = "protLSID";
        HMPHeader[8] = "assayLSID";
        HMPHeader[9] = "panelLSID";

        int index = 10;

        // set column header for each subject
        String query = "select column_name from user_tab_columns " +
            "where table_name='T03_HMP' and column_name like 'N%'"; 

        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()){
            String cname = rs.getString(1);
            if(!cname.equals("SNP_ID")){   
                HMPHeader[index] = cname;
                index++;    
            }  
        }      
        return HMPHeader;
    }

    int getSubjectCount(Statement stmt) throws SQLException{
        int total = 0;
        String query = "select count(*) from user_tab_columns " +
            "where table_name='T03_HMP' and column_name like 'N%'"; 

        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()){
            total = rs.getInt(1);
        }

        return total;
    }
}
