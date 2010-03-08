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
import java.io.Serializable;

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
@SuppressWarnings("serial")
public class DasFeature implements Serializable {

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
     * @deprecated
     * <b>Mandatory</b>
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/TYPE/@id
     *
     * For the types command, provides the value for
     * /DASTYPES/GFF/SEGMENT/TYPE/@id
     */
    protected  String typeId;

    /**
     * @deprecated
     *  <b>Optional</b> but recommended
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/TYPE/@category
     *
     * For the types command, provides the value for
     * /DASTYPES/GFF/SEGMENT/TYPE/@category
     */
    protected  String typeCategory;

    /**
     * @deprecated
     *  <b>Optional</b>
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/TYPE
     */
    protected  String typeLabel;

    /**
     * @deprecated
     *  <b>Optional</b> but highly recommended!
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/METHOD/@id
     */
    protected  String methodId;

    /**
     * @deprecated
     *  <b>Optional</b>
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/METHOD
     */
    protected  String methodLabel;

    
    /**
     * 
     * Created for DAS1.6
     * <b>Mandatory</b>
     * 
     * Each feature has just one TYPE field, which indicates the type of the annotation.
     * 
     */
    protected final DasType type;
    
    /**
     * 
     * Created for DAS1.6
     * <b>Mandatory</b>
     * 
     * Each feature has one <METHOD> field, which identifies the method used to identify the feature.
     * 
     */
    protected final DasMethod method;
    
    
    /**
     * Updated for DAS1.6
     * <b>Optional</b>
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/START
     *
     * Must be equal to or less than endCoordinate.
     *
     * <i>DAS servers are often required to serve non-positional features, such as descriptions (of the entire
     * segment) or related citations.  A commonly accepted mechanism is to give non-positional features start
     * and end coordinates of 0, however this is not enforced and is not part of the DAS 1.53 specification.</i>
     * 
     * DAS 1.6: <i>It will be omitted if equal to 0, where is indicating a non-positional feature</i>
     * 
     */    
    protected final int startCoordinate;

    /**
     * Updated for DAS1.6
     * <b>Optional</b>
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/END
     *
     * Must be equal to or greater than startCoordinate.
     *
     * <i>DAS servers are often required to serve non-positional features, such as descriptions (of the entire
     * segment) or related citations.  A commonly accepted mechanism is to give non-positional features start
     * and end coordinates of 0, however this is not enforced and is not part of the DAS 1.53 specification.</i>
     * 
     * DAS 1.6: <i>It will be omitted if equal to 0, where is indicating a non-positional feature</i>
     */
    protected final int endCoordinate;

    /**
     * Updated for DAS1.6
     * <b>optional; one per FEATURE</b>
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/SCORE
     * 
     * null will be serialize as '-' If this field is inapplicable
     * 
     */
    protected final Double score;

    /**
     * Updated for DAS1.6
     * <b>Optional; one per FEATURE</b>
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

     * This tag indicates the orientation of the feature relative to 
     * the direction of transcription. It may be 0 for features that 
     * are unrelated to transcription, +, for features that are on the 
     * sense strand, and -, for features on the antisense strand. 
     * If this tag is omitted, a value of 0 is assumed.
     */
    protected final DasFeatureOrientation orientation;

    /**
     * Updated for DAS1.6
     * <b>Optional; one per FEATURE</b>
     *
     * Select a value from:
     * DasFeature.PHASE_READING_FRAME_0
     * DasFeature.PHASE_READING_FRAME_1
     * DasFeature.PHASE_READING_FRAME_2
     * DasFeature.PHASE_NOT_APPLICABLE
     *
     * For the features command, provides the value for
     * /DASGFF/GFF/SEGMENT/FEATURE/PHASE
     * 
     * This tag indicates the position of the feature relative to open 
     * reading frame, if any. It may be one of the integers 0, 1 or 2, 
     * corresponding to each of the three reading frames, or - if the 
     * feature is unrelated to a reading frame. 
     * If this tag is omitted, a value of - is assumed.
     */
    protected final DasPhase phase;

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
     * Created for DAS 1.6
     * 
     * These tags identify other features that are parents 
     * of this feature within a hierarchy. 
     */
    protected Collection<String> parents;

    /**
     * Created for DAS 1.6
     * 
     * These tags identify other features that are children 
     * of this feature within a hierarchy. 
     */
    protected Collection<String> parts;
    
