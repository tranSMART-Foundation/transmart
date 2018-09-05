package uk.ac.ebi.mydas.model;
import uk.ac.ebi.mydas.exceptions.DataSourceException;

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

    
	public DasMethod(String id, String label,String cvId) throws DataSourceException {
		//The id is mandatory
        if (id == null || id.trim().length()<1){
            throw new DataSourceException ("An attempt to instantiate a DasFeature object without the minimal required mandatory values.");
        }
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

    public void setCvId(String cvId) {
		this.cvId = cvId;
	}
    
	    /**
     * Implementation of equals method.
     * @param o object to compare with.
     * @return boolean indicating equality
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DasMethod dasMethod = (DasMethod) o;

        if (!id.equals(dasMethod.id)) return false;
        if (cvId != null ? !cvId.equals(dasMethod.cvId) : dasMethod.cvId != null) return false;

        return true;
    }

    /**
     * Implementation of hashcode method.
     * @return unique integer for each (distinct, by equals method) instance.
     */
    public int hashCode() {
        int result;
        result = id.hashCode();
        result = 31 * result + (cvId != null ? cvId.hashCode() : 0);
        return result;
    }

    /**
     * To string simple representation of this object.
     * @return simple representation of this object.
     */
    public String toString(){
        StringBuffer buf = new StringBuffer("DasMethod.  id: '");
        buf .append (id)
            .append ("' method: '")
            .append ((cvId == null) ? "null" : cvId)
            .append ("'");
        return buf.toString();
    }
}
