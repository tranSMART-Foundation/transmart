/*
 * Copyright 2007 Philip Jones, EMBL-European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * For further details of the mydas project, including source code,
 * downloads and documentation, please see:
 *
 * http://code.google.com/p/mydas/
 *
 */

package uk.ac.ebi.mydas.model;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.extendedmodel.DasFeatureE;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.io.Serializable;

/**
 * Created Using IntelliJ IDEA.
 * Date: 16-May-2007
 * Time: 16:08:39
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * The DasComponentFeature allows you to build an assembly that can be served from
 * a reference server. (There is nothing stopping you from doing this on an annotation server,
 * but this is not normal practice.)
 *
 * It is highly recommended that you read through the specification section
 * <a href ="http://biodas.org/documents/spec.html#assemblies">
 *    DAS 1.53: Fetching Sequence Assemblies
 * </a>
 * before proceeding, if you have not done so already.
 *
 * More complete documentation of the DasComponentFeature mechanism for
 * fetching sequence assemblies can be found on the project wiki pages:
 * <a href="http://code.google.com/p/mydas/wiki/HOWTO_Build_Sequence_Assemblies">
 *     Building Sequence Assemblies using DasComponentFeature objects
 * </a>
 *
 * To get started, you first of all need to create a DasAnnotatedSegment object (essentially a 'feature holder') that
 * you would normally use to hold a Collection of DasFeature objects annotated on a particular segment.
 *
 * Once you have this object, call the <code>public DasComponentFeature getSelfComponentFeature()</code>
 * method, which will return a  DasComponentFeature representing the DasAnnotatedSegment.  You can then add
 * subparts or superparts to this object using the
 * <code>public DasComponentFeature addSubComponent(args...)</code> and
 * <code>public DasComponentFeature addSuperComponent(args...)</code> methods.  These methods also return
 * DasComponentFeature objects, to which in turn you can add further subparts or superparts.  This will allow
 * these subparts or superparts to report their status correctly.
 * 
 * Note that while it is theoretically possible to construct an assembly to any depth in this way, there is no
 * point in going beyond three tiers of components (up or down the assembly) as only two are reported by the
 * features command, with the third tier being hinted at in the /DASGFF/GFF/SEGMENT/FEATURE/TYPE/@superparts or
 * /DASGFF/GFF/SEGMENT/FEATURE/TYPE/@subparts attributes of the second-tier components.
 */
@SuppressWarnings("serial")
public class DasComponentFeature extends DasFeatureE implements Serializable {

    private Collection<DasComponentFeature> subComponents = null;

    private Collection<DasComponentFeature> superComponents = null;

    private DasComponentFeature(String featureId,
                    String featureLabel,
                    String targetSegmentId,
                    String componentTargetLabel,
                    int startCoordinateOnComponent,
                    int endCoordinateOnComponent,
                    DasType type,
                    DasMethod method,
                    int startCoodinateOnSegment,
                    int endCoordinateOnSegment,
                    Double score,
                    DasFeatureOrientation orientation,
                    DasPhase phase,
                    Collection<String> notes,
                    Map<URL, String> links)
        throws DataSourceException {

        super(
        		featureId, 
        		featureLabel, 
        		type, 
        		method, 
        		startCoodinateOnSegment, 
        		endCoordinateOnSegment,
				score, 
				orientation, 
				phase, 
				notes, 
				links, 
                new ArrayList<DasTarget>(1),
                null,
                null
        );
        DasTarget componentTarget = new DasTarget(targetSegmentId, startCoordinateOnComponent, endCoordinateOnComponent, componentTargetLabel);
        this.getTargets().add (componentTarget);
    }


    DasComponentFeature(DasAnnotatedSegment segment) throws DataSourceException {
        super(
        		segment.getSegmentId(),
        		null, 
        		new DasType("ThisSegment", "component", null,null), 
        		new DasMethod("assembly", null,null), 
                segment.getStartCoordinate(),
                segment.getStopCoordinate(),
				0.00, 
                DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE,
                DasPhase.PHASE_NOT_APPLICABLE,
                null,
                null,
                new ArrayList<DasTarget>(1),
                null,
                null
        );

        this.getTargets().add(
                new DasTarget(
                    segment.getSegmentId(),
                    segment.getStartCoordinate(),
                    segment.getStopCoordinate(),
                    segment.getSegmentLabel()
                )
        );
    }

    /**
     * This method is called by the mydas servlet to determine if there are any subparts to this component.
     * @return a boolean indicating if the component has sub-components.
     */
    public boolean hasSubParts(){
        return subComponents != null && subComponents.size() > 0;
    }

    /**
     * This method is called by the mydas servlet to determine if there are any superparts to this component.
     * @return a boolean indicating if the component has super-components.
     */
    public boolean hasSuperParts(){
        return superComponents != null && superComponents.size() > 0;
    }

