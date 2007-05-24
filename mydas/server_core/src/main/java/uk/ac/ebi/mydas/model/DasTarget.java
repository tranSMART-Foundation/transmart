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

/**
 * Created Using IntelliJ IDEA.
 * Date: 14-May-2007
 * Time: 17:03:30
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class DasTarget {

    /**
     * <b>Mandatory</b> field, corresponds to
     * the following attribute in the response to the
     * feature command:
     *
     * /DASGFF/GFF/SEGMENT/FEATURE/TARGET/@id
     */
    private String targetId;

    /**
     * <b>Optional</b> field, corresponds to
     * the value of the following element in the response to the
     * feature command:
     *
     * /DASGFF/GFF/SEGMENT/FEATURE/TARGET
     */
    private String targetName;

    /**
     * <b>Mandatory</b> field, corresponds to
     * the following attribute in the response to the
     * feature command:
     *
     * /DASGFF/GFF/SEGMENT/FEATURE/TARGET/@start
     */
    private int startCoordinate;

    /**
     * <b>Mandatory</b> field, corresponds to
     * the following attribute in the response to the
     * feature command:
     *
     * /DASGFF/GFF/SEGMENT/FEATURE/TARGET/@stop
     */
    private int stopCoordinate;

    /**
     * Constructor for a DasTarget, serialized out as the response to a feature request.
     * @param targetId <b>Mandatory</b> field, corresponds to
     * the following attribute in the response to the
     * feature command:
     * /DASGFF/GFF/SEGMENT/FEATURE/TARGET/@id
     * @param startCoordinate <b>Mandatory</b> field, corresponds to
     * the following attribute in the response to the
     * feature command:
     * /DASGFF/GFF/SEGMENT/FEATURE/TARGET/@start
     * @param stopCoordinate <b>Mandatory</b> field, corresponds to
     * the following attribute in the response to the
     * feature command:
     * /DASGFF/GFF/SEGMENT/FEATURE/TARGET/@stop
     * @param targetName <b>Optional</b> field, corresponds to
     * the value of the following element in the response to the
     * feature command:
     * /DASGFF/GFF/SEGMENT/FEATURE/TARGET
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     * if the targetId attribute has not been set to a valid value (i.e. non-zero length String).
     */
    public DasTarget(String targetId, int startCoordinate, int stopCoordinate, String targetName) throws DataSourceException {
        if (targetId == null || targetId.length() == 0){
            throw new DataSourceException("The mandatory targetId parameter has been set as null / empty String when attempting to instantiate a DasTarget object.");
        }
        this.targetId = targetId;
        this.startCoordinate = startCoordinate;
        this.stopCoordinate = stopCoordinate;
        this.targetName = targetName;
    }


    public String getTargetId() {
        return targetId;
    }

    public int getStartCoordinate() {
        return startCoordinate;
    }

    public int getStopCoordinate() {
        return stopCoordinate;
    }

    public String getTargetName() {
        return targetName;
    }
}
