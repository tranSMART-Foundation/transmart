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
public class UnknownFeatureSegmentReporter implements SegmentReporter {


	private String segmentId;

    public UnknownFeatureSegmentReporter(String segmentId){
        this.segmentId = segmentId;
    }
    public String getSegmentId() {
        return segmentId;
    }
	/**
	 * Generates the piece of XML into the XML serializer object to describe an UnknownFeature segment
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
	 */
    void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer) 
    	throws IllegalArgumentException, IllegalStateException, IOException{
	    serializer.startTag(DAS_XML_NAMESPACE, "UNKNOWNFEATURE");
	    serializer.attribute(DAS_XML_NAMESPACE, "id", this.getSegmentId());
	    serializer.endTag(DAS_XML_NAMESPACE, "UNKNOWNFEATURE");
    }
	public Integer getStart() {
		//  Auto-generated method stub
		return null;
	}
	public Integer getStop() {
		//  Auto-generated method stub
		return null;
	}

}
