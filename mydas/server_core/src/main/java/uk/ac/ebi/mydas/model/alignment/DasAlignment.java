package uk.ac.ebi.mydas.model.alignment;

import java.io.IOException;
import java.util.Collection;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 *
 * This class holds all of the information required to fully populate
 * a /dasalignment element returned from the alignment
 * command.  Some fields are optional and can be populated
 * using <code>null</code> as described in the JavaDoc for the
 * constructor.
 */
public class DasAlignment {
	protected final Collection<Alignment> alignments;

	/**
	 * @param alignments <b>Mandatory</b> The document may contain any number of alignments(at least one), according to the query criteria.
	 * @throws DataSourceException  to wrap any Exceptions thrown if this object is not constructed correctly.
	 */
	public DasAlignment(Collection<Alignment> alignments) throws DataSourceException {
		if (alignments == null || alignments.size()<1 ){
            throw new DataSourceException ("An attempt to instantiate a DasAlignment object without the minimal required mandatory values.");
        }
		this.alignments = alignments;
	}

	/**
	 * Generates the piece of XML into the XML serializer object to describe a DasStructure.
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
     */
	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"dasalignment");
		for (Alignment alignment: alignments)
			alignment.serialize( DAS_XML_NAMESPACE,  serializer);
		serializer.endTag(DAS_XML_NAMESPACE,"dasalignment");
		
	}

}

