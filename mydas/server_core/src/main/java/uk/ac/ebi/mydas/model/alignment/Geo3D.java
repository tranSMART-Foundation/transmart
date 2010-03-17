package uk.ac.ebi.mydas.model.alignment;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

public class Geo3D {
	
	protected final String intObjectId;
	protected final AlignmentVector vector;
	protected final AlignmentMatrix matrix;
	
	public Geo3D(String intObjectId, AlignmentVector vector,
			AlignmentMatrix matrix) throws DataSourceException{
		
		if (intObjectId==null || vector==null || matrix==null)
			throw new DataSourceException("An attempt to instantiate a Geo3D object without the minimal required mandatory values.");
		this.intObjectId = intObjectId;
		this.vector = vector;
		this.matrix = matrix;
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
		serializer.startTag(DAS_XML_NAMESPACE,"geo3D");
		serializer.attribute(DAS_XML_NAMESPACE, "intObjectId", intObjectId);
		vector.serialize(DAS_XML_NAMESPACE, serializer);
		matrix.serialize(DAS_XML_NAMESPACE, serializer);
		serializer.endTag(DAS_XML_NAMESPACE,"geo3D");
	}	

}
