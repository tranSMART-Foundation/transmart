package uk.ac.ebi.mydas.model.alignment;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 *
 * This class holds all of the information required to fully populate
 * a /dasalignment/alignment element returned from the alignment
 * command.  Some fields are optional and can be populated
 * using <code>null</code> as described in the JavaDoc for the
 * constructor.
 */
public class Alignment {
	protected final String alignType;
	protected final String name;
	protected final String description;
	protected final Integer position;
	protected final Integer max;
	
	protected final Collection<AlignObject> alignObjects;
    protected final Map<String, Double> scores;
	protected final Collection<Block> blocks;
	protected final Collection<Geo3D> geo3Ds;
	
	/**
	 * @param alignType <b>Optional</b> Type of the alignment
	 * @param name <b>Optional</b> short name of the alignment.
	 * @param description <b>Optional</b> longer description of the alignment.
	 * @param position <b>Optional</b> number of the alignment in the list of those returned 
	 * @param max <b>Optional</b>  the total number of returned alignments
	 * @param alignObjects <b>Mandatory</b> Each alignment must contain at least two objects, each of which representing a single row in the alignment.
	 * @param scores <b>Optional</b> An alignment can be annotated with any number of scores, according to different metrics.
	 * @param blocks <b>Mandatory</b> Each alignment must contain at least one block.
	 * @throws DataSourceException To wrap any Exceptions thrown if this object is not constructed correctly.
	 */
	public Alignment(String alignType, String name, String description,
			Integer position, Integer max, Collection<AlignObject> alignObjects,
			Map<String, Double> scores, Collection<Block> blocks,Collection<Geo3D> geo3Ds) throws DataSourceException {

		if (alignObjects == null || alignObjects.size()<2 ){
            throw new DataSourceException ("An attempt to instantiate a DasAlignment object without the minimal required mandatory values.");
        }
		if (blocks == null || blocks.size()<1 ){
            throw new DataSourceException ("An attempt to instantiate a DasAlignment object without the minimal required mandatory values.");
        }
		this.alignType = alignType;
		this.name = name;
		this.description = description;
		this.position = position;
		this.max = max;
		this.alignObjects = alignObjects;
		this.scores = scores;
		this.blocks = blocks;
		this.geo3Ds=geo3Ds;
	}

	/**
	 * Generates the piece of XML into the XML serializer object to describe an Alignment.
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
     */
	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"alignment");
		if (alignType!=null && alignType.length()>0)
			serializer.attribute(DAS_XML_NAMESPACE, "alignType", alignType);
		if (name!=null && name.length()>0)
			serializer.attribute(DAS_XML_NAMESPACE, "name", name);
		if (description!=null && description.length()>0)
			serializer.attribute(DAS_XML_NAMESPACE, "description", description);
		if (position!=null )
			serializer.attribute(DAS_XML_NAMESPACE, "position", position.toString());
		if (max!=null)
			serializer.attribute(DAS_XML_NAMESPACE, "max", max.toString());

		for (AlignObject alignObject: alignObjects)
			alignObject.serialize( DAS_XML_NAMESPACE,  serializer);

		if (scores!=null)
			for (String key: scores.keySet()){
				serializer.startTag(DAS_XML_NAMESPACE,"score");
				serializer.attribute(DAS_XML_NAMESPACE, "methodName", key);
				serializer.attribute(DAS_XML_NAMESPACE, "value", scores.get(key).toString());
				serializer.endTag(DAS_XML_NAMESPACE,"score");
			}
				
		if (blocks!=null)
			for (Block block: blocks)
				block.serialize( DAS_XML_NAMESPACE,  serializer);
		
		if (geo3Ds!=null)
			for (Geo3D geo3D: geo3Ds)
				geo3D.serialize( DAS_XML_NAMESPACE,  serializer);

		serializer.endTag(DAS_XML_NAMESPACE,"alignment");
		
	}

	
}
