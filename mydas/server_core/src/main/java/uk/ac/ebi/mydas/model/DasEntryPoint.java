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

import java.io.Serializable;

/**
 * Created Using IntelliJ IDEA.
 * Date: 10-May-2007
 * Time: 10:30:54
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * Implements an entry point that holds all of the information
 * available for a single /DASEP/ENTRY_POINTS/SEGMENT element
 * as returned by the entry_point command.
 */
public class DasEntryPoint implements Serializable {

    /**
     * The id of the entry point (or segment)
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@id</code> attribute.
     */
    private String segmentId;

    /**
     * The start coordinate of the entry point.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@start</code> attribute.
     */
    private Integer startCoordinate;

    /**
     * The end coordinate of the entry point.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@stop</code> attribute.
     */
    private Integer stopCoordinate;

    /**
     * The type of the entry point
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@type</code> attribute.
     */
    private String type;

    /**
     * Added for DAS1.6
     * The version attribute (optional) indicates the version of the reference object, used for coordinate systems which are not themselves versioned.
     */
    private String version;
    
    /**
     * The orientation of the entry point.  Three
     * possible values are available for this and are defined
     * as static variables of this interface:
     * POSITIVE_ORIENTATION, NEGATIVE_ORIENTATION and NO_INTRINSIC_ORIENTATION.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@orientation</code> attribute.
     */
    private DasEntryPointOrientation orientation;

    /**
     * An optional description of the entry point.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT</code> element.
     */
    private String description;

    /**
     * A boolean indicating if the entry point has subparts that can be
     * accessed using the feature request 'component' category.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@subparts</code> element.
     */
    private boolean subparts;


    /**
     * Constructor for a DasEntryPoint that enforces that a valid DasEntryPoint is constructed.
     * @param segmentId <b>Mandatory</b>.  The segmentId for the entry point (i.e. the identifier of the entry point).
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@id</code> attribute.
     * @param startCoordinate <b>Mandatory</b>. The start coordinate of the entry point.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@start</code> attribute.
     * @param stopCoordinate <b>Mandatory</b>. The end coordinate of the entry point.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@stop</code> attribute.
     * @param type <b>Optional</b>. May in the future be used to support ontology-based feature typing, according to the
     * DAS 1.53 specification.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@type</code> attribute.
     * @param orientation <b>Mandatory</b>.  Should be set as one of <code>DasEntryPoint.POSITIVE_ORIENTATION</code>,
     * <code>DasEntryPoint.NEGATIVE_ORIENTATION</code> or <code>DasEntryPoint.NO_INTRINSIC_ORIENTATION</code>.  If null
     * is passed in, will default to <code>DasEntryPoint.NO_INTRINSIC_ORIENTATION</code>.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@orientation</code> attribute.
     * @param hasSubparts <b>Mandatory</b>. indicates if the entry point has subparts that can be
     * accessed using the feature request 'component' category.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@subparts</code> element.
     * @param description <b>Optional</b> description of the entry point.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT</code> element.
     */
    public DasEntryPoint(String segmentId, Integer startCoordinate, Integer stopCoordinate, String type, String version, DasEntryPointOrientation orientation, String description, boolean hasSubparts)
            throws DataSourceException {
        if (segmentId == null || segmentId.length() == 0){
            throw new DataSourceException("A new DasEntryPoint object must be initialised with a segmentId.");
        }

        this.segmentId = segmentId;
        this.startCoordinate = startCoordinate;
        this.stopCoordinate = stopCoordinate;
        this.type = type;
        this.version=version;
        this.orientation = (orientation == null)
                ? DasEntryPointOrientation.NO_INTRINSIC_ORIENTATION
                : orientation;
        this.description = description;
        this.subparts = hasSubparts;
    }

    /**
     * Returns the id of the entry point (or segment)
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@id</code> attribute.
     * @return the id of the entry point (or segment)
     */
    public String getSegmentId() {
        return segmentId;
    }

    /**
     * Returns the start coordinate of the entry point.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@start</code> attribute.
     * @return the start coordinate of the entry point.
     */
    public Integer getStartCoordinate() {
        return startCoordinate;
    }

    /**
     * Returns the end coordinate of the entry point.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@stop</code> attribute.
     * @return the end coordinate of the entry point.
     */
    public Integer getStopCoordinate() {
        return stopCoordinate;
    }

    /**
     * Returns the type of the entry point
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@type</code> attribute.
     * @return the type of the entry point
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the orientation of the entry point.  Three
     * possible values are available for this and are defined
     * as static variables of this interface:
     * POSITIVE_ORIENTATION, NEGATIVE_ORIENTATION and NO_INTRINSIC_ORIENTATION.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@orientation</code> attribute.
     * @return the orientation of the entry point.
     */
    public DasEntryPointOrientation getOrientation() {
        return orientation;
    }

    /**
     * Returns an optional description of the entry point.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT</code> element.
     * @return an optional description of the entry point.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a boolean indicating if the entry point has subparts that can be
     * accessed using the feature request 'component' category.
     * For the entry_point command, provides the value for the
     * <code>/DASEP/ENTRY_POINTS/SEGMENT/@subparts</code> element.
     * @return a boolean indicating if the entry point has subparts.
     */
    public boolean hasSubparts() {
        return subparts;
    }

	public String getVersion() {
		return version;
	}
}
