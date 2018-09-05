package uk.ac.ebi.mydas.model.alignment;

import java.io.IOException;
import java.util.Collection;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 *
 * This class holds all of the information required to fully populate
 * a /dasalignment/alignment/block element returned from the alignment
 * command.  Some fields are optional and can be populated
 * using <code>null</code> as described in the JavaDoc for the
 * constructor.
 */
public class Block {
	protected final String blockScore;
	protected final String blockOrder;
	protected final Collection<Segment> segments;

	/**
	 * @param blockScore Allows a score to be attached to a section of the alignment rather than the whole alignment.
	 * @param blockOrder Numbers the blocks in the alignment, starting from "1".
	 * @param segments Each block must contain at least two segments - one for each alignObject. This identifies which region of each object is represented in the block.
	 * @throws DataSourceException To wrap any Exceptions thrown if this object is not constructed correctly.
	 */
	public Block(String blockScore, String blockOrder,
			Collection<Segment> segments) throws DataSourceException {
		
		if (blockOrder==null)
            throw new DataSourceException ("An attempt to instantiate a Block object without the minimal required mandatory values.");

		if (segments == null || segments.size()<2 ){
            throw new DataSourceException ("An attempt to instantiate a Block object without the minimal required mandatory values.");
        }

		this.blockScore = blockScore;
		this.blockOrder = blockOrder;
		this.segments = segments;
	}

	/**
	 * Generates the piece of XML into the XML serializer object to describe a Block.
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
     */
	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"block");
		if(blockScore!=null)
			serializer.attribute(DAS_XML_NAMESPACE, "blockScore", blockScore);
		serializer.attribute(DAS_XML_NAMESPACE, "blockOrder", blockOrder);
		if (segments!=null)
			for (Segment segment: segments)
				segment.serialize( DAS_XML_NAMESPACE,  serializer);
		serializer.endTag(DAS_XML_NAMESPACE,"block");
	}

}
