package fr.sanofi.fcl4transmart.model.classes.dataType;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

public interface IncrementalLoadingPossibleItf {
	public void setSubFolder(String folder) throws IOException;
	public boolean isIncremental();
	public Vector<File> getSubFolders();
}
