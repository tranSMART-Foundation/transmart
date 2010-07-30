package uk.ac.ebi.mydas.controller;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: ljgarcia
 * Date: 27-Jul-2010
 * Time: 16:42:24
 * To change this template use File | Settings | File Templates.
 */
public class ErrorSequenceReporter implements SequenceReporter {
    final SegmentQuery query;

    public ErrorSequenceReporter(SegmentQuery query){
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
	 * Generates the piece of XML into the XML serializer object to describe an ErrorSegment
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written
	 * @throws java.io.IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
	 */
    void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer)
    	throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(DAS_XML_NAMESPACE, "ERRORSEGMENT");
        serializer.attribute(DAS_XML_NAMESPACE, "id", this.getSegmentId());
        if (this.getStart() != null){
            serializer.attribute(DAS_XML_NAMESPACE, "start", Integer.toString(this.getStart()));
        }
        if (this.getStop() != null){
            serializer.attribute(DAS_XML_NAMESPACE, "stop", Integer.toString(this.getStop()));
        }
        serializer.endTag(DAS_XML_NAMESPACE, "ERRORSEGMENT");

    }
}
