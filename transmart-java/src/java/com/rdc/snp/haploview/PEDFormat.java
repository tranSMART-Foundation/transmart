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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
    
public class PEDFormat {
	
    private static final String tblname = "haploview_data";

    /**
     * @param genes    list of genes in the same chromosome
     * @param pids		list of i2b2 patient ids
     * @param file 		a file handle to indicate where the file should be written,
     *                  if the file = null, then the PED file will be written to a default 
     *                  place
     * @throws SQLException 2009-06-21: change return "void" to "boolean"
     */    
    public boolean createPEDFile(String genes, String pids, String file, Connection conn) throws SQLException {

    	Statement stmt;
    	try{
            stmt = conn.createStatement();
        }
        catch (Exception e) {
            conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@machineName:port:sid", "userid", "password");
            stmt = conn.createStatement();
    	}

        Map<String, String> map = pedOutput(stmt, genes, pids);

    	File pedFile;
    	File infoFile;

    	if(file.equals("")){
            pedFile = new File("C:/transmart.ped");
            infoFile = new File("C:/transmart.info");
    	}
        else {
            pedFile = new File(file + ".ped");
            infoFile = new File(file + ".info");
        }
        writeToFile(map, pedFile);

        return pedInfoOutput(stmt, infoFile, genes);
    }  

    /**
     * @param pids 	a group of i2b2 patient ids in csv format
     */
    private Map<String, String> pedOutput(Statement stmt, String listGenes, String pids) throws SQLException {
        Map<String, String> map = new HashMap<>();

        String[] ids = pids.split(",");
        String[] genes = listGenes.split(",");

        for (String id : ids) {
            String pid = removeSpaces(id);
            map.putAll(getCommonData(stmt, pid));

            for (String gene : genes) {
                merge(getSNPDataByGeneAndSubject(stmt, removeSpaces(gene), pid), map);
            }
        }

        return map;
    }

    /**
     * @throws SQLException 2009-06-22: change return "viod" to "boolean"
     */
    private boolean pedInfoOutput(Statement stmt, File file, String listGenes) throws SQLException {
        List<String> al = new ArrayList<>();

        // if info.length >10, set isOk = true, so Haploview will be displayed,
        // otherwise, don't display it
        boolean  isOk = false;

        String[] genes = listGenes.split(",");
        for (String gene : genes) {
            Collections.addAll(al, pedInfoByGeneNew(stmt, removeSpaces(gene)));
        } 

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));	
            if(al.size() >= 10){
                isOk = true;
            }
            for (String anAl : al) {
                out.write(anAl + "\n");
            } 			 
            out.close();
        }   	  
        catch (IOException ignored) {}

        return isOk;
    }    

    /**
     * @param gene    gene name 
     */
    private String[] pedInfoByGeneNew(Statement stmt, String gene) throws SQLException {

        String query = "select snp_data, snp_data from haploview_data " +
            " where i2b2_id=0 and jnj_id='2' and gene='" + gene + "'";

        String [] snps = null;    
        String [] positions = null;
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()){  
            snps = rs.getString(1).split(",");
            positions = rs.getString(2).split(",");
        }

        String[] snpInfo = new String[snps.length];
        if(snps.length != positions.length){
            snpInfo[0] =  "Mismatching between SNP names and their position";
        }
        else{ 
            for(int j=0; j < snps.length; j++){
                snpInfo[j] = removeSpaces(snps[j]) + "      " + removeSpaces(positions[j]);
            }
        }
        return snpInfo;
    }    

    private Map<String, String> getCommonData(Statement stmt, String pid) throws SQLException {
        Map<String, String> ht = new HashMap<>();

        String query = "select distinct i2b2_id, jnj_id, father_id, mother_id," +
            "       sex, affection_status" +
            " from " + tblname +
            " where i2b2_id!=0 " ;

        if(!pid.equals("")){
            query += " and i2b2_id=" + pid;
        }
        query += " order by 1";

        ResultSet rs = stmt.executeQuery(query);        
        while (rs.next()){
            String val = rs.getString(1);
            val += "  " + rs.getString(2);
            val += "  " + rs.getString(3);
            val += "  " + rs.getString(4);
            val += "  " + rs.getString(5);
            val += "  " + rs.getString(6);
            ht.put(rs.getString(1), val); 
        }   
        return ht;
    }   

    /**
     * @param gene    a gene name
     * @param pid     an i2b2 patient ids
     */
    private Map<String, String> getSNPDataByGeneAndSubject(Statement stmt, String gene,
                                                           String pid) throws SQLException {
        Map<String, String> ht = new HashMap<>();

        // remove distinct from select
        String query = "select i2b2_id, snp_data" +
            " from " + tblname +
            " where i2b2_id!=0 ";

        if(!pid.equals("")){
            query += " and i2b2_id=" + pid;
        }

        if(!gene.equals("")){
            query += " and upper(gene)='" + gene.toUpperCase() + "' ";
        }

        query += " order by 1";

        ResultSet rs = stmt.executeQuery(query);        
        while (rs.next()){
            Clob cl = rs.getClob(2);
            String snpVal = cl.getSubString(1, (int)cl.length());
            ht.put(rs.getString(1), snpVal); 
        }         	   	
        return ht;
    }  

    private void merge(Map<String, String> from, Map<String, String> to) {
        for (Map.Entry<String, String> entry : from.entrySet()) {
            String key = entry.getKey();
            to.put(key, to.get(key) + "  " + entry.getValue());
        }       
    }   

    private void writeToFile(Map<String, String> map, File file) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (String value : map.values()) {
                out.write(value + "\n");
            }
            out.close();
        }
        catch (IOException ignored) {}
    }

    private String removeSpaces(String s) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(s, " ", false);
        while (st.hasMoreElements()) {
            sb.append(st.nextElement());
        }
        return sb.toString();
    }
}
