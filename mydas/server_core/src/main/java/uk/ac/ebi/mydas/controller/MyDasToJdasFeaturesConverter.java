/*
 * Copyright 2012 jw12.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.mydas.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.biodas.jdas.creators.CreateFeatures;
import org.biodas.jdas.schema.features.*;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

import uk.ac.ebi.mydas.model.*;

/**
 *
 * @author jw12
 */
class MyDasToJdasFeaturesConverter {

    CreateFeatures createFeatures = new CreateFeatures();

    public DASGFF convertMyDasFeatureDataToJDAS(Collection<SegmentReporter> segmentReporterCollections, String requestHref, DasFeatureRequestFilter filter, boolean categorize, boolean isFeaturesStrictlyEnclosed, boolean isUseFeatureIdForFeatureLabel)
            throws IllegalArgumentException, IllegalStateException, IOException, DataSourceException {
        System.out.println("creating json");

        //TODO serializer.attribute(DAS_XML_NAMESPACE, "href", buildRequestHref(request));
        List<Object> segments = new ArrayList();
        for (SegmentReporter segmentReporter : segmentReporterCollections) {

            if (segmentReporter instanceof UnknownSegmentReporter) {
                //((UnknownSegmentReporter)segmentReporter).serialize(DAS_XML_NAMESPACE, serializer, referenceSource);
                UNKNOWNSEGMENT unknownSeg = new UNKNOWNSEGMENT();
                unknownSeg.setId(segmentReporter.getSegmentId());
                unknownSeg.setStart(BigInteger.valueOf(segmentReporter.getStart()));
                unknownSeg.setStop(BigInteger.valueOf(segmentReporter.getStop()));
                segments.add(unknownSeg);

            } else if (segmentReporter instanceof ErrorSegmentReporter) { //since 1.6.1
                //((ErrorSegmentReporter)segmentReporter).serialize(DAS_XML_NAMESPACE, serializer);
                ERRORSEGMENT errorSeg = new ERRORSEGMENT();
                errorSeg.setId(segmentReporter.getSegmentId());
                errorSeg.setStart(BigInteger.valueOf(segmentReporter.getStart()));
                errorSeg.setStop(BigInteger.valueOf(segmentReporter.getStop()));
                segments.add(errorSeg);
            } else if (segmentReporter instanceof UnknownFeatureSegmentReporter) {
                //((UnknownFeatureSegmentReporter)segmentReporter).serialize(DAS_XML_NAMESPACE, serializer);
                UNKNOWNFEATURE unknownFeature = new UNKNOWNFEATURE();
                unknownFeature.setId(segmentReporter.getSegmentId());
                unknownFeature.setStart(String.valueOf(segmentReporter.getStart()));
                unknownFeature.setStop(String.valueOf(segmentReporter.getStop()));
                segments.add(unknownFeature);
            } else {
                //Overlaps are always allowed (since 1.6.1, according to DAS spec 1.6, draft 6)
                //featuresStrictlyEnclosed set to false means that overlaps are allowed
                // ((FoundFeaturesReporter) segmentReporter).serialize(DAS_XML_NAMESPACE, serializer, filter, categorize, false, dsnConfig.isUseFeatureIdForFeatureLabel());
                //convert succesful foundFeatures Reporter to jdas model  then write json here
                BigInteger total=null;
                if(filter.isPaginated() && filter.getTotalFeatures()!=null){
                    total=BigInteger.valueOf(filter.getTotalFeatures());
                }
        
                SEGMENT segment = this.createFeaturesSegment((FoundFeaturesReporter) segmentReporter, requestHref, filter, categorize, isFeaturesStrictlyEnclosed, isUseFeatureIdForFeatureLabel, total);
                segments.add(segment);
            }

        }
        DASGFF dasGff = createFeatures.createDASGFF(segments, requestHref);
        return dasGff;
    }

