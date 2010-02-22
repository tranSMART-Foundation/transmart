package uk.ac.ebi.mydas.extendedmodel;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.model.DasMethod;

public class DasMethodE extends DasMethod {

	public DasMethodE(String id, String label) {
		super(id, label);
		// TODO Auto-generated constructor stub
	}
	public void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer)throws IllegalArgumentException, IllegalStateException, IOException{
        serializer.startTag(DAS_XML_NAMESPACE, "METHOD");
        if (this.getId() != null && this.getId().length() > 0){
            serializer.attribute(DAS_XML_NAMESPACE, "id", this.getId());
        }
        if (this.getLabel() != null && this.getLabel().length() > 0){
            serializer.text(this.getLabel());
        }
        serializer.endTag(DAS_XML_NAMESPACE, "METHOD");
	}

}
