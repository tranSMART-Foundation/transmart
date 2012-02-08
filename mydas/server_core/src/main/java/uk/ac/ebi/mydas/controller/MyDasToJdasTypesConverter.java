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

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.biodas.jdas.creators.CreateTypes;
import org.biodas.jdas.schema.types.*;
import org.xmlpull.v1.XmlSerializer;
import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.datasource.ReferenceDataSource;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.extendedmodel.DasTypeE;
import uk.ac.ebi.mydas.model.DasType;

/**
 *
 * @author jw12
 */
class MyDasToJdasTypesConverter {

    CreateTypes createTypes = new CreateTypes();
    ObjectFactory factory = new ObjectFactory();

    public DASTYPES convertMyDasToJdasTypesAll(Map<DasType, Integer> allTypesReport, String href) {

//        TYPE type1 = createTypes.createType("BS:01006", "BS:01006", "comment", null, null);
//        TYPE type2 = createTypes.createType("BS:01025", "BS:01025", "Comment", null, null);
//        SEGMENT seg1 = createTypes.createSegment(null, "Complete datasource summary", null, null, null);
        List<Object> segments = new ArrayList();
//        segments.add(seg1);

        SEGMENT seg1 = createTypes.createSegment(null, "Complete datasource summary", "1.0", null, null);


        // Iterate over the allTypeReport for the TYPE elements.
        for (DasType type : allTypesReport.keySet()) {
            //(new DasTypeE(type)).serialize(DAS_XML_NAMESPACE, serializer, allTypesReport.get(type), false);
            TYPE jdasType = this.createTypeFromMyDasType(type, allTypesReport.get(type));
            seg1.getTYPE().add(jdasType);
        }


        segments.add(seg1);

        DASTYPES dasTypes = createTypes.createDASTYPES(segments, href);
        return dasTypes;
    }

    DASTYPES convertMyDasToJdasSegmentedTypes(Map<SegmentReporter, Map<DasType, Integer>> typesReport, String href, DataSourceConfiguration dsnConfig) throws DataSourceException {

        List<Object> segments = new ArrayList();


        for (SegmentReporter segmentReporter : typesReport.keySet()) {
            //TODO make sure we have updated the jdas types schema to include errorsegment and unknown segments which currently it doesn't have but should
            if (segmentReporter instanceof UnknownSegmentReporter) {
                boolean referenceSource = dsnConfig.getDataSource() instanceof ReferenceDataSource;
                //((UnknownSegmentReporter) segmentReporter).serialize(DAS_XML_NAMESPACE, serializer, referenceSource);

                if (referenceSource) {
                    ERRORSEGMENT err = factory.createERRORSEGMENT();
                    err.setId(segmentReporter.getSegmentId());
                    err.setStart(BigInteger.valueOf(segmentReporter.getStart()));
                    err.setStop(BigInteger.valueOf(segmentReporter.getStop()));
                    segments.add(err);
                } else {
                    UNKNOWNSEGMENT err = factory.createUNKNOWNSEGMENT();
                    err.setId(segmentReporter.getSegmentId());
                    err.setStart(BigInteger.valueOf(segmentReporter.getStart()));
                    err.setStop(BigInteger.valueOf(segmentReporter.getStop()));
                    segments.add(err);
                }

            } else if (segmentReporter instanceof ErrorSegmentReporter) { //since 1.6.1
                ERRORSEGMENT err = factory.createERRORSEGMENT();
                    err.setId(segmentReporter.getSegmentId());
                    err.setStart(BigInteger.valueOf(segmentReporter.getStart()));
                    err.setStop(BigInteger.valueOf(segmentReporter.getStop()));
                    segments.add(err);
            } else if (segmentReporter instanceof FoundFeaturesReporter) {
                FoundFeaturesReporter featureReporter = (FoundFeaturesReporter) segmentReporter;
                SEGMENT seg1 = createTypes.createSegment(featureReporter.getSegmentId(), featureReporter.getSegmentLabel(), featureReporter.getVersion(), Integer.toString(featureReporter.getStart()), Integer.toString(featureReporter.getStop()));

//                    serializer.startTag(DAS_XML_NAMESPACE, "SEGMENT");
//                    serializer.attribute(DAS_XML_NAMESPACE, "id", featureReporter.getSegmentId());
//                    //start and stop are an optional group
//                    if ((featureReporter.getStart() != null) && (featureReporter.getStop() != null)) {
//                        serializer.attribute(DAS_XML_NAMESPACE, "start", Integer.toString(featureReporter.getStart()));
//                        serializer.attribute(DAS_XML_NAMESPACE, "stop", Integer.toString(featureReporter.getStop()));
//                    }
//                    if (featureReporter.getType() != null && featureReporter.getType().length() > 0) {
//                        serializer.attribute(DAS_XML_NAMESPACE, "type", featureReporter.getType());
//                    }
//                    serializer.attribute(DAS_XML_NAMESPACE, "version", featureReporter.getVersion());
//                    if (featureReporter.getSegmentLabel() != null && featureReporter.getSegmentLabel().length() > 0) {
//                        serializer.attribute(DAS_XML_NAMESPACE, "label", featureReporter.getSegmentLabel());
//                    }
                // Now for the types.
                Map<DasType, Integer> typeMap = typesReport.get(featureReporter);
                for (DasType type : typeMap.keySet()) {
                    //(new DasTypeE(type)).serialize(DAS_XML_NAMESPACE, serializer, typeMap.get(type), false);
                    TYPE jdasType = this.createTypeFromMyDasType(type, typeMap.get(type));
                    seg1.getTYPE().add(jdasType);
                }

                //serializer.endTag(DAS_XML_NAMESPACE, "SEGMENT");
                segments.add(seg1);
            }

        }
        DASTYPES dasTypes = createTypes.createDASTYPES(segments, href);
        return dasTypes;

    }

    private TYPE createTypeFromMyDasType(DasType type, Integer typeCount) {
        //categorize indicates if the categories will be included in the type of the feature --> this parameter (right next to count) was removed since 1.6.1
        TYPE jT = factory.createTYPE();
        type.setId(type.getId());
        if (type.getCvId() != null && type.getCvId().length() > 0) {
            jT.setCvId(type.getCvId());
        }
        // Handle DasReferenceFeatures.
        //if (hasReferenceFeatures){
        //I don't see any reference to this in the types part of the spec in 1.6
        //serializer.attribute(DAS_XML_NAMESPACE, "superparts", (hasSuperParts) ? "yes" : "no");
        //serializer.attribute(DAS_XML_NAMESPACE, "subparts", (hasSubParts) ? "yes" : "no");
        //}
        //if (categorize){
        if (type.getCategory() != null && type.getCategory().length() > 0) {
            jT.setCategory(type.getCategory());
        } //else {
        // To prevent the DAS server from dying, if no category has been set, but
        // a category is required, spit out the type ID again as the category.
        //serializer.attribute(DAS_XML_NAMESPACE, "category", this.getId());
        //}
        //}

//        if (featuresCommand) {
//            //Tag content should be the label
//            if (type.getLabel() != null && type.getLabel().length() > 0){
//			    jT.setValue(type.getLabel());
//            }
        // } else {
        //Tag content should be the count
        //if (typeCount != null){
        jT.setValue(Integer.toString(typeCount));
        //}
        //}

        return jT;

    }
}