    public DasComponentFeature addSubComponent(
                                String componentFeatureId,
                                int startCoordinateOnSegment,
                                int stopCoordinateOnSegment,
                                int startCoordinateOnComponent,
                                int stopCoordinateOnComponent,
                                String componentFeatureLabel,
                                DasType componentType,
                                String targetSegmentId,
                                String componentTargetLabel,
                                DasMethod componentMethod,
                                Double componentScore,
                                DasFeatureOrientation componentOrientation,
                                DasPhase componentPhase,
                                Collection<String> componentNotes,
                                Map<URL, String> componentLinks

                                ) throws DataSourceException {

        if (subComponents == null){
            subComponents =  new ArrayList<DasComponentFeature>();
        }

        DasComponentFeature newSubComponent = new DasComponentFeature(
            componentFeatureId,
            componentFeatureLabel,
            targetSegmentId,
            componentTargetLabel,
            startCoordinateOnComponent,
            stopCoordinateOnComponent,
            componentType, //Category should be component
            componentMethod,
            startCoordinateOnSegment,
            stopCoordinateOnSegment,
            componentScore,
            componentOrientation,
            componentPhase,
            componentNotes,
            componentLinks
        );

        subComponents.add (newSubComponent);
        if(parts==null)
        	parts =  new ArrayList<String>();
        parts.add(newSubComponent.getFeatureId());
        // Relationship needs to go both ways so the subpart and superpart attributes
        // are set correctly....
        newSubComponent.addSuperComponent(this);
        return newSubComponent;
    }

    private void addSubComponent (DasComponentFeature subComponentFeature){
        if (subComponents == null){
            subComponents =  new ArrayList<DasComponentFeature>();
        }
        if(parts==null)
        	parts =  new ArrayList<String>();
        
        parts.add(subComponentFeature.getFeatureId());
        subComponents.add (subComponentFeature);
    }

    /**
     *
     * @param componentFeatureId
     * @param startCoordinateOnSegment
     * @param stopCoordinateOnSegment
     * @param startCoordinateOnComponent
     * @param stopCoordinateOnComponent
     * @param componentFeatureLabel
     * @param componentTypeId
     * @param componentTypeLabel
     * @param targetSegmentId
     * @param componentTargetLabel
     * @param componentMethodId
     * @param componentMethodLabel
     * @param componentScore
     * @param componentOrientation
     * @param componentPhase
     * @param componentNotes
     * @param componentLinks
     * @return
     * @throws DataSourceException
     */
    public DasComponentFeature addSuperComponent(String componentFeatureId,
                                int startCoordinateOnSegment,
                                int stopCoordinateOnSegment,
                                int startCoordinateOnComponent,
                                int stopCoordinateOnComponent,
                                String componentFeatureLabel,
                                DasType componentType,
                                String targetSegmentId,
                                String componentTargetLabel,
                                DasMethod componentMethod,
                                Double componentScore,
                                DasFeatureOrientation componentOrientation,
                                DasPhase componentPhase,
                                Collection<String> componentNotes,
                                Map<URL, String> componentLinks) throws DataSourceException {
        if (superComponents == null){
            superComponents =  new ArrayList<DasComponentFeature>();
        }
        DasComponentFeature newSuperComponent = new DasComponentFeature(
                componentFeatureId,
                componentFeatureLabel,
                targetSegmentId,
                componentTargetLabel,
                startCoordinateOnComponent,
                stopCoordinateOnComponent,
                componentType,//Should be supercomponent
                componentMethod,
                startCoordinateOnSegment,
                stopCoordinateOnSegment,
                componentScore,
                componentOrientation,
                componentPhase,
                componentNotes,
                componentLinks
        );
        superComponents.add (newSuperComponent);
        if(parents==null)
        	parents =  new ArrayList<String>();
        parents.add(newSuperComponent.getFeatureId());
        // Relationship needs to go both ways so the subpart and superpart attributes
        // are set correctly....
        newSuperComponent.addSubComponent(this);
        return newSuperComponent;
    }

    private void addSuperComponent (DasComponentFeature superComponentFeature){
        if (superComponents == null){
            superComponents =  new ArrayList<DasComponentFeature>();
        }
        if(parents==null)
        	parents =  new ArrayList<String>();
        parents.add(superComponentFeature.getFeatureId());
        superComponents.add (superComponentFeature);
    }

    private Collection<DasComponentFeature> getSubComponents() {
        if (subComponents == null)
			return Collections.emptyList();
		else
			return subComponents;
    }

    /**
     *
     * @return
     */
    public Collection<DasComponentFeature> getReportableSubComponents() {
        Collection<DasComponentFeature> deepComponents = new ArrayList<DasComponentFeature>();
        for (DasComponentFeature component : getSubComponents()) {
            deepComponents.add(component);
//            deepComponents.addAll(component.getReportableSubComponents());
        }
        return deepComponents;
    }

    private Collection<DasComponentFeature> getSuperComponents() {
        if (superComponents == null)
			return Collections.emptyList();
		else
			return superComponents;
   }

    /**
     *
     * @return
     */
    public Collection<DasComponentFeature> getReportableSuperComponents() {
        Collection<DasComponentFeature> deepSuperComponents = new ArrayList<DasComponentFeature>();
        for (DasComponentFeature component : getSuperComponents()) {
            deepSuperComponents.add(component);
        }
        return deepSuperComponents;
    }

    /**
     *
     * @return
     */
    public boolean isTypeIsReference() {
        return true;
    }
    public String toString(){
    	String text ="Feature :"+super.toString();
    	text += "| supercomponents : {";
//        for (DasComponentFeature component : getSuperComponents()) 
//        	text += "-"+component.toString()+"-";
    	text += "} | subcomponents : {";
        for (DasComponentFeature component : getSubComponents()) 
        	text += "-"+component.toString()+"-";
    	
    	return text+"}";
    }
}