    /**
     * @deprecated
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
    protected  Collection<DasGroup> groups;

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
                      DasType type,
                      DasMethod method,
                      int startCoordinate,
                      int endCoordinate,
                      Double score,
                      DasFeatureOrientation orientation,
                      DasPhase phase,
                      Collection<String> notes,
                      Map<URL, String> links,
                      Collection<DasTarget> targets,
                      Collection<String> parents,
                      Collection<String> parts) throws DataSourceException {

        if (featureId == null || type == null){
            throw new DataSourceException ("An attempt to instantiate a DasFeature object without the minimal required mandatory values.");
        }
        if (orientation == null) {
            orientation = DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE;
        }
        if (phase == null){
            phase = DasPhase.PHASE_NOT_APPLICABLE;
        }
        this.featureId = featureId;
        this.featureLabel = featureLabel;
        this.type = type;
        this.method = method;
        this.startCoordinate = startCoordinate;
        this.endCoordinate = endCoordinate;
        this.score = score;
        this.orientation = orientation;
        this.phase = phase;
        this.notes = notes;
        this.links = links;
        this.targets = targets;
        this.parents = parents;
        this.parts = parts;
    }


    public String getFeatureId() {
        return featureId;
    }

    public String getFeatureLabel() {
        return featureLabel;
    }

    /**
     * @deprecated
     */
    public String getTypeId() {
        return typeId;
    }

    /**
     * @deprecated
     */
    public String getTypeCategory() {
        return typeCategory;
    }

    /**
     * @deprecated
     */
    public String getTypeLabel() {
        return typeLabel;
    }

    /**
     * @deprecated
     */
    public String getMethodId() {
        return methodId;
    }

    /**
     * @deprecated
     */
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

    public DasFeatureOrientation getOrientation() {
        return orientation;
    }

    public DasPhase getPhase() {
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

    /**
     * @deprecated
     */
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
        if (parents != null ? !parents.equals(that.parents) : that.parents != null) return false;
        if (parts != null ? !parts.equals(that.parts) : that.parts != null) return false;
        if (links != null ? !links.equals(that.links) : that.links != null) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        if (notes != null ? !notes.equals(that.notes) : that.notes != null) return false;
        if (!orientation.equals(that.orientation)) return false;
        if (!phase.equals(that.phase)) return false;
        if (!score.equals(that.score)) return false;
        if (targets != null ? !targets.equals(that.targets) : that.targets != null) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = featureId.hashCode();
        result = 31 * result + (featureLabel != null ? featureLabel.hashCode() : 0);
        result = 31 * result + type.hashCode();
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + startCoordinate;
        result = 31 * result + endCoordinate;
        result = 31 * result + score.hashCode();
        result = 31 * result + orientation.hashCode();
        result = 31 * result + phase.hashCode();
        result = 31 * result + (notes != null ? notes.hashCode() : 0);
        result = 31 * result + (links != null ? links.hashCode() : 0);
        result = 31 * result + (targets != null ? targets.hashCode() : 0);
        result = 31 * result + (parents != null ? parents.hashCode() : 0);
        result = 31 * result + (parts != null ? parts.hashCode() : 0);
        return result;
    }

    public String toString() {
        String result="";
        result = result + (featureLabel != null ? " LABEL: "+featureLabel : "");
        result = result + " TYPE: "+type.toString();
        result = result + (method != null ? "METHOD "+method.toString() : "");
        result = result + " START: "+startCoordinate;
        result = result + " STOP: "+endCoordinate;
        result = result + " SCORE: "+score;
        result = result + " ORIENTATION: "+orientation;
        result = result + " PHASE: "+phase.hashCode();
        result = result + (notes != null ? " NOTES: "+notes.toString() : "");
        result = result + (links != null ? " LINKS: "+links.toString() : "");
        result = result + (targets != null ? " TARGETS: "+targets.toString() : "");
        result = result + (parents != null ? " PARENTS: "+parents.toString() : "");
        result = result + (parts != null ? " PARTS: "+parts.toString() : "");
        return result;
    }

	public DasType getType() {
		return type;
	}


	public DasMethod getMethod() {
		return method;
	}


	public Collection<String> getParents() {
		return parents;
	}


	public Collection<String> getParts() {
		return parts;
	}
}
