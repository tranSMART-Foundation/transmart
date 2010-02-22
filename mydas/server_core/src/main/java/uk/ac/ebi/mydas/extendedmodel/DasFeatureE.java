package uk.ac.ebi.mydas.extendedmodel;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.controller.DasFeatureRequestFilter;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasComponentFeature;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasFeatureOrientation;
import uk.ac.ebi.mydas.model.DasGroup;
import uk.ac.ebi.mydas.model.DasPhase;
import uk.ac.ebi.mydas.model.DasTarget;

public class DasFeatureE extends DasFeature {

	public DasFeatureE(String featureId, String featureLabel, String typeId,
			String typeCategory, String typeLabel, String methodId,
			String methodLabel, int startCoordinate, int endCoordinate,
			Double score, DasFeatureOrientation orientation, DasPhase phase,
			Collection<String> notes, Map<URL, String> links,
			Collection<DasTarget> targets, Collection<DasGroup> groups)
			throws DataSourceException {
		super(featureId, featureLabel, typeId, typeCategory, typeLabel,
				methodId, methodLabel, startCoordinate, endCoordinate, score,
				orientation, phase, notes, links, targets, groups);
	}
	public DasFeatureE(DasFeature feature) throws DataSourceException{
		super(feature.getFeatureId(), feature.getFeatureLabel(), feature.getTypeId(), feature.getTypeCategory(), feature.getTypeLabel(),
				feature.getMethodId(), feature.getMethodLabel(), feature.getStartCoordinate(), feature.getStopCoordinate(), feature.getScore(),
				feature.getOrientation(), feature.getPhase(), feature.getNotes(), feature.getLinks(), feature.getTargets(), feature.getGroups());
		
	}
	
	public void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer,DasFeatureRequestFilter filter,boolean categorize,boolean isFeaturesStrictlyEnclosed, boolean isUseFeatureIdForFeatureLabel) throws IllegalArgumentException, IllegalStateException, IOException, DataSourceException{
        // Check the feature passes the filter.
        if (filter.featurePasses(this)){
            serializer.startTag(DAS_XML_NAMESPACE, "FEATURE");
            serializer.attribute(DAS_XML_NAMESPACE, "id", this.getFeatureId());
            if (this.getFeatureLabel() != null && this.getFeatureLabel().length() > 0){
                serializer.attribute(DAS_XML_NAMESPACE, "label", this.getFeatureLabel());
            }
            else if (isUseFeatureIdForFeatureLabel){
                serializer.attribute(DAS_XML_NAMESPACE, "label", this.getFeatureId());
            }

            // TYPE element
            boolean hasReferences=false;
            boolean hasSuperParts=false;
            boolean hasSubParts=false;
            if ((DasFeature)this instanceof DasComponentFeature){
            	hasReferences=true;
                DasComponentFeature refFeature = (DasComponentFeature)(DasFeature)this;
                hasSuperParts=refFeature.hasSuperParts();
                hasSubParts=refFeature.hasSubParts();
            }
        	(new DasTypeE (this.getTypeId(),this.getTypeCategory(),null,this.getTypeLabel())).serialize(DAS_XML_NAMESPACE, serializer, null,categorize,hasReferences,hasSubParts,hasSuperParts);

            // METHOD element
        	(new DasMethodE(this.getMethodId(),this.getMethodLabel())).serialize(DAS_XML_NAMESPACE, serializer);

            // START element
            serializer.startTag(DAS_XML_NAMESPACE, "START");
            serializer.text(Integer.toString(this.getStartCoordinate()));
            serializer.endTag(DAS_XML_NAMESPACE, "START");

            // END element
            serializer.startTag(DAS_XML_NAMESPACE, "END");
            serializer.text(Integer.toString(this.getStopCoordinate()));
            serializer.endTag(DAS_XML_NAMESPACE, "END");

            // SCORE element
            serializer.startTag(DAS_XML_NAMESPACE, "SCORE");
            serializer.text ((this.getScore() == null) ? "-" : Double.toString(this.getScore()));
            serializer.endTag(DAS_XML_NAMESPACE, "SCORE");

            // ORIENTATION element
            serializer.startTag(DAS_XML_NAMESPACE, "ORIENTATION");
            serializer.text (this.getOrientation().toString());
            serializer.endTag(DAS_XML_NAMESPACE, "ORIENTATION");

            // PHASE element
            serializer.startTag(DAS_XML_NAMESPACE, "PHASE");
            serializer.text (this.getPhase().toString());
            serializer.endTag(DAS_XML_NAMESPACE, "PHASE");

            // NOTE elements
            if (this.getNotes() != null){
                for (String note : this.getNotes()){
                    serializer.startTag(DAS_XML_NAMESPACE, "NOTE");
                    serializer.text (note);
                    serializer.endTag(DAS_XML_NAMESPACE, "NOTE");
                }
            }

            // LINK elements
            if (this.getLinks() != null){
                for (URL url : this.getLinks().keySet()){
                    if (url != null){
                    	(new DasLinkE(url,this.getLinks().get(url))).serialize(DAS_XML_NAMESPACE, serializer);
                    }
                }
            }

            // TARGET elements
            if (targets != null){
                for (DasTarget target : this.getTargets()){
                	(new DasTargetE(target)).serialize(DAS_XML_NAMESPACE, serializer);
                }
            }


            // GROUP elements
            if (this.getGroups() != null){
                for (DasGroup group : this.getGroups()){
                	(new DasGroupE(group)).serialize(DAS_XML_NAMESPACE, serializer);
                }
            }

            serializer.endTag(DAS_XML_NAMESPACE, "FEATURE");
        }
		
	}


}
