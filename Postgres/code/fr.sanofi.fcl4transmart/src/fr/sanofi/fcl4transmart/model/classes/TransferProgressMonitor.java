package fr.sanofi.fcl4transmart.model.classes;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import com.jcraft.jsch.SftpProgressMonitor;

public class TransferProgressMonitor implements SftpProgressMonitor {
	private ProgressBar bar;
	private Label progressLabel;
	private Label statusLabel;
	private String status;
	private long max=0;
	private long count=0;
	private boolean cancelled;
	private long percent=0;
	private TransferProcess process;
	public TransferProgressMonitor(TransferProcess process){
		this.process=process;
		this.status="Not started";
	}
	@Override
	public boolean count(long count) {
		this.count+=count;
		if(this.cancelled){
			return false;
		}
	      percent=this.count*100/max;
	 
	      Display.getDefault().asyncExec(new Runnable() {
              public void run() {
           	   if (!(progressLabel==null || progressLabel.isDisposed ())){
           		   if(percent<100){
           			   progressLabel.setText((int)(percent)+"%");
           		   }else{
           			progressLabel.setText("100%");
           		   }
           	   }
	           	if (!(bar==null || bar.isDisposed ())){
	           		if(percent<100){
	           			bar.setSelection((int)percent);
	           		}else{
	           			bar.setSelection(100);
	           		}
				}
              }
	    });
	    return true;
	}

	@Override
	public void end() {
		if(!this.cancelled){
			this.status="Loaded";
			changeStatus();
			process.finished();
		}
	}

	@Override
	public void init(int op, String src, String dest, long max) {
		this.status="Loading";
    	changeStatus();
          this.max=max;
          count=0;
	}
	public void setCancelled(boolean bool){
		this.cancelled=bool;
		this.status="Cancelled";
		changeStatus();
	}
	public void changeStatus(){
		Display.getDefault().asyncExec(new Runnable() {
            public void run() {
         	   if (!(statusLabel==null || statusLabel.isDisposed ())){
         		   statusLabel.setText(status);
         	   }
            }
	    });	
	}
	public void setStatus(String status){
		this.status=status;
		this.changeStatus();
	}
	public long getPercent(){
		return this.percent;
	}
	public void setBar(ProgressBar pbar){
		this.bar=pbar;
		Display.getDefault().asyncExec(new Runnable() {
            public void run() {
         	   if (!(bar==null || bar.isDisposed ())){
         		   bar.setSelection((int)percent);
         	   }
            }
	    });	
	}
	public void setStatusLabel(Label statusLabel){
		this.statusLabel=statusLabel;
		changeStatus();
	}
	public void setProgressLabel(Label label){
		this.progressLabel=label;
		Display.getDefault().asyncExec(new Runnable() {
            public void run() {
         	   if (!(progressLabel==null || progressLabel.isDisposed ())){
         		   progressLabel.setText(String.valueOf((int)percent)+"%");
         	   }
            }
	    });	
	}
}
