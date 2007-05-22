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

package uk.ac.ebi.mydas.controller;

/**
 * Created Using IntelliJ IDEA.
 * Date: 21-May-2007
 * Time: 17:59:48
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * This interface is used to report either a successfully found segment with features (using the FeatureReporter
 * implementation), or a missing segment (using the UnknownSegmentReporter).  This is to allow the errorsegment /
 * unknownsegment response to be built.
 */
public interface SegmentReporter {

    /**
     * The start coordinate:
     * <ul>
     *     <li>As requested, if the start coordinate was specified in the request.</li>
     *     <li>If not requested, the actual startcoordinates of the segment returned from the dsn </li>
     *     <li>null if no coordinates were requested and the segment has not been found.</li>
     * </ul>
     * @return the start coordinate to be reported.
     */
    public Integer getStart();

    /**
     * The stop coordinate:
     * <ul>
     *     <li>As requested, if the stop coordinate was specified in the request.</li>
     *     <li>If not requested, the actual stop coordinates of the segment returned from the dsn </li>
     *     <li>null if no coordinates were requested and the segment has not been found.</li>
     * </ul>
     * @return the stop coordinate to be reported.
     */
    public Integer getStop();

    /**
     * The id of the segment.
     * @return the id of the segment.
     */
    public String getSegmentId();
}
