package uk.ac.ebi.mydas.extendedmodel;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasTarget;

/**
 * Class that extends the basic DasTarget bean from the model to support serializing tasks
 * @author Gustavo Salazar
 *
 */
public class DasTargetE extends DasTarget {

	public DasTargetE(String targetId, int startCoordinate, int stopCoordinate,
			String targetName) throws DataSourceException {
		super(targetId, startCoordinate, stopCoordinate, targetName);
	}
	/**
	 * Constructor to create a copy from a DasTarget
	 * @param target DasTarget with all its data loaded.
	 * @throws DataSourceException in case a problem in the creation
	 */
	public DasTargetE(DasTarget target) throws DataSourceException {
		super(target.getTargetId(), target.getStartCoordinate(), target.getStopCoordinate(), target.getTargetName());
	}
	/**
	 * Generates the piece of XML into the XML serializer object to describe a DasTarget
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
	 */
	public void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(DAS_XML_NAMESPACE, "TARGET");
        serializer.attribute(DAS_XML_NAMESPACE, "id", this.getTargetId());
        serializer.attribute(DAS_XML_NAMESPACE, "start", Integer.toString(this.getStartCoordinate()));
        serializer.attribute(DAS_XML_NAMESPACE, "stop", Integer.toString(this.getStopCoordinate()));
        if (this.getTargetName() != null && this.getTargetName().length() > 0){
            serializer.text(this.getTargetName());
        }
        serializer.endTag(DAS_XML_NAMESPACE, "TARGET");
	}

}
