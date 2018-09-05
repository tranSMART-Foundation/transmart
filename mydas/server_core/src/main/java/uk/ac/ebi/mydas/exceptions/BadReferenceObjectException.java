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

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 14:12:53
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * Exception class specifically to indicate that a particular segment
 * cannot be found by the AnnotationDataSource, in the absence of any other errors
 * (i.e. should only be thrown if the AnnotationDataSource is otherwise healthy)
 */
public class BadReferenceObjectException extends Exception{

    /**
     * The name / accession of the segment being queried.
     */
    private final String segment;

    public BadReferenceObjectException(String segment, String message){
        super(message);
        this.segment = segment;
    }

    public BadReferenceObjectException(String segment, String message, Throwable cause){
        super(message, cause);
        this.segment = segment;
    }


    public String getSegment() {
        return segment;
    }
}
