package uk.ac.ebi.mydas.model.structure;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

public class DasObject {
	protected final String dbAccessionId;
	protected final Integer intObjectId;
	protected final String objectVersion;
	protected final String type;
	protected final String dbSource;
	protected final String dbVersion;
	protected final String dbCoordSystem;
	
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
