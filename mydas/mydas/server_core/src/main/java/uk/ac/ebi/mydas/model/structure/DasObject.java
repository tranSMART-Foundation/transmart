package uk.ac.ebi.mydas.model.structure;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 *
 * Provides basic details of each structure object.
 * This class holds all of the information required to fully populate
 * a /dasstructure/object element returned from the structure
 * command.  Some fields are optional and can be populated
 * using <code>null</code> as described in the JavaDoc for the
 * constructor.
 */
public class DasObject {
	protected final String dbAccessionId;
	protected final Integer intObjectId;
	protected final String objectVersion;
	protected final String type;
	protected final String dbSource;
	protected final String dbVersion;
	protected final String dbCoordSystem;
	
	/**
	 * @param dbAccessionId <b>Mandatory</b> Unique identifier for the structure record - that is, the query ID.
	 * @param intObjectId <b>Optional</b> 
	 * @param objectVersion <b>Mandatory</b> Identifies the version of the structure. The content of the attribute depends on the coordinate system.
	 * @param type <b>Optional</b> 
	 * @param dbSource <b>Mandatory</b> Is the name of the database containing the structure.
	 * @param dbVersion <b>Mandatory</b> Is the version of the database containing the structure.
	 * @param dbCoordSystem <b>Mandatory</b> Is the text description of the object's coordinate system, e.g. PDBresnum,Protein Structure. 
	 * @throws DataSourceException to wrap any Exceptions thrown if this object is not constructed correctly.
	 */
	public DasObject(String dbAccessionId, Integer intObjectId,
			String objectVersion, String type, String dbSource,
			String dbVersion, String dbCoordSystem) throws DataSourceException {

		if (dbAccessionId == null || objectVersion == null || dbSource == null || dbVersion == null){
            throw new DataSourceException ("An attempt to instantiate a DasObject without the minimal required mandatory values.");
        }
			
		this.dbAccessionId = dbAccessionId;
		this.intObjectId = intObjectId;
		this.objectVersion = objectVersion;
		this.type = type;
		this.dbSource = dbSource;
		this.dbVersion = dbVersion;
		this.dbCoordSystem = dbCoordSystem;
	}

    /**
	 * Generates the piece of XML into the XML serializer object to describe a dasstructure/object.
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
     */
	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"object");
		
		serializer.attribute(DAS_XML_NAMESPACE, "dbAccessionId", dbAccessionId);
		if (intObjectId!=null)
			serializer.attribute(DAS_XML_NAMESPACE, "intObjectId", intObjectId.toString());
		serializer.attribute(DAS_XML_NAMESPACE, "objectVersion", objectVersion);
		if (type!=null && type.length()>0)
			serializer.attribute(DAS_XML_NAMESPACE, "type", type);
		serializer.attribute(DAS_XML_NAMESPACE, "dbSource", dbSource);
		serializer.attribute(DAS_XML_NAMESPACE, "dbVersion", dbVersion);
		if (dbCoordSystem!=null && dbCoordSystem.length()>0)
			serializer.attribute(DAS_XML_NAMESPACE, "dbCoordSys", dbCoordSystem);
			
		serializer.endTag(DAS_XML_NAMESPACE,"object");
	}
	
}