    private SEGMENT createFeaturesSegment(FoundFeaturesReporter segmentReporter, String DAS_XML_NAMESPACE, DasFeatureRequestFilter filter, boolean categorize, boolean isFeaturesStrictlyEnclosed, boolean isUseFeatureIdForFeatureLabel, BigInteger total)
            throws IllegalArgumentException, IllegalStateException, IOException, DataSourceException {



        // serializer.startTag(DAS_XML_NAMESPACE, "SEGMENT");

        //serializer.attribute(DAS_XML_NAMESPACE, "id", this.getSegmentId());
        String segmentId = segmentReporter.getSegmentId();
        String segStart = null;
        String segStop = null;
        String segmentVersion = null;
        String segmentLabel = null;
        //start and stop are an optional group
        if ((segmentReporter.getStart() != null) && (segmentReporter.getStop() != null)) {
            // serializer.attribute(DAS_XML_NAMESPACE, "start", Integer.toString(this.getStart()));
            //serializer.attribute(DAS_XML_NAMESPACE, "stop", Integer.toString(this.getStop()));
            segStart = Integer.toString(segmentReporter.getStart());
            segStop = Integer.toString(segmentReporter.getStop());
        }
        if (segmentReporter.getTotalFeatures() != null) {
            total = BigInteger.valueOf(segmentReporter.getTotalFeatures());
        }

        //removed the type of segment as this is not in 1.6 anymore - chromosome etc is from the dascoordinatesystem
//		if (segmentReporter.getType() != null && segmentReporter.getType().length() > 0){
//			segment.serializer.attribute(DAS_XML_NAMESPACE, "type", segmentReporter.getType());
//		}
        //version is optional in DAS 1.6
        if (segmentReporter.getVersion() != null) {
            segmentVersion = segmentReporter.getVersion();
        }

        if (segmentReporter.getSegmentLabel() != null && segmentReporter.getSegmentLabel().length() > 0) {
            segmentLabel = segmentReporter.getSegmentLabel();
        }
        List<FEATURE> features = new ArrayList<FEATURE>();
        for (DasFeature feature : segmentReporter.getFeatures()) {
            boolean hasSuperParts = false;
            boolean hasSubParts = false;
            if (feature instanceof DasComponentFeature) {
                DasComponentFeature refFeature = (DasComponentFeature) (DasFeature) feature;
                hasSuperParts = refFeature.hasSuperParts();
                hasSubParts = refFeature.hasSubParts();
            }
            FEATURE jdasFeature = this.createFeature(feature, DAS_XML_NAMESPACE, filter, categorize, isUseFeatureIdForFeatureLabel, feature instanceof DasComponentFeature, hasSuperParts, hasSubParts);
            if (jdasFeature != null) {
                features.add(jdasFeature);
            }
        }
        SEGMENT seg= createFeatures.createSegment(segmentId, segmentVersion, segStart, segStop, features);
        if(segmentLabel!=null){
            seg.setLabel(segmentLabel);
        }
        return seg;
    }

    public FEATURE createFeature(DasFeature feature, String DAS_XML_NAMESPACE, DasFeatureRequestFilter filter, boolean categorize, boolean isUseFeatureIdForFeatureLabel, boolean hasReferences, boolean hasSuperParts, boolean hasSubParts)
            throws IllegalArgumentException, IllegalStateException, IOException, DataSourceException {
        // Check the feature passes the filter.
        FEATURE jdasFeature = null;
        if (filter.featurePasses(feature)) {
            //serializer.startTag(DAS_XML_NAMESPACE, "FEATURE");
            //serializer.attribute(DAS_XML_NAMESPACE, "id", this.getFeatureId());
            jdasFeature = new FEATURE();
            if (feature.getFeatureLabel() != null && feature.getFeatureLabel().length() > 0) {
                jdasFeature.setLabel(feature.getFeatureLabel());
            } else if (isUseFeatureIdForFeatureLabel) {
                jdasFeature.setLabel(feature.getFeatureId());
            }

            // TYPE element
            TYPE jdasType = this.createType(feature.getType(), DAS_XML_NAMESPACE, null, hasReferences, hasSubParts, hasSuperParts, true);
            jdasFeature.setTYPE(jdasType);
            // METHOD element
            //(new DasMethodE(this.getMethod())).serialize(DAS_XML_NAMESPACE, serializer);
            METHOD method = this.createMethod(feature.getMethod());
            jdasFeature.setMETHOD(method);
            //DAS1.6 START and END are optional for the cases of non positional features
            //start and stop are an optional group
            if ((feature.getStartCoordinate() != 0) && (feature.getStopCoordinate() != 0)) {
                // START element
                // serializer.startTag(DAS_XML_NAMESPACE, "START");
                // serializer.text(Integer.toString(this.getStartCoordinate()));

                jdasFeature.setSTART(BigInteger.valueOf(feature.getStartCoordinate()));
                //serializer.endTag(DAS_XML_NAMESPACE, "START");

                // END element
                //serializer.startTag(DAS_XML_NAMESPACE, "END");
                jdasFeature.setEND(BigInteger.valueOf(feature.getStopCoordinate()));
                //serializer.endTag(DAS_XML_NAMESPACE, "END");
            }

            // SCORE element
            // DAS 1.6: The value of - is assumed if the tag is omitted entirely. therefore it is optional.
            if (feature.getScore() != null) {
                jdasFeature.setSCORE(Double.toString(feature.getScore()));
            }
            // ORIENTATION element
            // DAS 1.6: The value of 0 is assumed if the tag is omitted entirely. therefore it is optional.
            if ((feature.getOrientation() != null) && (feature.getOrientation() != DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE)) {
                jdasFeature.setORIENTATION(feature.getOrientation().toString());
            }
            // PHASE element
            // DAS 1.6: The value of - is assumed if the tag is omitted entirely. therefore it is optional.
            if ((feature.getPhase() != null) && (feature.getPhase() != DasPhase.PHASE_NOT_APPLICABLE)) {
                jdasFeature.setPHASE(feature.getPhase().toString());
            }

            // NOTE elements
            if (feature.getNotes() != null) {
                for (String note : feature.getNotes()) {
                    jdasFeature.getNOTE().add(note);
                }
            }

            // LINK elements
            if (feature.getLinks() != null) {
                for (URL url : feature.getLinks().keySet()) {
                    if (url != null) {
                        LINK link = new LINK();
                        // serializer.attribute(DAS_XML_NAMESPACE, "href", this.getHref().toString());
                        String linkText = feature.getLinks().get(url);
                        if (linkText != null && linkText.length() > 0) {
                            link.setHref(url.toString());
                            link.setContent(linkText);
                        }
                        jdasFeature.getLINK().add(link);


                    }
                }
            }

            // TARGET elements
            if (feature.getTargets() != null) {
                for (DasTarget target : feature.getTargets()) {

                    TARGET jdasTarget = new TARGET();
                    jdasTarget.setId(target.getTargetId());
                    jdasTarget.setStart(BigInteger.valueOf(target.getStartCoordinate()));
                    jdasTarget.setStop(BigInteger.valueOf(target.getStopCoordinate()));
                    if (target.getTargetName() != null && target.getTargetName().length() > 0) {
                        jdasTarget.setContent(target.getTargetName());
                    }
                    jdasFeature.getTARGET().add(jdasTarget);
                }
            }

            if (feature.getParents() != null) {
                for (String parent : feature.getParents()) {
                    PARENT jdasParent = new PARENT();
                    jdasParent.setId(parent);
                    jdasFeature.getPARENT().add(jdasParent);
                }
            }
            if (feature.getParts() != null) {
                for (String part : feature.getParts()) {
                    PART jdasPart = new PART();
                    jdasPart.setId(part);
                    jdasFeature.getPART().add(jdasPart);
                }
            }
            return jdasFeature;
        }
        return null;//no feature created as not passed filter

    }

