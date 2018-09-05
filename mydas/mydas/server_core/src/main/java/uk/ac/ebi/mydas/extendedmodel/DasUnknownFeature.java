package uk.ac.ebi.mydas.extendedmodel;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasFeatureOrientation;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasPhase;
import uk.ac.ebi.mydas.model.DasTarget;
import uk.ac.ebi.mydas.model.DasType;

@SuppressWarnings("serial")
public class DasUnknownFeature extends DasFeature {

	private DasUnknownFeature(String featureId, String featureLabel,
			DasType type, DasMethod method, int startCoordinate,
			int endCoordinate, Double score, DasFeatureOrientation orientation,
			DasPhase phase, Collection<String> notes, Map<URL, String> links,
			Collection<DasTarget> targets, Collection<String> parents,
			Collection<String> parts) throws DataSourceException {
		super(featureId, featureLabel, type, method, startCoordinate, endCoordinate,
				score, orientation, phase, notes, links, targets, parents, parts);
	}
	public DasUnknownFeature(String featureId) throws DataSourceException{
		this(featureId, "_", new DasType("_",null,null,null), new DasMethod(), 0, 0,
				0.0, DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE, null, null, null, null, null);
	}

    void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer) 
	throws IllegalArgumentException, IllegalStateException, IOException{
	    serializer.startTag(DAS_XML_NAMESPACE, "UNKNOWNFEATURE");
	    serializer.attribute(DAS_XML_NAMESPACE, "id", this.getFeatureId());
	    serializer.endTag(DAS_XML_NAMESPACE, "UNKNOWNFEATURE");
    }
}
