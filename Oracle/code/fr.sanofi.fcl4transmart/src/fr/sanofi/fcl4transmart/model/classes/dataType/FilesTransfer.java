package fr.sanofi.fcl4transmart.model.classes.dataType;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;
import fr.sanofi.fcl4transmart.handlers.FTPPreferences;
import fr.sanofi.fcl4transmart.model.classes.TransferProcess;
import fr.sanofi.fcl4transmart.model.classes.steps.fileTransfer.ChooseFolder;
import fr.sanofi.fcl4transmart.model.classes.steps.fileTransfer.SelectFiles;
import fr.sanofi.fcl4transmart.model.classes.steps.fileTransfer.TransferMonitoring;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.StudyItf;

public class FilesTransfer implements DataTypeItf {
	private Vector<StepItf> steps;
	private Vector<TransferProcess> processes;
	private String remotePath;
	private String folderName;
	private HashMap<File, String> files;//hashmap with a file as key and its remote path as value
	private int processesRunning;
	private int nextProcess;
	private Vector<Integer> toRestart;
	public FilesTransfer(){
		this.files=new HashMap<File, String>();
		this.steps=new Vector<StepItf>();
		this.steps.add(new ChooseFolder(this));
		this.steps.add(new SelectFiles(this));
		this.steps.add(new TransferMonitoring(this));
		this.processes=new Vector<TransferProcess>();
		this.nextProcess=0;
		this.toRestart=new Vector<Integer>();
		this.folderName="";
	}
	@Override
	public Vector<StepItf> getSteps() {
		return this.steps;
	}

	@Override
	public void setFiles(File path) {
		// nothing to do

	}

	@Override
	public Vector<File> getFiles() {
		Vector<File> filesToTransfer=new Vector<File>();
		if(this.remotePath==null || this.remotePath.compareTo("")==0) return null;
		for(File f: this.files.keySet()){
			 if(this.files.get(f).compareTo(this.remotePath)==0){
				 filesToTransfer.add(f);
			 }
		}
		return filesToTransfer;
	}
	public void addFile(File file){
		this.files.put(file, this.remotePath);
	}
	public void removeFile(File file){
		File removed=null;
		for(File f: this.files.keySet()){
			if(f.getName().compareTo(file.getName())==0 && this.files.get(f).compareTo(this.remotePath)==0){
				removed=f;
				break;
			}
		}
		if(removed!=null){
			this.files.remove(removed);
		}
	}
	@Override
	public StudyItf getStudy() {
		return null;
	}

	@Override
	public File getPath() {
		return null;
	}
	public String toString(){
		return "File transfer";
	}
	public void setRemotePath(String remotePath){
		this.remotePath=remotePath;
	}
	public String getRemotePath(){
		return this.remotePath;
	}
	public Vector<TransferProcess> getProcesses(){
		return this.processes;
	}
	public void setProcesses(){
		Vector<Integer> indexesFound=new Vector<Integer>();
		//to do: modify so it takes less time
		int i=0;
		for(File f: this.files.keySet()){
			i++;
			if(this.files.get(f).compareTo(this.remotePath)==0){
				for(int j=0; j<this.processes.size(); j++){
					if(f.getName().compareTo(this.processes.get(j).getFileName())==0 && this.processes.get(j).getRemotePath().compareTo(this.remotePath)==0){
						indexesFound.add(i);
					}
				}
			}else{
				indexesFound.add(i);
			}
		}
		i=0;
		for(File f: this.files.keySet()){
			i++;
			if(!indexesFound.contains(i)){
				TransferProcess p=new TransferProcess(f, this);
				p.setRemotePath(this.remotePath);
				this.processes.add(p);
			}
		}
		
	}
	public void runProcesses(){
		for(int i=0; i<FTPPreferences.getMax(); i++){
			this.runNextProcess();
		}
	}
	public void runNextProcess(){
		if(this.toRestart.size()>0){
			this.processes.get(this.toRestart.get(0)).start();
			this.processesRunning++;
			this.toRestart.remove(0);
		}
		else if(this.processes.size()>this.nextProcess && this.processesRunning<FTPPreferences.getMax()){
			try{
				this.processes.get(this.nextProcess).start();
				this.processesRunning++;
				this.nextProcess++;
			}catch(IllegalThreadStateException e){
				if(this.toRestart.size()>0){
					this.processes.get(this.toRestart.get(0)).start();
					this.processesRunning++;
					this.toRestart.remove(0);
				}
			}
		}
	}
	public void processesFinished(){
		this.processesRunning--;
		this.runNextProcess();
	}
	public void restart(TransferProcess process){
		int i=this.processes.indexOf(process);
		if(i==-1) return;
		this.toRestart.add(i);
		this.processes.set(i, process.copyForRestart());
		this.runNextProcess();
	}
	public void setFolderName(String name){
		this.folderName=name;
	}
	public String getFolderName(){
		return this.folderName;
	}
}
