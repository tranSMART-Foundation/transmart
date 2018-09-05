package uk.ac.ebi.mydas.model.structure;

import java.io.IOException;
import java.util.Collection;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 *
 * This class holds all of the information required to fully populate
 * a /dasstructure/chain element returned from the structure
 * command.  Some fields are optional and can be populated
 * using <code>null</code> as described in the JavaDoc for the
 * constructor.
 */
public class DasChain {
	protected final String chainId;
	protected final String model;
	protected final String swissprotId;
    protected final Collection<Group> groups;
    
	/**
	 * Represents a single structural chain.
	 * @param chainId identifier for the chain, e.g. "A" or "B".
	 * @param model <b>Mandatory</b> Is the model number of the chain, where applicable (e.g NMR structures).
	 * @param swissprotId <b>Optional</b> 
	 * @param groups <b>Optional</b> Each chain has zero or more group elements, each representing a group of atoms such as an amino acid or a hetero molecule.
	 * @throws DataSourceException To wrap any Exceptions thrown if this object is not constructed correctly.
	 */
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

	
	/**
	 * Generates the piece of XML into the XML serializer object to describe a dasstructure/chain.
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
	 */
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
