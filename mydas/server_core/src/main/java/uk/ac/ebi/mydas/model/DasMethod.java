package uk.ac.ebi.mydas.model;
import java.io.Serializable;

public class DasMethod implements Serializable{
    /**
     * the method id
     */
    private String id;

    /**
     * The content of the method.
     */
    private String label;
    
    public DasMethod(){}

	public DasMethod(String id, String label) {
		this.id = id;
		this.label = label;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
    
	
}
