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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.biodas.jdas.creators.CreateEntryPoints;
import org.biodas.jdas.schema.entryPoints.DASEP;
import org.biodas.jdas.schema.entryPoints.SEGMENT;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.datasource.AnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.BadCommandArgumentsException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.extendedmodel.DasEntryPointE;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasEntryPointOrientation;

/**
 *
 * @author jw12
 */
class MyDasToJdasEntryPointsConverter {

    CreateEntryPoints instance = new CreateEntryPoints();

    DASEP entryPointsCommand(String href, AnnotationDataSource refDsn, Integer start, Integer stop, HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
            throws IOException, DataSourceException, UnimplementedFeatureException, BadCommandArgumentsException {
        //TODO replace this header with a json one?
        //writeHeader (request, response, XDasStatus.STATUS_200_OK, true, dsnConfig.getCapabilities());
        //OK, got our entry points, so write out the XML.

        Collection<DasEntryPoint> entryPoints = refDsn.getEntryPoints(start, stop);
        System.out.println("createEntryPoints");
        String version = "1.0";//default version if not specified

        BigInteger totalEntryPoints = null;
        BigInteger firstInList = null;
        BigInteger lastInList = null;





        if (refDsn.getEntryPointVersion() != null) {
            version = refDsn.getEntryPointVersion();
            totalEntryPoints = BigInteger.valueOf(refDsn.getTotalEntryPoints());
        }



        if (start != null) {
            firstInList = BigInteger.valueOf(start);
        }


//				else {
//					start = 1;
//                    serializer.attribute(DAS_XML_NAMESPACE, "start", ""+start);
//                }

        if (stop != null) {
            Integer expectedSize = stop - start + 1;
            if (expectedSize == entryPoints.size()) { //From start to stop was returned, check that it is in accordance to max_entry_points
                if (dsnConfig.getMaxEntryPoints() != null) {
                    if (dsnConfig.getMaxEntryPoints() < entryPoints.size()) {
                        stop = start + dsnConfig.getMaxEntryPoints() - 1;
                    }
                }
            } else { //Less elements were returned, could be because less elements actually exist or because of max_entry_points
                if (dsnConfig.getMaxEntryPoints() != null) {
                    if (dsnConfig.getMaxEntryPoints() < entryPoints.size()) {
                        stop = start + dsnConfig.getMaxEntryPoints() - 1;
                    } else if (entryPoints.size() == 0) {
                        //Both start ans stop where out of limits, do not change the stop variable
                    } else {
                        stop = start + entryPoints.size() - 1;
                    }
                } else {
                    if (entryPoints.size() != 0) {
                        stop = start + entryPoints.size() - 1;
                    }
                }
            }

        }
//				else {
//                    stop = dsnConfig.getMaxEntryPoints() == null ? entryPoints.size() : Math.min(dsnConfig.getMaxEntryPoints(), entryPoints.size());
//                    serializer.attribute(DAS_XML_NAMESPACE, "end", ""+stop);
//                }

        Iterator<DasEntryPoint> iterator = entryPoints.iterator();
        /*
         * for (int i=1; i<start ;i++)
					iterator.next();
         */
        //DasEntryPoint[] entryPointsA = (DasEntryPoint[]) entryPoints.toArray();
        // Now for the individual segments.
        List<SEGMENT> segments = new ArrayList<SEGMENT>();
        for (int i = start; (i <= stop) && (iterator.hasNext()); i++) {
            DasEntryPoint entryPoint = iterator.next();
            if (entryPoint != null) {
                //(new DasEntryPointE(entryPoint)).serialize(DAS_XML_NAMESPACE, serializer);
                SEGMENT seg = this.createSegmentFromMyDASEntryPoint(entryPoint);
                segments.add(seg);
            }
        }

        DASEP result = instance.createEntryPoints(version, href, totalEntryPoints, firstInList, lastInList, segments);
        return result;
    }

    private SEGMENT createSegmentFromMyDASEntryPoint(DasEntryPoint myDasEntrPointObject) {
        //start and stop are an optional group
        SEGMENT seg = new SEGMENT();
        if ((myDasEntrPointObject.getStartCoordinate() != null) && (myDasEntrPointObject.getStopCoordinate() != null)) {
            seg.setStart(BigInteger.valueOf(myDasEntrPointObject.getStartCoordinate()));
            seg.setStop(BigInteger.valueOf(myDasEntrPointObject.getStopCoordinate()));
        }
        if (myDasEntrPointObject.getVersion() != null && myDasEntrPointObject.getVersion().length() > 0) {
            seg.setVersion(myDasEntrPointObject.getVersion());
        }
        if (myDasEntrPointObject.getType() != null && myDasEntrPointObject.getType().length() > 0) {
            seg.setType(myDasEntrPointObject.getType());
        }
        if (!myDasEntrPointObject.getOrientation().equals(DasEntryPointOrientation.NO_INTRINSIC_ORIENTATION)) {
            seg.setOrientation(myDasEntrPointObject.getOrientation().toString());
        }
        if (myDasEntrPointObject.hasSubparts()) {
            seg.setSubparts("yes");
        }
        if (myDasEntrPointObject.getDescription() != null && myDasEntrPointObject.getDescription().length() > 0) {
            seg.setContent(myDasEntrPointObject.getDescription());
        }

        return seg;
    }
}
