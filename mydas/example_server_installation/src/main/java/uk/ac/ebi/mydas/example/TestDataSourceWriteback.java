package uk.ac.ebi.mydas.example;

import java.net.URL;
import java.util.Collection;
import java.util.Map;

import javax.servlet.ServletContext;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.controller.CacheManager;
import uk.ac.ebi.mydas.datasource.AnnotationDataSource;
import uk.ac.ebi.mydas.datasource.WritebackDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasType;

public class TestDataSourceWriteback implements WritebackDataSource, AnnotationDataSource {


	public DasAnnotatedSegment create(DasAnnotatedSegment segment)
			throws DataSourceException {
		return segment;
	}


	public DasAnnotatedSegment delete(String login, String password,
			String segmentId, String featureId) throws DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}


	public DasAnnotatedSegment history(String featureId)
			throws DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}


	public DasAnnotatedSegment update(DasAnnotatedSegment segment)
			throws DataSourceException {
		return segment;
	}


	public void destroy() {
		// TODO Auto-generated method stub

	}

	public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbeans)
			throws BadReferenceObjectException, DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<DasAnnotatedSegment> getFeatures(
			Collection<String> featureIdCollection, Integer maxbins)
			throws UnimplementedFeatureException, DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public URL getLinkURL(String field, String id)
			throws UnimplementedFeatureException, DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

    public Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop) throws UnimplementedFeatureException, DataSourceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getEntryPointVersion() throws UnimplementedFeatureException, DataSourceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getTotalEntryPoints() throws UnimplementedFeatureException, DataSourceException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Integer getTotalCountForType(DasType type)
			throws DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<DasType> getTypes() throws DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public void init(ServletContext servletContext,
			Map<String, PropertyType> globalParameters,
			DataSourceConfiguration dataSourceConfig)
			throws DataSourceException {
		// TODO Auto-generated method stub

	}

	public void registerCacheManager(CacheManager cacheManager) {
		// TODO Auto-generated method stub

	}

}
