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

/**
 * Created Using IntelliJ IDEA.
 * Date: 14-May-2007
 * Time: 17:03:30
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class DasTarget {

    private String targetId;

    private String targetName;

    private int startCoordinate;

    private int stopCoordinate;

    /**
     * Constructor for a DasTarget, serialized out as the response to a feature request.
     * @param targetId <b>Mandatory</b>
     * @param startCoordinate <b>Mandatory</b>
     * @param stopCoordinate <b>Mandatory</b>
     * @param targetName <b>Optional</b>
     */
    public DasTarget(String targetId, int startCoordinate, int stopCoordinate, String targetName) {
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
