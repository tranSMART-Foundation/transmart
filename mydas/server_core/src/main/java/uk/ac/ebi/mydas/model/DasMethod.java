package uk.ac.ebi.mydas.model;
import java.io.Serializable;

@SuppressWarnings("serial")
public class DasMethod implements Serializable{
    /**
     * the method id
     */
    private String id;

    /**
     * The content of the method.
     */
    private String label;
    
    /**
     * Added for DAS1.6
     * Ontology term ID from the Evidence Codes Ontology.
     */
    private String cvId;
    
    public DasMethod(){}

	public DasMethod(String id, String label,String cvId) {
		this.id = id;
		this.label = label;
		this.cvId=cvId;
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

	public String getCvId() {
		return cvId;
	}
    
	
}
