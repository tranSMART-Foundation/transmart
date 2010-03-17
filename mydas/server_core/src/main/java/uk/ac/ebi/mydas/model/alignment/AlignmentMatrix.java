package uk.ac.ebi.mydas.model.alignment;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

public class AlignmentMatrix {
	private final double mat11,mat12,mat13,mat21,mat22,mat23,mat31,mat32,mat33;
	

	public AlignmentMatrix(double mat11, double mat12, double mat13, double mat21,
			double mat22, double mat23, double mat31, double mat32, double mat33) {
		this.mat11 = mat11;
		this.mat12 = mat12;
		this.mat13 = mat13;
		this.mat21 = mat21;
		this.mat22 = mat22;
		this.mat23 = mat23;
		this.mat31 = mat31;
		this.mat32 = mat32;
		this.mat33 = mat33;
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
		serializer.startTag(DAS_XML_NAMESPACE,"matrix");
		serializer.attribute(DAS_XML_NAMESPACE, "mat11", ""+mat11);
		serializer.attribute(DAS_XML_NAMESPACE, "mat12", ""+mat12);
		serializer.attribute(DAS_XML_NAMESPACE, "mat13", ""+mat13);
		serializer.attribute(DAS_XML_NAMESPACE, "mat21", ""+mat21);
		serializer.attribute(DAS_XML_NAMESPACE, "mat22", ""+mat22);
		serializer.attribute(DAS_XML_NAMESPACE, "mat23", ""+mat23);
		serializer.attribute(DAS_XML_NAMESPACE, "mat31", ""+mat31);
		serializer.attribute(DAS_XML_NAMESPACE, "mat32", ""+mat32);
		serializer.attribute(DAS_XML_NAMESPACE, "mat33", ""+mat33);
		serializer.endTag(DAS_XML_NAMESPACE,"matrix");
	}
}
