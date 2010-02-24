package uk.ac.ebi.mydas.extendedmodel;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasGroup;
import uk.ac.ebi.mydas.model.DasTarget;

/**
 * Class that extends the basic DasGroup bean from the model to support serializing tasks
 * @author Gustavo Salazar
 *
 */
public class DasGroupE extends DasGroup {

	public DasGroupE(String groupId, String groupLabel, String groupType,
			Collection<String> notes, Map<URL, String> links,
			Collection<DasTarget> targets) throws DataSourceException {
		super(groupId, groupLabel, groupType, notes, links, targets);
	}
	
	/**
	 * Constructor to create a copy from a DasGroup
	 * @param group DasGroup with all its data loaded.
	 * @throws DataSourceException in case a problem in the creation
	 */
	public DasGroupE(DasGroup group) throws DataSourceException {
		super(group.getGroupId(),group.getGroupLabel(),group.getGroupType(),group.getNotes(),group.getLinks(),group.getTargets());
	}
	
	/**
	 * Generates the piece of XML into the XML serializer object to describe a Dasgroup 
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
	 * @throws DataSourceException indicate that there is something wrong with the data source
	 */
	public void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException, DataSourceException {
        serializer.startTag(DAS_XML_NAMESPACE, "GROUP");
        serializer.attribute(DAS_XML_NAMESPACE, "id", this.getGroupId());
        if (this.getGroupLabel() != null && this.getGroupLabel().length() > 0){
            serializer.attribute(DAS_XML_NAMESPACE, "label", this.getGroupLabel());
        }
        if (this.getGroupType() != null && this.getGroupType().length() > 0){
            serializer.attribute(DAS_XML_NAMESPACE, "type", this.getGroupType());
        }
        // GROUP/NOTE elements
        if (this.getNotes() != null){
            for (String note : this.getNotes()){
                serializer.startTag(DAS_XML_NAMESPACE, "NOTE");
                serializer.text (note);
                serializer.endTag(DAS_XML_NAMESPACE, "NOTE");
            }
        }

        // GROUP/LINK elements
        if (this.getLinks() != null){
            for (URL url : this.getLinks().keySet()){
                if (url != null){
                	(new DasLinkE(url,this.getLinks().get(url))).serialize(DAS_XML_NAMESPACE, serializer);
                }
            }
        }

        // GROUP/TARGET elements
        if (this.getTargets() != null){
            for (DasTarget target : this.getTargets()){
            	(new DasTargetE(target)).serialize(DAS_XML_NAMESPACE, serializer);
            }
        }

        serializer.endTag(DAS_XML_NAMESPACE, "GROUP");
		
	}

}
