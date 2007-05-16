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

import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.net.URL;

/**
 * Created Using IntelliJ IDEA.
 * Date: 16-May-2007
 * Time: 16:08:39
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class DasComponentFeature extends DasFeature {

    Collection<DasComponentFeature> subComponents = null;

    Collection<DasComponentFeature> superComponents = null;

    /**
     * This class is designed to simplify the process of building an assembly using features in DAS.
     * After constructing one of these components, it is possible to add DasComponent or DasSuperComponent
     * objects to this instance, in order to describe a hierarchical assembly.
     *
     * <b>Note</b> You should NOT add a component
     * @param featureId
     * @param featureLabel
     * @param targetSegmentId
     * @param componentTargetLabel
     * @param startCoordinateOnComponent
     * @param endCoordinateOnComponent
     * @param typeId
     * @param typeLabel
     * @param methodId
     * @param methodLabel
     * @param startCoodinateOnSegment
     * @param endCoordinateOnSegment
     * @param score
     * @param orientation
     * @param phase
     * @param notes
     * @param links
     * @throws DataSourceException
     */
    private DasComponentFeature(String featureId,
                        String featureLabel,
                        String targetSegmentId,
                        String componentTargetLabel,
                        int startCoordinateOnComponent,
                        int endCoordinateOnComponent,
                        String typeId,
                        String category,
                        String typeLabel,
                        String methodId,
                        String methodLabel,
                        int startCoodinateOnSegment,
                        int endCoordinateOnSegment,
                        Double score,
                        String orientation,
                        String phase,
                        Collection<String> notes,
                        Map<URL, String> links)
            throws DataSourceException {
        super(
                featureId,
                featureLabel,
                typeId,
                category,
                typeLabel,
                methodId,
                methodLabel,
                startCoodinateOnSegment,
                endCoordinateOnSegment,
                score,
                orientation,
                phase,
                notes,
                links,
                new ArrayList<DasTarget>(1),
                null
        );
        DasTarget componentTarget = new DasTarget(targetSegmentId, startCoordinateOnComponent, endCoordinateOnComponent, componentTargetLabel);
        this.getTargets().add (componentTarget);
    }


    public DasComponentFeature(DasAnnotatedSegment segment) throws DataSourceException {
        super(
                segment.getSegmentId(),
                null,
                "ThisSegment",
                "component",
                null,
                "assembly",
                null,
                segment.getStartCoordinate(),
                segment.getStopCoordinate(),
                0.00,
                DasFeature.ORIENTATION_NOT_APPLICABLE,
                DasFeature.PHASE_NOT_APPLICABLE,
                null,
                null,
                new ArrayList<DasTarget>(1),
                null
        );
        this.getTargets().add(new DasTarget(
                segment.getSegmentId(),
                segment.getStartCoordinate(),
                segment.getStopCoordinate(),
                segment.getSegmentLabel()
        ));
    }

    public boolean hasSubParts(){
        return subComponents != null && subComponents.size() > 0;
    }

    public boolean hasSuperParts(){
        return superComponents != null && superComponents.size() > 0;
    }

    public void addSubComponent(String componentFeatureId,
                                int startCoordinateOnComponent,
                                int stopCoordinateOnComponent,
                                int startCoordinateOnSegment,
                                int stopCoordinateOnSegment,
                                String componentFeatureLabel,
                                String componentTypeId,
                                String componentTypeLabel,
                                String targetSegmentId,
                                String componentTargetLabel,
                                String componentMethodId,
                                String componentMethodLabel,
                                Double componentScore,
                                String componentOrientation,
                                String componentPhase,
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
                componentTypeId,
                "component",
                componentTypeLabel,
                componentMethodId,
                componentMethodLabel,
                startCoordinateOnSegment,
                stopCoordinateOnSegment,
                componentScore,
                componentOrientation,
                componentPhase,
                componentNotes,
                componentLinks
        );

        subComponents.add (newSubComponent);
        newSubComponent.addSuperComponent(this);
    }

    private void addSubComponent (DasComponentFeature subComponentFeature){
        if (subComponents == null){
            subComponents =  new ArrayList<DasComponentFeature>();
        }
        subComponents.add (subComponentFeature);
    }

    public void addSuperComponent(String componentFeatureId,
                                int startCoordinateOnComponent,
                                int stopCoordinateOnComponent,
                                int startCoordinateOnSegment,
                                int stopCoordinateOnSegment,
                                String componentFeatureLabel,
                                String componentTypeId,
                                String componentTypeLabel,
                                String targetSegmentId,
                                String componentTargetLabel,
                                String componentMethodId,
                                String componentMethodLabel,
                                Double componentScore,
                                String componentOrientation,
                                String componentPhase,
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
                componentTypeId,
                "supercomponent",
                componentTypeLabel,
                componentMethodId,
                componentMethodLabel,
                startCoordinateOnSegment,
                stopCoordinateOnSegment,
                componentScore,
                componentOrientation,
                componentPhase,
                componentNotes,
                componentLinks
        );
        superComponents.add (newSuperComponent);
        // Relationship needs to go both ways...
        newSuperComponent.addSubComponent(this);
    }

    private void addSuperComponent (DasComponentFeature superComponentFeature){
        if (superComponents == null){
            superComponents =  new ArrayList<DasComponentFeature>();
        }
        superComponents.add (superComponentFeature);
    }

    public Collection<DasComponentFeature> getSubComponents() {
        return (subComponents == null) ? Collections.EMPTY_LIST : subComponents;
    }

    public Collection<DasComponentFeature> getDeepSubComponents() {
        Collection<DasComponentFeature> deepComponents = new ArrayList<DasComponentFeature>();
        for (DasComponentFeature component : getSubComponents()) {
            deepComponents.add(component);
            deepComponents.addAll(component.getSubComponents());
        }
        return deepComponents;
    }

    public Collection<DasComponentFeature> getSuperComponents() {
        return (superComponents == null) ? Collections.EMPTY_LIST : superComponents;
    }

    public Collection<DasComponentFeature> getDeepSuperComponents() {
        Collection<DasComponentFeature> deepSuperComponents = new ArrayList<DasComponentFeature>();
        for (DasComponentFeature component : getSuperComponents()) {
            deepSuperComponents.add(component);
            deepSuperComponents.addAll(component.getSuperComponents());
        }
        return deepSuperComponents;
    }

    public boolean isTypeIsReference() {
        return true;
    }
}
