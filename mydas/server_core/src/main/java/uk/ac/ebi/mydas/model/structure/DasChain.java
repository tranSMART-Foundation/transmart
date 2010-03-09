package uk.ac.ebi.mydas.model.structure;

import java.io.IOException;
import java.util.Collection;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

public class DasChain {
	protected final String chainId;
	protected final String model;
	protected final String swissprotId;
    protected final Collection<Group> groups;
    
	public DasChain(String chainId, String model, String swissprotId,
			Collection<Group> groups) throws DataSourceException {
		super();
		if (chainId == null  ){
            throw new DataSourceException ("An attempt to instantiate a ObjectDetail without the minimal required mandatory values.");
        }

		this.chainId = chainId;
		this.model = model;
		this.swissprotId = swissprotId;
		this.groups = groups;
	}

	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"chain");
		
		serializer.attribute(DAS_XML_NAMESPACE, "id", chainId);
		if (model!=null && model.length()>0)
			serializer.attribute(DAS_XML_NAMESPACE, "model", model);
		if (swissprotId!=null && swissprotId.length()>0)
			serializer.attribute(DAS_XML_NAMESPACE, "SwissprotId", swissprotId);
		
		if (groups!=null)
			for(Group group : groups)
				group.serialize(DAS_XML_NAMESPACE, serializer);

		serializer.endTag(DAS_XML_NAMESPACE,"chain");
		
	}
    
}
