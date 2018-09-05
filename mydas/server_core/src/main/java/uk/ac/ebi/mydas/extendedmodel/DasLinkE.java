package uk.ac.ebi.mydas.extendedmodel;

import java.io.IOException;
import java.net.URL;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasLink;

/**
 * Class that extends the basic DasLink bean from the model to support serializing tasks
 * @author Gustavo Salazar
 *
 */
public class DasLinkE extends DasLink {

	public DasLinkE(URL href, String text) {
		super(href, text);
	}

	/**
	 * Constructor to create a copy from a DasLink
	 * @param link DasLink with all its data loaded.
	 * @throws DataSourceException in case a problem in the creation
	 */
	public DasLinkE(DasLink link) {
		super(link.getHref(), link.getText());
	}
	/**
	 * Generates the piece of XML into the XML serializer object to describe a DasLink 
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
	 */
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
