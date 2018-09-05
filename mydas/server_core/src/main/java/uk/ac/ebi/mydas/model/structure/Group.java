package uk.ac.ebi.mydas.model.structure;

import java.io.IOException;
import java.util.Collection;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 *
 * This class holds all of the information required to fully populate
 * a /dasstructure/chain/group element returned from the structure
 * command.  Some fields are optional and can be populated
 * using <code>null</code> as described in the JavaDoc for the
 * constructor.
 */
public class Group {
	protected final String name;
	protected final GroupType type;
	protected final String groupId;
	protected final String insertCode;
    protected final Collection<DasAtom> atoms;
	
	
	/**
	 * @param name <b>Mandatory</b> Is the name of the group, e.g. "ALA"
	 * @param type <b>Mandatory</b> Describes the type of group. It may be one of: amino, nucleotide, hetatom.
	 * @param groupId <b>Mandatory</b> Is a unique identifier within the structure. 
	 * @param insertCode <b>Optional</b> Is an upper-case alphabet character (A-Z) used to distinguish sequential groups with the same residue number. For example amino acid "86A" might be the 87th group in a chain, with an insertCode of "A".
	 * @param atoms <b>Mandatory</b> Each group has one or more atom elements, each representing a single atom in a single conformation.
	 * @throws DataSourceException To wrap any Exceptions thrown if this object is not constructed correctly.
	 */
	public Group(String name, GroupType type, String groupId, String insertCode,
			Collection<DasAtom> atoms) throws DataSourceException {
		super();
		if (name == null || type == null || groupId == null || atoms==null || atoms.size()<1 ){
            throw new DataSourceException ("An attempt to instantiate a Group of atoms without the minimal required mandatory values.");
        }
		this.name = name;
		this.type = type;
		this.groupId = groupId;
		this.insertCode = insertCode;
		this.atoms = atoms;
	}

	
	/**
	 * Generates the piece of XML into the XML serializer object to describe a /dasstructure/chain/group.
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
     */
	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"group");

		serializer.attribute(DAS_XML_NAMESPACE, "name", name);
		serializer.attribute(DAS_XML_NAMESPACE, "type", type.toString());
		serializer.attribute(DAS_XML_NAMESPACE, "groupID", groupId);
		if (insertCode!=null && insertCode.length()>0)
			serializer.attribute(DAS_XML_NAMESPACE, "insertCode", insertCode);
		
		if (atoms!=null)
			for(DasAtom atom : atoms)
				atom.serialize(DAS_XML_NAMESPACE,serializer);

		serializer.endTag(DAS_XML_NAMESPACE,"group");
	}
	
}
