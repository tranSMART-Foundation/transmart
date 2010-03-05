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
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasPhase;
import uk.ac.ebi.mydas.model.DasTarget;
import uk.ac.ebi.mydas.model.DasType;

/**
 * Class that extends the basic DasFeature bean from the model to support serializing tasks
 * @author Gustavo Salazar
 *
 */
@SuppressWarnings("serial")
public class DasFeatureE extends DasFeature {



	
	public DasFeatureE(String featureId, String featureLabel, DasType type,
			DasMethod method, int startCoordinate, int endCoordinate,
			Double score, DasFeatureOrientation orientation, DasPhase phase,
			Collection<String> notes, Map<URL, String> links,
			Collection<DasTarget> targets, Collection<String> parents,
			Collection<String> parts) throws DataSourceException {
		super(featureId, featureLabel, type, method, startCoordinate, endCoordinate,
				score, orientation, phase, notes, links, targets, parents, parts);
	}
	/**
	 * Constructor to create a copy from a DasFeature
	 * @param feature Das feature with all its data loaded.
	 * @throws DataSourceException in case a problem in the creation
	 */
	public DasFeatureE(DasFeature feature) throws DataSourceException{
		super(feature.getFeatureId(), feature.getFeatureLabel(), feature.getType(), 
				feature.getMethod(), feature.getStartCoordinate(), feature.getStopCoordinate(),
				feature.getScore(), feature.getOrientation(), feature.getPhase(), feature.getNotes(), 
				feature.getLinks(), feature.getTargets(), feature.getParents(), feature.getParts());
		
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
	public void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer,DasFeatureRequestFilter filter,boolean categorize, boolean isUseFeatureIdForFeatureLabel, boolean hasReferences, boolean hasSuperParts,boolean hasSubParts) 
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
        	(new DasTypeE (this.getType())).serialize(DAS_XML_NAMESPACE, serializer, null,categorize,hasReferences,hasSubParts,hasSuperParts);

            // METHOD element
        	(new DasMethodE(this.getMethod())).serialize(DAS_XML_NAMESPACE, serializer);

        	//DAS1.6 START and END are optional for the cases of non positional features
        	if ((this.getStartCoordinate()!=0) || (this.getStopCoordinate()!=0)){
	            // START element
	            serializer.startTag(DAS_XML_NAMESPACE, "START");
	            serializer.text(Integer.toString(this.getStartCoordinate()));
	            serializer.endTag(DAS_XML_NAMESPACE, "START");
	
	            // END element
	            serializer.startTag(DAS_XML_NAMESPACE, "END");
	            serializer.text(Integer.toString(this.getStopCoordinate()));
	            serializer.endTag(DAS_XML_NAMESPACE, "END");
        	}

            // SCORE element
        	// DAS 1.6: The value of - is assumed if the tag is omitted entirely. therefore it is optional.
        	if (this.getScore() != null){
	            serializer.startTag(DAS_XML_NAMESPACE, "SCORE");
	            serializer.text (Double.toString(this.getScore()));
	            serializer.endTag(DAS_XML_NAMESPACE, "SCORE");
        	}
            // ORIENTATION element
        	// DAS 1.6: The value of 0 is assumed if the tag is omitted entirely. therefore it is optional.
        	if ((this.getOrientation()!=null) &&(this.getOrientation()!=DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE)){
	            serializer.startTag(DAS_XML_NAMESPACE, "ORIENTATION");
	            serializer.text (this.getOrientation().toString());
	            serializer.endTag(DAS_XML_NAMESPACE, "ORIENTATION");
        	}
            // PHASE element
        	// DAS 1.6: The value of - is assumed if the tag is omitted entirely. therefore it is optional.
            if ((this.getPhase()!=null) && (this.getPhase()!=DasPhase.PHASE_NOT_APPLICABLE)){
	        	serializer.startTag(DAS_XML_NAMESPACE, "PHASE");
	            serializer.text (this.getPhase().toString());
	            serializer.endTag(DAS_XML_NAMESPACE, "PHASE");
            }

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

            if (parents != null){
                for (String parent : this.getParents()){
                    serializer.startTag(DAS_XML_NAMESPACE, "PARENT");
                    serializer.attribute(DAS_XML_NAMESPACE, "id", parent);
                    serializer.endTag(DAS_XML_NAMESPACE, "PARENT");
                }
            }
            if (parts != null){
                for (String part : this.getParts()){
                    serializer.startTag(DAS_XML_NAMESPACE, "PART");
                    serializer.attribute(DAS_XML_NAMESPACE, "id", part);
                    serializer.endTag(DAS_XML_NAMESPACE, "PART");
                }
            }


            serializer.endTag(DAS_XML_NAMESPACE, "FEATURE");
        }
		
	}


}
