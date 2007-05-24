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
 *
 * This class holds all of the information required to fully populate
 * a /DASGFF/GFF/SEGMENT/FEATURE element returned from the features
 * command, and also provides information used to build the response
 * to the types command.  Some fields are optional and can be populated
 * using <code>null</code> as described in the JavaDoc for the
 * constructor.
 */
public class DasFeature {

    /**
     * <b>Mandatory</b>
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/@id
     */
    protected final String featureId;

    /**
     * <b>Optional</b>
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/@label.
     *
     * Note that in the XML configuration for your data source,
     * you may indicate that the featureId should be used to
     * populate this attribute if no featureLabel is provided.
     */
    protected final String featureLabel;

    /**
     * <b>Mandatory</b>
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/TYPE/@id
     *
     * For the types command, provides the value for
     * /DASTYPES/GFF/SEGMENT/TYPE/@id
     */
    protected final String typeId;

    /**
     *  <b>Optional</b> but recommended
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/TYPE/@category
     *
     * For the types command, provides the value for
     * /DASTYPES/GFF/SEGMENT/TYPE/@category
     */
    protected final String typeCategory;

    /**
     *  <b>Optional</b>
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/TYPE
     */
    protected final String typeLabel;

    /**
     *  <b>Optional</b> but highly recommended!
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/METHOD/@id
     */
    protected final String methodId;

    /**
     *  <b>Optional</b>
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/METHOD
     */
    protected final String methodLabel;

    /**
     * <b>Mandatory</b>
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/START
     *
     * Must be equal to or less than endCoordinate.
     *
     * <i>DAS servers are often required to serve non-positional features, such as descriptions (of the entire
     * segment) or related citations.  A commonly accepted mechanism is to give non-positional features start
     * and end coordinates of 0, however this is not enforced and is not part of the DAS 1.53 specification.</i>
     */
    protected final int startCoordinate;

    /**
     * <b>Mandatory</b>
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/END
     *
     * Must be equal to or greater than startCoordinate.
     *
     * <i>DAS servers are often required to serve non-positional features, such as descriptions (of the entire
     * segment) or related citations.  A commonly accepted mechanism is to give non-positional features start
     * and end coordinates of 0, however this is not enforced and is not part of the DAS 1.53 specification.</i>
     */
    protected final int endCoordinate;

    /**
     * <b>Mandatory</b>
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/SCORE
     */
    protected final Double score;

    /**
     * <b>Mandatory</b>
     *
     * Select a value from:
     * <ul>
     * <li>DasFeature.ORIENTATION_NOT_APPLICABLE</li>
     * <li>DasFeature.ORIENTATION_SENSE_STRAND</li>
     * <li>DasFeature.ORIENTATION_ANTISENSE_STRAND</li>
     * </ul>
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/ORIENTATION
     */
    protected final String orientation;

    /**
     * <b>Mandatory</b>
     *
     * Select a value from:
     * DasFeature.PHASE_READING_FRAME_0
     * DasFeature.PHASE_READING_FRAME_1
     * DasFeature.PHASE_READING_FRAME_2
     * DasFeature.PHASE_NOT_APPLICABLE
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/PHASE
     */
    protected final String phase;

    /**
     * <b>Optional</b> - May be <code>null</code>.
     *
     * For the features command, provides the values for
     * any number of
     * /DASGFF/GFF/SEGMENT/FEATURE/NOTE
     * elements.
     */
    protected final Collection<String> notes;

    /**
     * <b>Optional</b> - May be <code>null</code>.
     *
     * For the features command, provides the values for
     * any number of
     * /DASGFF/GFF/SEGMENT/FEATURE/LINK
     * elements, where the Map key is the
     * /DASGFF/GFF/SEGMENT/FEATURE/LINK/@href attribute
     * and the Map value is the
     * /DASGFF/GFF/SEGMENT/FEATURE/LINK element value.
     */
    protected final Map<URL, String> links;

