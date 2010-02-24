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

/**
 * Class that extends the basic DasFeature bean from the model to support serializing tasks
 * @author Gustavo Salazar
 *
 */
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
	/**
	 * Constructor to create a copy from a DasFeature
	 * @param feature Das feature with all its data loaded.
	 * @throws DataSourceException in case a problem in the creation
	 */
	public DasFeatureE(DasFeature feature) throws DataSourceException{
		super(feature.getFeatureId(), feature.getFeatureLabel(), feature.getTypeId(), feature.getTypeCategory(), feature.getTypeLabel(),
				feature.getMethodId(), feature.getMethodLabel(), feature.getStartCoordinate(), feature.getStopCoordinate(), feature.getScore(),
				feature.getOrientation(), feature.getPhase(), feature.getNotes(), feature.getLinks(), feature.getTargets(), feature.getGroups());
		
	}
	
	/**
	 * Generates the piece of XML into the XML serializer object to describe a DasFeature 
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @param filter Set of the query specifications to accept/reject a feature that belongs to the segment 
	 * @param categorize indicates if the categories will be included in the type of the feature
	 * @param isFeaturesStrictlyEnclosed indicates if a feature that is partially included in the segment should be excluded(true) or included(false)
	 * @param isUseFeatureIdForFeatureLabel indicates if a feature should use the Id as a label in case that a label is null
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
	 * @throws DataSourceException indicate that there is something wrong with the data source
	 */
	public void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer,DasFeatureRequestFilter filter,boolean categorize, boolean isUseFeatureIdForFeatureLabel) 
		throws IllegalArgumentException, IllegalStateException, IOException, DataSourceException {
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
