package uk.ac.ebi.mydas.model.alignment;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

public class AlignmentVector {
	private final double x,y,z;

	public AlignmentVector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	/**
	 * Generates the piece of XML into the XML serializer object to describe a Geo3D.
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
     */
	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"vector");
		serializer.attribute(DAS_XML_NAMESPACE, "x", ""+x);
		serializer.attribute(DAS_XML_NAMESPACE, "y", ""+y);
		serializer.attribute(DAS_XML_NAMESPACE, "z", ""+z);
		serializer.endTag(DAS_XML_NAMESPACE,"vector");
	}
}