    /**
     * <b>Optional</b> - May be <code>null</code>.
     *
     * For the features command, provides the values for
     * any number of
     * /DASGFF/GFF/SEGMENT/FEATURE/TARGET
     * elements.
     *
     * See the documentation of the {@link DasTarget} class
     * for details of how this class maps to the DAS XML.
     */
    protected final Collection<DasTarget> targets;

    /**
     * <b>Optional</b> - May be <code>null</code>.
     *
     * For the features command, provides the values for
     * any number of
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP
     * elements.
     *
     * See the documentation of the {@link DasGroup} class
     * for details of how this class maps to the DAS XML.
     */
    protected final Collection<DasGroup> groups;

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

    /**
     *
     * @param featureId <b>Mandatory</b> For the features command, provides the value for /DASGFF/GFF/SEGMENT/FEATURE/@id
     * @param featureLabel <b>Optional</b> (May be null)For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/@label. Note that in the XML configuration for your data source,
     * you may indicate that the featureId should be used to populate this attribute if no featureLabel is provided.
     * @param typeId <b>Mandatory</b> For the features command, provides the value for /DASGFF/GFF/SEGMENT/FEATURE/TYPE/@id
     * For the types command, provides the value for /DASTYPES/GFF/SEGMENT/TYPE/@id
     * @param typeCategory <b>Optional</b> but recommended. For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/TYPE/@category For the types command, provides the value for
     * /DASTYPES/GFF/SEGMENT/TYPE/@category
     * @param typeLabel <b>Optional</b> For the features command, provides the value for /DASGFF/GFF/SEGMENT/FEATURE/TYPE
     * @param methodId <b>Optional</b> but highly recommended! For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/METHOD/@id
     * @param methodLabel <b>Optional</b> For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/METHOD
     * @param startCoordinate <b>Mandatory</b> For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/START.  Must be equal to or less than endCoordinate.
     * <i>DAS servers are often required to serve non-positional features, such as descriptions (of the entire
     * segment) or related citations.  A commonly accepted mechanism is to give non-positional features start
     * and end coordinates of 0, however this is not enforced and is not part of the DAS 1.53 specification.</i>
     * @param endCoordinate <b>Mandatory</b> For the features command, provides the value for /DASGFF/GFF/SEGMENT/FEATURE/END
     * Must be equal to or greater than startCoordinate.
     * <i>DAS servers are often required to serve non-positional features, such as descriptions (of the entire
     * segment) or related citations.  A commonly accepted mechanism is to give non-positional features start
     * and end coordinates of 0, however this is not enforced and is not part of the DAS 1.53 specification.</i>
     * @param score <b>Mandatory</b> For the features command, provides the value for /DASGFF/GFF/SEGMENT/FEATURE/SCORE
     * @param orientation <b>Mandatory</b> Select a value from:
     * <ul>
     * <li>DasFeature.ORIENTATION_NOT_APPLICABLE</li>
     * <li>DasFeature.ORIENTATION_SENSE_STRAND</li>
     * <li>DasFeature.ORIENTATION_ANTISENSE_STRAND</li>
     * </ul>
     * For the features command, provides the value for /DASGFF/GFF/SEGMENT/FEATURE/ORIENTATION
     * @param phase <b>Mandatory</b>
     * Select a value from:
     * <ul>
     * <li>DasFeature.PHASE_READING_FRAME_0</li>
     * <li>DasFeature.PHASE_READING_FRAME_1</li>
     * <li>DasFeature.PHASE_READING_FRAME_2</li>
     * <li>DasFeature.PHASE_NOT_APPLICABLE</li>
     * </ul>
     * For the features command, provides the value for /DASGFF/GFF/SEGMENT/FEATURE/PHASE
     * @param notes <b>Optional</b> - May be <code>null</code>. For the features command, provides the values for
     * any number of  /DASGFF/GFF/SEGMENT/FEATURE/NOTE elements.
     * @param links <b>Optional</b> - May be <code>null</code>. For the features command, provides the values for
     * any number of /DASGFF/GFF/SEGMENT/FEATURE/LINK elements, where the Map key is the
     * /DASGFF/GFF/SEGMENT/FEATURE/LINK/@href attribute and the Map value is the
     * /DASGFF/GFF/SEGMENT/FEATURE/LINK element value.
     * @param targets <b>Optional</b> - May be <code>null</code>. For the features command, provides the values for
     * any number of /DASGFF/GFF/SEGMENT/FEATURE/TARGET elements.
     * See the documentation of the {@link DasTarget} class for details of how this class maps to the DAS XML.
     * @param groups <b>Optional</b> - May be <code>null</code>. For the features command, provides the values for
     * any number of /DASGFF/GFF/SEGMENT/FEATURE/GROUPelements. See the documentation of the {@link DasGroup} class
     * for details of how this class maps to the DAS XML.
     * @throws DataSourceException to wrap any Exceptions thrown if this object is not constructed correctly.
     */
    public DasFeature(String featureId,
                      String featureLabel,
                      String typeId,
                      String typeCategory,
                      String typeLabel,
                      String methodId,
                      String methodLabel,
                      int startCoordinate,
                      int endCoordinate,
                      Double score,
                      String orientation,
                      String phase,
                      Collection<String> notes,
                      Map<URL, String> links,
                      Collection<DasTarget> targets,
                      Collection<DasGroup> groups) throws DataSourceException {

        if (featureId == null || typeId == null){
            throw new DataSourceException ("An attempt to instantiate a DasFeature object without the minimal required mandatory values.");
        }
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
        this.methodId = methodId;
        this.methodLabel = methodLabel;
        this.startCoordinate = startCoordinate;
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

    public String getMethodId() {
        return methodId;
    }

    public String getMethodLabel() {
        return methodLabel;
    }

    public int getStartCoordinate() {
        return startCoordinate;
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


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DasFeature that = (DasFeature) o;

        if (endCoordinate != that.endCoordinate) return false;
        if (startCoordinate != that.startCoordinate) return false;
        if (!featureId.equals(that.featureId)) return false;
        if (featureLabel != null ? !featureLabel.equals(that.featureLabel) : that.featureLabel != null) return false;
        if (groups != null ? !groups.equals(that.groups) : that.groups != null) return false;
        if (links != null ? !links.equals(that.links) : that.links != null) return false;
        if (methodId != null ? !methodId.equals(that.methodId) : that.methodId != null) return false;
        if (methodLabel != null ? !methodLabel.equals(that.methodLabel) : that.methodLabel != null) return false;
        if (notes != null ? !notes.equals(that.notes) : that.notes != null) return false;
        if (!orientation.equals(that.orientation)) return false;
        if (!phase.equals(that.phase)) return false;
        if (!score.equals(that.score)) return false;
        if (targets != null ? !targets.equals(that.targets) : that.targets != null) return false;
        if (typeCategory != null ? !typeCategory.equals(that.typeCategory) : that.typeCategory != null) return false;
        if (!typeId.equals(that.typeId)) return false;
        if (typeLabel != null ? !typeLabel.equals(that.typeLabel) : that.typeLabel != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = featureId.hashCode();
        result = 31 * result + (featureLabel != null ? featureLabel.hashCode() : 0);
        result = 31 * result + typeId.hashCode();
        result = 31 * result + (typeCategory != null ? typeCategory.hashCode() : 0);
        result = 31 * result + (typeLabel != null ? typeLabel.hashCode() : 0);
        result = 31 * result + (methodId != null ? methodId.hashCode() : 0);
        result = 31 * result + (methodLabel != null ? methodLabel.hashCode() : 0);
        result = 31 * result + startCoordinate;
        result = 31 * result + endCoordinate;
        result = 31 * result + score.hashCode();
        result = 31 * result + orientation.hashCode();
        result = 31 * result + phase.hashCode();
        result = 31 * result + (notes != null ? notes.hashCode() : 0);
        result = 31 * result + (links != null ? links.hashCode() : 0);
        result = 31 * result + (targets != null ? targets.hashCode() : 0);
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        return result;
    }
}
