package fr.sanofi.fcl4transmart.model.classes.workUI;

import java.io.File;

public interface LoadAnnotationUIItf {
	public String getPathToFile();
	public String getPlatformId();
	public String getAnnotationTitle();
	public void setMessage(String message);
	public void setIsLoading(boolean isLoading);
	public boolean getEtlServer();
	public void waitForThread();
	public void displayMessage(String message);
	public void openLoadingShell();
	public void setAnnotationLogFile(File log);
}
