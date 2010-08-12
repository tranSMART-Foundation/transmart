package uk.ac.ebi.mydas.datasource;

import java.util.Map;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;

public interface WritebackDataSource {
    public DasAnnotatedSegment create(DasAnnotatedSegment segment) throws DataSourceException;
    public DasAnnotatedSegment update(DasAnnotatedSegment segment) throws DataSourceException;
    public DasAnnotatedSegment delete(String segmentId,String featureId, Map<String, String> extraParameters) throws DataSourceException;
    public DasAnnotatedSegment history(String featureId) throws DataSourceException;

}
