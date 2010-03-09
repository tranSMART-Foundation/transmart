package uk.ac.ebi.mydas.model.structure;

import java.io.IOException;
import java.util.Collection;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

public class DasStructure {

	protected final DasObject object;
    protected final Collection<ObjectDetail> objectDetails;
    protected final Collection<DasChain> chains;
    protected final Collection<DasConnect> connects;

    public DasStructure(DasObject object,
			Collection<ObjectDetail> objectDetails,
			Collection<DasChain> chains, Collection<DasConnect> connects) throws DataSourceException {

		if (object == null ){
            throw new DataSourceException ("An attempt to instantiate a DasStructure object without the minimal required mandatory values.");
        }

    	this.object = object;
		this.objectDetails = objectDetails;
		this.chains = chains;
		this.connects = connects;
	}

	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"dasstructure");
		object.serialize( DAS_XML_NAMESPACE,  serializer);
		if (objectDetails!=null)
			for (ObjectDetail objDetail: objectDetails)
				objDetail.serialize( DAS_XML_NAMESPACE,  serializer);
		if (chains!=null)
			for (DasChain chain: chains)
				chain.serialize( DAS_XML_NAMESPACE,  serializer);
		if (connects!=null)
			for (DasConnect connect: connects)
				connect.serialize( DAS_XML_NAMESPACE,  serializer);
		serializer.endTag(DAS_XML_NAMESPACE,"dasstructure");
		
	}
}
