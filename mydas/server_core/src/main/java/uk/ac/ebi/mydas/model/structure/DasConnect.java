package uk.ac.ebi.mydas.model.structure;

import java.io.IOException;
import java.util.Collection;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

public class DasConnect {
	protected final String type;
	protected final String atomSerial;
    protected final Collection<String> atoms;
    
	public DasConnect(String type, String atomSerial, Collection<String> atoms) throws DataSourceException {
		super();
		if (type == null || atomSerial == null ){
            throw new DataSourceException ("An attempt to instantiate a ObjectDetail without the minimal required mandatory values.");
        }

		this.type = type;
		this.atomSerial = atomSerial;
		this.atoms = atoms;
	}

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
