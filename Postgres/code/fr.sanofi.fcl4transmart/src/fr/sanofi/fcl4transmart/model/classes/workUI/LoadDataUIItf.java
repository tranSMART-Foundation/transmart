package fr.sanofi.fcl4transmart.model.classes.workUI;

import java.io.File;

public interface LoadDataUIItf {
	public String getTopNode();
	public void openLoadingShell();
	public boolean getEtlServer();
	public void displayMessage(String message);
	public void waitForThread();
	public void setMessage(String message);
	public void setIsLoading(boolean isLoading);
	public void setLogFile(File file);
}
