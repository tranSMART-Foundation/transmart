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

import org.xmlpull.v1.XmlSerializer;

/**
 * Created Using IntelliJ IDEA.
 * Date: 21-May-2007
 * Time: 18:05:35
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class UnknownSegmentReporter implements SegmentReporter {

    final SegmentQuery query;

    public UnknownSegmentReporter(SegmentQuery query){
        this.query = query;
    }
    public Integer getStart() {
        return query.getStartCoordinate();
    }

    public Integer getStop() {
        return query.getStopCoordinate();
    }

    public String getSegmentId() {
        return query.getSegmentId();
    }
	/**
	 * Generates the piece of XML into the XML serializer object to describe an ErrorSegment or an UnknownSegment 
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @param referenceSource indicates if the reference server has answered.
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
	 */
    void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer,boolean referenceSource) 
    	throws IllegalArgumentException, IllegalStateException, IOException{
        serializer.startTag(DAS_XML_NAMESPACE, (referenceSource) ? "ERRORSEGMENT" : "UNKNOWNSEGMENT");
        serializer.attribute(DAS_XML_NAMESPACE, "id", this.getSegmentId());
        if (this.getStart() != null){
            serializer.attribute(DAS_XML_NAMESPACE, "start", Integer.toString(this.getStart()));
        }
        if (this.getStop() != null){
            serializer.attribute(DAS_XML_NAMESPACE, "stop", Integer.toString(this.getStop()));
        }
        serializer.endTag(DAS_XML_NAMESPACE, (referenceSource) ? "ERRORSEGMENT" : "UNKNOWNSEGMENT");
    	
    }

}
