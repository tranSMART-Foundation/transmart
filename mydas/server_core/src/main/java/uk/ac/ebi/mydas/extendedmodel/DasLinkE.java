package uk.ac.ebi.mydas.extendedmodel;

import java.io.IOException;
import java.net.URL;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.model.DasLink;

public class DasLinkE extends DasLink {

	public DasLinkE(URL href, String text) {
		super(href, text);
	}
	public DasLinkE(DasLink link) {
		super(link.getHref(), link.getText());
	}
	public void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(DAS_XML_NAMESPACE, "LINK");
        serializer.attribute(DAS_XML_NAMESPACE, "href", this.getHref().toString());
        String linkText = this.getText();
        if (linkText != null && linkText.length() > 0){
            serializer.text(linkText);
        }
        serializer.endTag(DAS_XML_NAMESPACE, "LINK");
	
	}

}
