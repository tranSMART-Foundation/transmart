package uk.ac.ebi.mydas.model.alignment;

import java.io.IOException;
import java.util.Collection;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 *
 * This class holds all of the information required to fully populate
 * a /dasalignment/alignment/alignObject element returned from the alignment
 * command.  Some fields are optional and can be populated
 * using <code>null</code> as described in the JavaDoc for the
 * constructor.
 */
public class AlignObject {
	protected final String dbAccessionId;
	protected final String objectVersion;
	protected final String intObjectId;
	protected final AlignType type;
	protected final String dbSource;
	protected final String dbVersion;	
	protected final String dbCoordSys;	
	protected final Collection<AlignObjectDetail> alignObjectDetails;
	protected final String sequence;
	
	/**
	 * @param dbAccessionId <b>Mandatory</b> is the external ID of this reference object. In the case of PDB structures, it does not include a chain identifier.
	 * @param objectVersion <b>Mandatory</b> is the object's version (the content of which depends on the coordinate system
	 * @param intObjectId <b>Mandatory</b> attribute is an internal ID of the object, referred to elsewhere within the document. It is usually the same as the external ID, but may be different if, for example, the alignment contains the same object more than once and thus requires a way to differentiate.
	 * @param type <b>Optional</b> Indicates the type of alignment object. One of (PROTEIN, DNA or STRUCTURE)
	 * @param dbSource <b>Mandatory</b> identify the database from which the object was taken
	 * @param dbVersion <b>Mandatory</b> identify the version of the database from which the object was taken
	 * @param dbCoordSys <b>Optional</b> attribute is the text description of the object's coordinate system, e.g. UniProt,Protein Sequence. Note that the coordinate system need not be the same as the database source, e.g. Pfam stores UniProt sequences.
	 * @param alignObjectDetails <b>Optional</b> Each alignObject may contain any number of key-value properties.
	 * @param sequence <b>Optional</b> The sequence of an alignObject may optionally be provided.
	 * @throws DataSourceException To wrap any Exceptions thrown if this object is not constructed correctly.
	 */
	public AlignObject(String dbAccessionId, String objectVersion,
			String intObjectId, AlignType type, String dbSource, String dbVersion,
			String dbCoordSys,
			Collection<AlignObjectDetail> alignObjectDetails, String sequence) throws DataSourceException {

		if (dbAccessionId == null || objectVersion == null || intObjectId == null || dbSource == null || dbVersion == null){
            throw new DataSourceException ("An attempt to instantiate a AlignObject object without the minimal required mandatory values.");
        }

		this.dbAccessionId = dbAccessionId;
		this.objectVersion = objectVersion;
		this.intObjectId = intObjectId;
		this.type = type;
		this.dbSource = dbSource;
		this.dbVersion = dbVersion;
		this.dbCoordSys = dbCoordSys;
		this.alignObjectDetails = alignObjectDetails;
		this.sequence = sequence;
	}

	/**
	 * Generates the piece of XML into the XML serializer object to describe an AlignmentObject.
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
     */
	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"alignObject");

		serializer.attribute(DAS_XML_NAMESPACE, "dbAccessionId", dbAccessionId);
		serializer.attribute(DAS_XML_NAMESPACE, "objectVersion", objectVersion);
		serializer.attribute(DAS_XML_NAMESPACE, "intObjectId", intObjectId);
		if (type!=null)
			serializer.attribute(DAS_XML_NAMESPACE, "type", type.toString());
		serializer.attribute(DAS_XML_NAMESPACE, "dbSource", dbSource);
		serializer.attribute(DAS_XML_NAMESPACE, "dbVersion", dbVersion);
		if (dbCoordSys!=null && dbCoordSys.length()>0)
			serializer.attribute(DAS_XML_NAMESPACE, "dbCoordSys", dbCoordSys);
		
		if (alignObjectDetails!=null)
			for (AlignObjectDetail alignObjectDetail: alignObjectDetails)
				alignObjectDetail.serialize( DAS_XML_NAMESPACE,  serializer);
		
		if (sequence!=null && sequence.length()>0){
			serializer.startTag(DAS_XML_NAMESPACE,"sequence");
			serializer.text(sequence);
			serializer.endTag(DAS_XML_NAMESPACE,"sequence");
		}

		serializer.endTag(DAS_XML_NAMESPACE,"alignObject");
		
	}
}
