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

import java.io.IOException;

import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasSequence;

import org.xmlpull.v1.XmlSerializer;

/**
 * Created using IntelliJ IDEA.
 * Date: 12-May-2007
 * Time: 11:49:24
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
class SequenceReporter{

    private final boolean restricted;

    private int requestedStart;

    private int requestedStop;

    private final DasSequence sequence;


    SequenceReporter(DasSequence sequence, SegmentQuery segmentQuery) throws CoordinateErrorException {
        this.restricted = segmentQuery.getStartCoordinate() != null;
        if (restricted){
            this.requestedStart = segmentQuery.getStartCoordinate();
            this.requestedStop = segmentQuery.getStopCoordinate();
        }
        this.sequence = sequence;

        if (restricted && (requestedStart < sequence.getStartCoordinate() || requestedStop > sequence.getStopCoordinate())){
            throw new CoordinateErrorException(sequence.getSegmentId(), requestedStart,  requestedStop);
        }
    }

    String getSequenceString() throws CoordinateErrorException {
        return (restricted)
            ? sequence.getRestrictedSequenceString(requestedStart, requestedStop)
            : sequence.getSequenceString();
    }

    String getSegmentName(){
        return sequence.getSegmentId();
    }

    String getSequenceVersion(){
        return sequence.getVersion();
    }

    String getSequenceMoleculeType(){
        return sequence.getMolType();
    }

    int getStart(){
        return (restricted)
                ? requestedStart
                : sequence.getStartCoordinate();
    }

    int getStop(){
        return (restricted)
                ? requestedStop
                : sequence.getStopCoordinate();
    }
    
    /**
     * Wrapper for the method serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer,boolean dna) to get the format of the DAS command sequence 
     * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
     * @param serializer Object where the XML is been written
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
     * @throws CoordinateErrorException indicating that a request for sequenceString of features over a particular range of coordinates is out of range of the segment itself
     */
    void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer) throws java.io.IOException, IllegalArgumentException, IllegalStateException, CoordinateErrorException{
    	this.serialize(DAS_XML_NAMESPACE, serializer,false);
    }
    
    /**
	 * Generates the piece of XML into the XML serializer object to describe a Sequence.
	 * It supprots the formats of the DAs commands sequence and the deprecated dna 
     * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
     * @param serializer Object where the XML is been written
     * @param dna indicates if should have the format of the command dna(true) or the format of the sequence command(false)
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
     * @throws CoordinateErrorException indicating that a request for sequenceString of features over a particular range of coordinates is out of range of the segment itself
     */
    void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer,boolean dna) 
    	throws java.io.IOException, IllegalArgumentException, IllegalStateException, CoordinateErrorException{
        serializer.startTag(DAS_XML_NAMESPACE, "SEQUENCE");
        serializer.attribute(DAS_XML_NAMESPACE, "id", this.getSegmentName());
        serializer.attribute(DAS_XML_NAMESPACE, "start", Integer.toString(this.getStart()));
        serializer.attribute(DAS_XML_NAMESPACE, "stop", Integer.toString(this.getStop()));
        serializer.attribute(DAS_XML_NAMESPACE, "version", this.getSequenceVersion());

        if (dna){
        	serializer.startTag(DAS_XML_NAMESPACE, "DNA");
        	serializer.attribute(DAS_XML_NAMESPACE, "length", Integer.toString(this.getSequenceString().length()));
        } else {
            serializer.attribute(DAS_XML_NAMESPACE, "moltype", this.getSequenceMoleculeType());
        }
        serializer.text(this.getSequenceString());

        if (dna){
        	serializer.endTag(DAS_XML_NAMESPACE, "DNA");
        }
        serializer.endTag(DAS_XML_NAMESPACE, "SEQUENCE");
        
    	
    }
}
