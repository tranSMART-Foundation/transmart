package uk.ac.ebi.mydas.model.alignment;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 *
 * This class holds all of the information required to fully populate
 * a /dasalignment/alignment/block/segment element returned from the alignment
 * command.  Some fields are optional and can be populated
 * using <code>null</code> as described in the JavaDoc for the
 * constructor.
 */
public class Segment {
	protected final String intObjectId;
	protected final Integer start;
	protected final Integer end;
	protected final String orientation;
	protected final String cigar;
	
	/**
	 * @param intObjectId identifies the alignObject
	 * @param start marking the start of the segment. If omitted together with end and orientation, the segment is assumed to represent the full length of the object, and be unstranded.
	 * @param end marking the end of the segment. If omitted together with start and orientation, the segment is assumed to represent the full length of the object, and be unstranded.
	 * @param marking the orientation of the segment. orientation If omitted together with start and end, the segment is assumed to represent the full length of the object, and be unstranded.
	 * @param cigar If the segment contains gaps within the block, it must be accompanied by a cigar element in order to provide the relevant information. The content takes the form of a CIGAR (Compact Idiosyncratic Gapped Alignment Report) string. For example "3MI5M2D" indicates that the segment can be expanded to: 3 matches, 1 insertion, 5 matches followed by 2 deletions. If omitted, it is assumed that the segment can be represented as a full-length contiguous matching block (i.e. all matches). 
	 * @throws DataSourceException To wrap any Exceptions thrown if this object is not constructed correctly.
	 */
	public Segment(String intObjectId, Integer start, Integer end,
			String orientation, String cigar) throws DataSourceException {
		
		if(intObjectId==null)
			throw new DataSourceException ("An attempt to instantiate a alignment.Segment object without the minimal required mandatory values.");
		
		this.intObjectId = intObjectId;
		this.start = start;
		this.end = end;
		this.orientation = orientation;
		this.cigar = cigar;
	}
	
	/**
	 * Generates the piece of XML into the XML serializer object to describe a Segment.
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
     */
	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"segment");

		serializer.attribute(DAS_XML_NAMESPACE, "intObjectId", intObjectId);
		if(start!=null)
			serializer.attribute(DAS_XML_NAMESPACE, "start", start.toString());
		if(end!=null)
			serializer.attribute(DAS_XML_NAMESPACE, "end", end.toString());
		if (orientation!=null)
			serializer.attribute(DAS_XML_NAMESPACE, "orientation", orientation);
		if (cigar!=null){
			serializer.startTag(DAS_XML_NAMESPACE,"cigar");
			serializer.text(cigar);
			serializer.endTag(DAS_XML_NAMESPACE,"cigar");
		}
		
		serializer.endTag(DAS_XML_NAMESPACE,"segment");
	}
	
}
