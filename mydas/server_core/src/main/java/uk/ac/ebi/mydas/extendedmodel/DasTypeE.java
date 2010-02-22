package uk.ac.ebi.mydas.extendedmodel;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.model.DasComponentFeature;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasType;

public class DasTypeE extends DasType {
	private String label;
	
	public DasTypeE(String id, String category, String method) {
		super(id, category, method);
	}
	public DasTypeE(String id, String category, String method,String label) {
		super(id, category, method);
		this.label=label;
	}
	public DasTypeE(DasType type) {
		super(type.getId(), type.getCategory(), type.getMethod());
	}
	public void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer,Integer count) throws IllegalArgumentException, IllegalStateException, IOException{
		this.serialize( DAS_XML_NAMESPACE, serializer, count,true,false,false,false);
	}	
	public void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer,Integer count,boolean categorize,boolean hasReferenceFeatures,boolean hasSubParts,boolean hasSuperParts) throws IllegalArgumentException, IllegalStateException, IOException{
        serializer.startTag(DAS_XML_NAMESPACE, "TYPE");
        serializer.attribute(DAS_XML_NAMESPACE, "id", this.getId());
        if (this.getMethod() != null && this.getMethod().length() > 0){
            serializer.attribute(DAS_XML_NAMESPACE, "method", this.getMethod());
        }
        // Handle DasReferenceFeatures.
        if (hasReferenceFeatures){
            serializer.attribute(DAS_XML_NAMESPACE, "reference", "yes");
            serializer.attribute(DAS_XML_NAMESPACE, "superparts", (hasSuperParts) ? "yes" : "no");
            serializer.attribute(DAS_XML_NAMESPACE, "subparts", (hasSubParts) ? "yes" : "no");
        }
        if (categorize){
	        if (this.getCategory() != null && this.getCategory().length() > 0){
	            serializer.attribute(DAS_XML_NAMESPACE, "category", this.getCategory());
	        }else {
	            // To prevent the DAS server from dying, if no category has been set, but
	            // a category is required, spit out the type ID again as the category.
	            serializer.attribute(DAS_XML_NAMESPACE, "category", this.getId());
	        }
        }
        if (count != null){
            serializer.text(Integer.toString(count));
        }else  if (this.getLabel() != null && this.getLabel().length() > 0){
        	serializer.text(this.getLabel());
        }
        serializer.endTag(DAS_XML_NAMESPACE, "TYPE");

	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

}
