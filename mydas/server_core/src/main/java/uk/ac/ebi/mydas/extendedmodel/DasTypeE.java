package uk.ac.ebi.mydas.extendedmodel;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.model.DasType;

public class DasTypeE extends DasType {

	public DasTypeE(String id, String category, String method) {
		super(id, category, method);
	}
	public DasTypeE(DasType type) {
		super(type.getId(), type.getCategory(), type.getMethod());
	}
	
	public void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer,Integer count) throws IllegalArgumentException, IllegalStateException, IOException{
        serializer.startTag(DAS_XML_NAMESPACE, "TYPE");
        serializer.attribute(DAS_XML_NAMESPACE, "id", this.getId());
        if (this.getMethod() != null && this.getMethod().length() > 0){
            serializer.attribute(DAS_XML_NAMESPACE, "method", this.getMethod());
        }
        if (this.getCategory() != null && this.getCategory().length() > 0){
            serializer.attribute(DAS_XML_NAMESPACE, "category", this.getCategory());
        }
        if (count != null){
            serializer.text(Integer.toString(count));
        }
        serializer.endTag(DAS_XML_NAMESPACE, "TYPE");
		
	}

}
