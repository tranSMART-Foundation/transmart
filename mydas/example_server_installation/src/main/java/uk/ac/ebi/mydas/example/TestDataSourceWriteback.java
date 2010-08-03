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
import uk.ac.ebi.mydas.model.DasType;

public class TestDataSourceWriteback implements WritebackDataSource, AnnotationDataSource {

	@Override
	public DasAnnotatedSegment create(DasAnnotatedSegment segment)
			throws DataSourceException {
		return segment;
	}

	@Override
	public DasAnnotatedSegment delete(String login, String password,
			String segmentId, String featureId) throws DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DasAnnotatedSegment history(String featureId)
			throws DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DasAnnotatedSegment update(DasAnnotatedSegment segment)
			throws DataSourceException {
		return segment;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbeans)
			throws BadReferenceObjectException, DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<DasAnnotatedSegment> getFeatures(
			Collection<String> featureIdCollection, Integer maxbins)
			throws UnimplementedFeatureException, DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getLinkURL(String field, String id)
			throws UnimplementedFeatureException, DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getTotalCountForType(DasType type)
			throws DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<DasType> getTypes() throws DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(ServletContext servletContext,
			Map<String, PropertyType> globalParameters,
			DataSourceConfiguration dataSourceConfig)
			throws DataSourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerCacheManager(CacheManager cacheManager) {
		// TODO Auto-generated method stub

	}

}
