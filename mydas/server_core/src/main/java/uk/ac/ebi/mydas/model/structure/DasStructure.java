package uk.ac.ebi.mydas.model.structure;

import java.io.IOException;
import java.util.Collection;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 *
 * This class holds all of the information required to fully populate
 * a /dasstructure element returned from the structure
 * command.  Some fields are optional and can be populated
 * using <code>null</code> as described in the JavaDoc for the
 * constructor.
 */
public class DasStructure {

	protected final DasObject object;
    protected final Collection<ObjectDetail> objectDetails;
    protected final Collection<DasChain> chains;
    protected final Collection<DasConnect> connects;

    /**
     * @param object <b>Mandatory</b> Provides basic details of each structure object
     * @param objectDetails <b>Optional</b> (May be null) Provides additional key-value details of each structure object.
     * @param chains <b>Optional</b> (May be null) Set of the structural chains. 
     * @param connects <b>Optional</b> (May be null) Set of the inter-atom connections.
     * @throws DataSourceException to wrap any Exceptions thrown if this object is not constructed correctly.
     */
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

    /**
	 * Generates the piece of XML into the XML serializer object to describe a DasStructure.
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
     */
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
