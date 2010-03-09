package uk.ac.ebi.mydas.model.structure;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

public class ObjectDetail {
	protected final String dbSource;
	protected final String property;
	protected final String text;
	public ObjectDetail(String dbSource, String property, String text) throws DataSourceException {
		super();
		if (dbSource == null || property == null || text == null ){
            throw new DataSourceException ("An attempt to instantiate a ObjectDetail without the minimal required mandatory values.");
        }
		
		this.dbSource = dbSource;
		this.property = property;
		this.text = text;
	}
	public void serialize(String DAS_XML_NAMESPACE, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag(DAS_XML_NAMESPACE,"objectDetail");
		
		serializer.attribute(DAS_XML_NAMESPACE, "dbSource", dbSource);
		serializer.attribute(DAS_XML_NAMESPACE, "property", property);
		
		serializer.text(text);
		
		serializer.endTag(DAS_XML_NAMESPACE,"objectDetail");
	}
}
