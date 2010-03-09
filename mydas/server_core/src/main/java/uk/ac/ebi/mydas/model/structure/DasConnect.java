package uk.ac.ebi.mydas.model.structure;

import java.io.IOException;
import java.util.Collection;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 *
 * Represents an inter-atom connection
 * This class holds all of the information required to fully populate
 * a /dasstructure/connect element returned from the structure
 * command.  Some fields are optional and can be populated
 * using <code>null</code> as described in the JavaDoc for the
 * constructor.
 */
public class DasConnect {
	protected final String type;
	protected final String atomSerial;
    protected final Collection<String> atoms;
    
	/**
	 * @param type Describes the type of connection (e.g. bond)
	 * @param atomSerial Is the atomID of the source of the bond.
	 * @param atoms Each target atom within the connection is represented by an atomID  element. The element has a single atomID (required) attribute, which is the atomID of the target atom.
	 * @throws DataSourceException
	 */
	public DasConnect(String type, String atomSerial, Collection<String> atoms) throws DataSourceException {
		super();
		if (type == null || atomSerial == null ){
            throw new DataSourceException ("An attempt to instantiate a ObjectDetail without the minimal required mandatory values.");
        }

		this.type = type;
		this.atomSerial = atomSerial;
		this.atoms = atoms;
	}

	/**
	 * Generates the piece of XML into the XML serializer object to describe a /dasstructure/connect.
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
     */
	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"connect");
		
		serializer.attribute(DAS_XML_NAMESPACE, "type", type);
		serializer.attribute(DAS_XML_NAMESPACE, "atomSerial", atomSerial);
		
		if (atoms!=null)
			for(String atomid : atoms){
				serializer.startTag(DAS_XML_NAMESPACE,"atomID");
				serializer.attribute(DAS_XML_NAMESPACE, "atomID", atomid);
				serializer.endTag(DAS_XML_NAMESPACE,"atomID");
				
			}

		serializer.endTag(DAS_XML_NAMESPACE,"connect");
	}
	
}