    private TYPE createType(DasType dasType, String DAS_XML_NAMESPACE, Integer count, boolean hasReferenceFeatures, boolean hasSubParts, boolean hasSuperParts, boolean featuresCommand)
            throws IllegalArgumentException, IllegalStateException, IOException {
        //categorize indicates if the categories will be included in the type of the feature --> this parameter (right next to count) was removed since 1.6.1
        TYPE jdasType = new TYPE();
        //serializer.startTag(DAS_XML_NAMESPACE, "TYPE");
        jdasType.setId(dasType.getId());
        if (dasType.getCvId() != null && dasType.getCvId().length() > 0) {
            jdasType.setCvId(dasType.getCvId());
        }
        // Handle DasReferenceFeatures.
        if (hasReferenceFeatures) {
            //serializer.attribute(DAS_XML_NAMESPACE, "reference", "yes");
            jdasType.setReference("yes");
            //serializer.attribute(DAS_XML_NAMESPACE, "superparts", (hasSuperParts) ? "yes" : "no");
            jdasType.setSuperparts((hasSuperParts) ? "yes" : "no");
            jdasType.setSubparts((hasSubParts) ? "yes" : "no");
        }
        //if (categorize){
        if (dasType.getCategory() != null && dasType.getCategory().length() > 0) {
            jdasType.setCategory(dasType.getCategory());
        } //else {
        // To prevent the DAS server from dying, if no category has been set, but
        // a category is required, spit out the type ID again as the category.
        //serializer.attribute(DAS_XML_NAMESPACE, "category", this.getId());
        //}
        //}

        if (featuresCommand) {
            //Tag content should be the label
            if (dasType.getLabel() != null && dasType.getLabel().length() > 0) {
                jdasType.setContent(dasType.getLabel());
            }
        } else {
            //Tag content should be the count
            if (count != null) {
                jdasType.setContent(Integer.toString(count));
            }
        }

        return jdasType;

    }

    private METHOD createMethod(DasMethod dasMethod) {
        METHOD method = new METHOD();
        //serializer.startTag(DAS_XML_NAMESPACE, "METHOD");
        if (dasMethod.getId() != null && dasMethod.getId().length() > 0) {
            method.setId(dasMethod.getId());
        }
        if (dasMethod.getCvId() != null && dasMethod.getCvId().length() > 0) {
            method.setCvId(dasMethod.getCvId());
        }
        if (dasMethod.getLabel() != null && dasMethod.getLabel().length() > 0) {
            method.setContent(dasMethod.getLabel());
        }
        //serializer.endTag(DAS_XML_NAMESPACE, "METHOD");
        return method;
    }
}
