package fr.sanofi.fcl4transmart.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
	public static void copyFile(File file1, File file2) throws IOException{
		InputStream inStream = new FileInputStream(file1);
		OutputStream outStream = new FileOutputStream(file2);
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inStream.read(buffer)) > 0){
				outStream.write(buffer, 0, length);
		}
 		inStream.close();
		outStream.close();
	}
}
