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

package uk.ac.ebi.mydas.exceptions;

import java.text.MessageFormat;

/**
 * Created using IntelliJ IDEA.
 * Date: 12-May-2007
 * Time: 12:03:46
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 * Exception indicating that a request for sequenceString of features over a particular range of
 * coordinates is out of range of the segment itself.
 */
public class CoordinateErrorException extends Exception{

    public CoordinateErrorException(String segmentName, int requestedStart, int requestedEnd){
        super (MessageFormat.format("A request has been made for a coordinate that is out of range, segment name {0} requested start {1} requested stop {2}", segmentName, requestedStart, requestedEnd));
    }
}
