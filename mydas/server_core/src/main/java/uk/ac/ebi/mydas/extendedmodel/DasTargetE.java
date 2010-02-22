package uk.ac.ebi.mydas.extendedmodel;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasTarget;

public class DasTargetE extends DasTarget {

	public DasTargetE(String targetId, int startCoordinate, int stopCoordinate,
			String targetName) throws DataSourceException {
		super(targetId, startCoordinate, stopCoordinate, targetName);
	}
	public DasTargetE(DasTarget target) throws DataSourceException {
		super(target.getTargetId(), target.getStartCoordinate(), target.getStopCoordinate(), target.getTargetName());
	}
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
