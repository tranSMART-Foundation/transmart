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

package com.recomdata.etl.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class MssqlConnectImpl implements DBConnect {

    // Informs the driver to use server a side-cursor,
    // which permits more than one active statement
    // on a connection.
    private static final String selectMethod = "cursor";
    private static final String url = "jdbc:jtds:sqlserver://";
    private static final String portNumber = "1433";

    private Connection con;
    private String serverName= "localhost";
    private String databaseName= "Billing";
    private String userName = "sa";
    private String password = "sa";

    public static MssqlConnectImpl createStrangeLoveConnect(){
        return new MssqlConnectImpl("StrangeLove", "CentClinRD1","sa","BioMkr101") ;
    }

    public static MssqlConnectImpl createLocalConnect(){
    	return new MssqlConnectImpl();
    }

    public MssqlConnectImpl(String serverName, String databaseName, String userName, String password) {
        super();
        this.databaseName = databaseName;
        this.password = password;
        this.serverName = serverName;
        this.userName = userName;
    }

    public MssqlConnectImpl() {
    }

    private String getConnectionUrl(){
        return url+serverName+":"+portNumber+";databaseName="+databaseName+";selectMethod="+selectMethod+";";
    }

    public Connection getConnection() {
        try{
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            con = DriverManager.getConnection(getConnectionUrl(), userName, password);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error Trace in getConnection() : " + e.getMessage());
        }
        return con;
    }

    public void displayDbProperties(){
        try{
            con = getConnection();
            if(con!=null){
                DatabaseMetaData dm = con.getMetaData();
                System.out.println("Driver Information");
                System.out.println("\tDriver Name: "+ dm.getDriverName());
                System.out.println("\tDriver Version: "+ dm.getDriverVersion ());
                System.out.println("\nDatabase Information ");
                System.out.println("\tDatabase Name: "+ dm.getDatabaseProductName());
                System.out.println("\tDatabase Version: "+ dm.getDatabaseProductVersion());
                System.out.println("Avalilable Catalogs ");
                ResultSet rs = dm.getCatalogs();
                while(rs.next()){
                    System.out.println("\tcatalog: "+ rs.getString(1));
                }
                rs.close();
                closeConnection();
            }
            else {
                System.out.println("Error: No active Connection");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        try{
            if (con != null) {
                con.close();
            }
            con=null;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        MssqlConnectImpl myDbTest = new MssqlConnectImpl();
        myDbTest.displayDbProperties();
    }
}
