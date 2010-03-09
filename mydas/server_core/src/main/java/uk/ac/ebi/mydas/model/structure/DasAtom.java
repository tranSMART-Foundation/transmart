package uk.ac.ebi.mydas.model.structure;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

public class DasAtom {
	protected final double x;
	protected final double y;
	protected final double z;
	protected final String atomName;
	protected final String atomId;
	protected final String occupancy;
	protected final String tempFactor;
	protected final String altLoc;
	
	public DasAtom(double x, double y, double z, String atomName,
			String atomId, String occupancy, String tempFactor, String altLoc) throws DataSourceException {
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

	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"atom");
		
		serializer.attribute(DAS_XML_NAMESPACE, "atomID", atomId);
		serializer.attribute(DAS_XML_NAMESPACE, "atomName", atomName);
		serializer.attribute(DAS_XML_NAMESPACE, "x", ""+x);
		serializer.attribute(DAS_XML_NAMESPACE, "y", ""+y);
		serializer.attribute(DAS_XML_NAMESPACE, "z", ""+z);
		if (occupancy!=null && occupancy.length()>0)
			serializer.attribute(DAS_XML_NAMESPACE, "occupancy", occupancy);
		if (tempFactor!=null && tempFactor.length()>0)
			serializer.attribute(DAS_XML_NAMESPACE, "tempFactor", tempFactor);
		if (altLoc!=null && altLoc.length()>0)
			serializer.attribute(DAS_XML_NAMESPACE, "altLoc", altLoc);

		serializer.endTag(DAS_XML_NAMESPACE,"atom");
		
	}
	
}
