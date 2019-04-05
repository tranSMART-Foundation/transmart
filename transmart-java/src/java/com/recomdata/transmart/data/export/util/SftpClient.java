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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class SftpClient {
    private String server;
    private String username;
    private String keyFilePath;
    private String port;
    private Session session;
    private JSch jSch = new JSch();
    private Channel channel;
    private ChannelSftp channelSftp;
	
    // If the private key file is password protected, this is the password.
    @SuppressWarnings("unused")
    private String passPhrase;
	
    /**
     * @param server Server to connect to.
     * @param username User to connect as.
     * @param keyFilePath Absolute path to private key file.
     * @param port Port to connect over.
     * @param passPhrase Password for private key file (NOT USED CURRENTLY)
     */
    public SftpClient(String server, String username, String keyFilePath, String port, String passPhrase) throws Exception {
        this.server = server;
        this.username = username;
        this.keyFilePath = keyFilePath;
        this.port = port;
        this.passPhrase = passPhrase;

        try{
            initSftpClient();
        }
        catch (Exception e) {
            throw new Exception("Failed to initialize connection!",e);
        }
    }
	
    /**
     * Attempt to initialize JSch object, establish identity, session, and connection.
     */
    protected void initSftpClient() throws Exception {
        //Add our private key file with no pass to the JSch object.
        try {
            if (StringUtils.isNotEmpty(keyFilePath)) {
                jSch.addIdentity(keyFilePath, new byte[0]);
            }
        }
        catch (JSchException jse) {
            throw new Exception("Failed to add JSch identity!",jse);
        }

        //Set the session information (User, Server, port)
        try{
            if (StringUtils.isNotEmpty(port) && NumberUtils.isNumber(port)) {
                session = jSch.getSession(username, server, Integer.parseInt(port));
            }
            else {
                session = jSch.getSession(username, server);
            }
        }
        catch (JSchException jse) {
            throw new Exception("Failed to create JSch session!",jse);
        }

        //Don't think this does anything unless you are using some kind of interactive login. Voodoo programming, I'll leave it here for now.
        session.setUserInfo(new MyUserInfo());

        // set properties so we don't get unknown host key exception
        session.setConfig("StrictHostKeyChecking", "no");

        try {
            initConnection();
        }
        catch (Exception e) {
            throw new Exception("JSch session failed to initConnection!",e);
        }
    }	

    /**
     * Try to initialize our FTP connection, session and channel.
     */
    private void initConnection() throws Exception {
        try {
            session.connect();
        }
        catch (JSchException jse) {
            throw new Exception("JSch session failed to connect!",jse);
		}

        try {
            channel = session.openChannel("sftp");
        }
        catch (JSchException jse) {
            throw new Exception("JSch session failed to open a channel!",jse);
        }

        try {
            channel.connect();
            channelSftp = (ChannelSftp) channel;
        }
        catch (JSchException jse) {
            throw new Exception("JSch channel failed to connect!",jse);
        }
    }	
	
    public void changeDirectory(String ftpRemoteDirectory) throws Exception {
        channelSftp.cd(ftpRemoteDirectory);
    }
	
    /**
     * Puts a file on the ftp server over the already established connection.
     *
     * @param fileToTransfer Absolute path to the file to be transfered. 
     */
    public void putFile(File fileToTransfer) throws Exception {
        try {
            channelSftp.put(new FileInputStream(fileToTransfer), fileToTransfer.getName());
        }
        catch (FileNotFoundException fnf) {
            throw new Exception("Could not find file to upload!",fnf);
        }
        catch (SftpException sftpException) {
            throw new Exception("Could not transfer file!",sftpException);
        }
    }
	
    /**
     * Gets a file on the ftp server over the already established connection.
     *
     * @param fileToGet Absolute path to the file to be transfered.
     */
    public void getFile(String fileToGet, File outputFile) throws Exception {
        try {
            channelSftp.get(fileToGet, new FileOutputStream(outputFile));
        }
        catch (FileNotFoundException fnf) {
            throw new Exception("Could not find directory to fetch from!",fnf);
        }
        catch (SftpException sftpException) {
            throw new Exception("Could not get file!",sftpException);
        }
    }	
	
    /**
     * clean up resources
     */
    public void close()  {
        try{
            if (channelSftp != null) {
                channelSftp.exit();
            }
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
            if (jSch != null) {
                jSch.removeAllIdentity();
            }
	}
        catch (JSchException ignored) { }
    }
	
    public static class MyUserInfo implements UserInfo{

        String passwd;

        public String getPassword() {
            return passwd;
        }

        public boolean promptYesNo(String str){
            return true;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message){
            return true;
        }

        public void showMessage(String message){
        }
    }
	
    public static void main(String[] args) {
        //File outputFile = new File("./latestGet.log");
        try {
            SftpClient sftpClient = new SftpClient("factbookdev", "SvcCOPSSH", "C:/Users/smunikuntla/Downloads/SaiMunikuntla12.ppk", "22", "");
            sftpClient.putFile(new File("C:/Users/smunikuntla/Downloads/camelinaction-src.zip"));
            //sftpClient.getFile("server1.log", outputFile);
			
            File jobZipFile = null;
            File tempFile = null;

            tempFile = new File("ftp://SvcCOPSSH:@factbookdev:22/camelinaction-src.zip");
            jobZipFile = new File(tempFile.getName());

            sftpClient.getFile("camelinaction-src.zip", jobZipFile);

            sftpClient.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
