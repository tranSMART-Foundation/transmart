package uk.ac.ebi.mydas.model.structure;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 *
 * This class holds all of the information required to fully populate
 * a /dasstructure/chain/group/atom element returned from the structure
 * command.  Some fields are optional and can be populated
 * using <code>null</code> as described in the JavaDoc for the
 * constructor.
 */
public class DasAtom {
	protected final double x;
	protected final double y;
	protected final double z;
	protected final String atomName;
	protected final String atomId;
	protected final Double occupancy;
	protected final Double tempFactor;
	protected final String altLoc;
	
	/**
	 * A single atom in a single conformation.
	 * @param x <b>Mandatory</b> Describing the coordinate X of the atom.
	 * @param y <b>Mandatory</b> Describing the coordinate Y of the atom.
	 * @param z <b>Mandatory</b> Describing the coordinate Z of the atom.
	 * @param atomName <b>Mandatory</b> Is a name (symbol) for the atom.
	 * @param atomId <b>Mandatory</b> Uniquely identifies the atom within the structure
	 * @param occupancy <b>Optional</b> Floating point number representing the occupancy of the atom.
	 * @param tempFactor <b>Optional</b> Floating point number representing the temperature factor of the atom.
	 * @param altLoc <b>Optional</b> Indicates that this definition describes one of several possible locations for the atom. Different atoms with the same altLoc consitute a single conformation.
	 * @throws DataSourceException To wrap any Exceptions thrown if this object is not constructed correctly.
	 */
	public DasAtom(double x, double y, double z, String atomName,
			String atomId, Double occupancy, Double tempFactor, String altLoc) throws DataSourceException {
		super();
		if (atomName == null || atomId == null ){
            throw new DataSourceException ("An attempt to instantiate an Atom without the minimal required mandatory values.");
        }
		this.x = x;
		this.y = y;
		this.z = z;
		this.atomName = atomName;
		this.atomId = atomId;
		this.occupancy = occupancy;
		this.tempFactor = tempFactor;
		this.altLoc = altLoc;
	}

	/**
	 * Generates the piece of XML into the XML serializer object to describe a /dasstructure/chain/group/atom.
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
     */
	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"atom");
		
		serializer.attribute(DAS_XML_NAMESPACE, "atomID", atomId);
		serializer.attribute(DAS_XML_NAMESPACE, "atomName", atomName);
		serializer.attribute(DAS_XML_NAMESPACE, "x", ""+x);
		serializer.attribute(DAS_XML_NAMESPACE, "y", ""+y);
		serializer.attribute(DAS_XML_NAMESPACE, "z", ""+z);
		if (occupancy!=null)
			serializer.attribute(DAS_XML_NAMESPACE, "occupancy", occupancy.toString());
		if (tempFactor!=null)
			serializer.attribute(DAS_XML_NAMESPACE, "tempFactor", tempFactor.toString());
		if (altLoc!=null && altLoc.length()>0)
			serializer.attribute(DAS_XML_NAMESPACE, "altLoc", altLoc);

		serializer.endTag(DAS_XML_NAMESPACE,"atom");
		
	}
	
}
