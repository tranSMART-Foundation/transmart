package uk.ac.ebi.mydas.extendedmodel;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasEntryPointOrientation;

/**
 * Class that extends the basic DasEntryPoint bean from the model to support serializing tasks
 * @author Gustavo Salazar
 *
 */
public class DasEntryPointE extends DasEntryPoint {


	public DasEntryPointE(String segmentId, Integer startCoordinate,
			Integer stopCoordinate, String type,String version,
			DasEntryPointOrientation orientation, String description,
			boolean hasSubparts) throws DataSourceException {
		super(segmentId, startCoordinate, stopCoordinate, type,version, orientation,
				description, hasSubparts);
	}
	
	/**
	 * Constructor to create a copy from a DasEntryPoint
	 * @param entryPoint Entry point with all its data loaded.
	 * @throws DataSourceException should be thrown if there is any
     * fatal problem with loading this data source.
	 */
	public DasEntryPointE(DasEntryPoint entryPoint) throws DataSourceException {
		super(entryPoint.getSegmentId(), entryPoint.getStartCoordinate(), entryPoint.getStopCoordinate(), entryPoint.getType(),entryPoint.getVersion(), entryPoint.getOrientation(),entryPoint.getDescription(), entryPoint.hasSubparts());
	}
	
    /**
	 * Generates the piece of XML into the XML serializer object to describe a Das Entry Point 
	 * @param DAS_XML_NAMESPACE XML namespace to link with the elements to create
	 * @param serializer Object where the XML is been written 
	 * @throws IOException If the XML writer have an error
	 * @throws IllegalStateException a method has been invoked at an illegal or inappropriate time.
	 * @throws IllegalArgumentException indicate that a method has been passed an illegal or inappropriate argument.
     */
    public void serialize(String DAS_XML_NAMESPACE,XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(DAS_XML_NAMESPACE, "SEGMENT");
        serializer.attribute(DAS_XML_NAMESPACE, "id", this.getSegmentId());
        //start and stop are an optional group
        if ( (this.getStartCoordinate() != null) && (this.getStopCoordinate() != null) ) {
            serializer.attribute(DAS_XML_NAMESPACE, "start", Integer.toString(this.getStartCoordinate()));
            serializer.attribute(DAS_XML_NAMESPACE, "stop", Integer.toString(this.getStopCoordinate()));
        }
        if (this.getVersion() != null && this.getVersion().length() > 0){
            serializer.attribute(DAS_XML_NAMESPACE, "version", this.getVersion());
        }
        if (this.getType() != null && this.getType().length() > 0){
            serializer.attribute(DAS_XML_NAMESPACE, "type", this.getType());
        }
        serializer.attribute(DAS_XML_NAMESPACE, "orientation", this.getOrientation().toString());
        if (this.hasSubparts()){
            serializer.attribute(DAS_XML_NAMESPACE, "subparts", "yes");
        }
        if (this.getDescription() != null && this.getDescription().length() > 0){
            serializer.text(this.getDescription());
        }
        serializer.endTag(DAS_XML_NAMESPACE, "SEGMENT");
    }

}
