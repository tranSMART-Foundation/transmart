package uk.ac.ebi.mydas.model.structure;

import java.io.IOException;
import java.util.Collection;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

public class Group {
	protected final String name;
	protected final String type;
	protected final String groupId;
	protected final String insertCode;
    protected final Collection<DasAtom> atoms;
    
	public Group(String name, String type, String groupId, String insertCode,
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

	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"group");

		serializer.attribute(DAS_XML_NAMESPACE, "name", name);
		serializer.attribute(DAS_XML_NAMESPACE, "type", type);
		serializer.attribute(DAS_XML_NAMESPACE, "groupID", groupId);
		if (insertCode!=null && insertCode.length()>0)
			serializer.attribute(DAS_XML_NAMESPACE, "insertCode", insertCode);
		
		if (atoms!=null)
			for(DasAtom atom : atoms)
				atom.serialize(DAS_XML_NAMESPACE,serializer);

		serializer.endTag(DAS_XML_NAMESPACE,"group");
	}
	
}
