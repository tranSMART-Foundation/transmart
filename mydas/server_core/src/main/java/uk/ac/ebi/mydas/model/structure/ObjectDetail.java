package uk.ac.ebi.mydas.model.structure;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 *
 * This class holds all of the information required to fully populate
 * a /dasstructure/objectDetail element returned from the structure
 * command.  Some fields are optional and can be populated
 * using <code>null</code> as described in the JavaDoc for the
 * constructor.
 */
public class ObjectDetail {
	protected final String dbSource;
	protected final String property;
	protected final String text;
	
	/**
	 * @param dbSource <b>Mandatory</b> Identifies the source of the property.
	 * @param <b>Mandatory</b> Identifies the name of the property.
	 * @param text <b>Mandatory</b> Provide a free-text value for the property.
	 * @throws DataSourceException To wrap any Exceptions thrown if this object is not constructed correctly.
	 */
	public ObjectDetail(String dbSource, String property, String text) throws DataSourceException {
		super();
		if (dbSource == null || property == null || text == null ){
            throw new DataSourceException ("An attempt to instantiate a ObjectDetail without the minimal required mandatory values.");
        }
		
		this.dbSource = dbSource;
		this.property = property;
		this.text = text;
	}

	/**
	 * Generates the piece of XML into the XML serializer object to describe a dasstructure/objectDetail.
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
     */
	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"objectDetail");
		
		serializer.attribute(DAS_XML_NAMESPACE, "dbSource", dbSource);
		serializer.attribute(DAS_XML_NAMESPACE, "property", property);
		
		serializer.text(text);
		
		serializer.endTag(DAS_XML_NAMESPACE,"objectDetail");
	}
}
