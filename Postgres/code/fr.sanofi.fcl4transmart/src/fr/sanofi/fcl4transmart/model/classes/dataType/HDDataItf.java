package fr.sanofi.fcl4transmart.model.classes.dataType;

import java.io.File;
import java.util.Vector;

public interface HDDataItf {
	public Vector<File> getRawFiles();
	public Vector<String> getRawFilesNames();
	public void addRawFile(File rawFile);
	public File getMappingFile();
	public void setMappingFile(File mappingFile);
	public File getLogFile();
	public void setLogFile(File logFile);
	public void setQClog(File file);
	public void setDimFile(File file);
	public File getDimFile();
}
