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

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 14:20:35
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class DasFeature {

    private final String featureId;

    private final String featureLabel;

    private final String typeId;

    private final String typeCategory;

    private final String typeLabel;

    private final boolean typeIsReference;

    private final String methodId;

    private final String methodLabel;

    /*
     * <i>DAS servers are often required to serve non-positional features, such as descriptions (of the entire
     * segment) or related citations.  A commonly accepted mechanism is to give non-positional features start
     * and end coordinates of 0, however this is not enforced and is not part of the DAS 1.53 specification.</i>
     */
    private final int startCoodinate;

    private final int endCoordinate;

    private final Double score;

    private final String orientation;

    private final String phase;

    private final Collection<String> notes;

    private final Map<URL, String> links;

    private final Collection<DasTarget> targets;

    private final Collection<DasGroup> groups;

    public static final String ORIENTATION_NOT_APPLICABLE = "0";
    public static final String ORIENTATION_SENSE_STRAND = "+";
    public static final String ORIENTATION_ANTISENSE_STRAND = "-";

    public static final String PHASE_READING_FRAME_0 = "0";
    public static final String PHASE_READING_FRAME_1 = "1";
    public static final String PHASE_READING_FRAME_2 = "2";
    public static final String PHASE_NOT_APPLICABLE = "-";

    private static final List<String> VALID_ORIENTATIONS = new ArrayList<String>(3);
    private static final List<String> VALID_PHASES = new ArrayList<String>(4);

    static {
        VALID_ORIENTATIONS.add(ORIENTATION_NOT_APPLICABLE);
        VALID_ORIENTATIONS.add(ORIENTATION_ANTISENSE_STRAND);
        VALID_ORIENTATIONS.add(ORIENTATION_SENSE_STRAND);

        VALID_PHASES.add(PHASE_NOT_APPLICABLE);
        VALID_PHASES.add(PHASE_READING_FRAME_0);
        VALID_PHASES.add(PHASE_READING_FRAME_1);
        VALID_PHASES.add(PHASE_READING_FRAME_2);
    }


    public DasFeature(String featureId,
                      String featureLabel,
                      String typeId,
                      String typeCategory,
                      String typeLabel,
                      boolean typeIsReference,
                      String methodId,
                      String methodLabel,
                      int startCoodinate,
                      int endCoordinate,
                      Double score,
                      String orientation,
                      String phase,
                      Collection<String> notes,
                      Map<URL, String> links,
                      Collection<DasTarget> targets,
                      Collection<DasGroup> groups) throws DataSourceException {

        if (orientation == null) {
            orientation = ORIENTATION_NOT_APPLICABLE;
        }
        if (phase == null){
            phase = PHASE_NOT_APPLICABLE;
        }

        if (! VALID_ORIENTATIONS.contains(orientation)){
            throw new DataSourceException ("An attempt to instantiate a DasFeature object with an invalid orientation has been detected.");
        }
        if (! VALID_PHASES.contains(phase)){
            throw new DataSourceException ("An attempt to instantiate a DasFeature object with an invalid phase has been detected.");
        }
        this.featureId = featureId;
        this.featureLabel = featureLabel;
        this.typeId = typeId;
        this.typeCategory = typeCategory;
        this.typeLabel = typeLabel;
        this.typeIsReference = typeIsReference;
        this.methodId = methodId;
        this.methodLabel = methodLabel;
        this.startCoodinate = startCoodinate;
        this.endCoordinate = endCoordinate;
        this.score = score;
        this.orientation = orientation;
        this.phase = phase;
        this.notes = notes;
        this.links = links;
        this.targets = targets;
        this.groups = groups;
    }


    public String getFeatureId() {
        return featureId;
    }

    public String getFeatureLabel() {
        return featureLabel;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getTypeCategory() {
        return typeCategory;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public boolean isTypeIsReference() {
        return typeIsReference;
    }

    public String getMethodId() {
        return methodId;
    }

    public String getMethodLabel() {
        return methodLabel;
    }

    public int getStartCoordinate() {
        return startCoodinate;
    }

    public int getStopCoordinate() {
        return endCoordinate;
    }

    public Double getScore() {
        return score;
    }

    public String getOrientation() {
        return orientation;
    }

    public String getPhase() {
        return phase;
    }

    public Collection<String> getNotes() {
        return notes;
    }

    public Map<URL, String> getLinks() {
        return links;
    }

    public Collection<DasTarget> getTargets() {
        return targets;
    }

    public Collection<DasGroup> getGroups() {
        return groups;
    }
}
