package uk.ac.ebi.mydas.example;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasFeatureOrientation;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasPhase;
import uk.ac.ebi.mydas.model.DasTarget;
import uk.ac.ebi.mydas.model.DasType;
import uk.ac.ebi.mydas.model.Range;

public class TestDataSourceWriteback implements WritebackDataSource, AnnotationDataSource {


	public DasAnnotatedSegment create(DasAnnotatedSegment segment)
			throws DataSourceException {
		return segment;
	}


	public DasAnnotatedSegment delete(String segmentId, String featureId,Map<String,String> extraParameters) throws DataSourceException {
        Collection<DasFeature> oneFeatures = new ArrayList<DasFeature>(2);
		oneFeatures.add(new DasFeature(
				featureId,
		        "DELETED",
		        new DasType("DELETED", null, null,null),
		        new DasMethod("DELETED",null,null),
		        5,
		        10,
		        123.45,
		        DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE,
		        DasPhase.PHASE_NOT_APPLICABLE,
		        Collections.singleton("USER="+extraParameters.get("user")),
		        null,
		        null,
		        null,
		        null
		));
		return new DasAnnotatedSegment(segmentId, 0, 0, "FROMDELETION", null, oneFeatures);
	}


	public DasAnnotatedSegment history(String featureId) throws DataSourceException {
        Collection<DasFeature> oneFeatures = new ArrayList<DasFeature>(2);
        Collection<String> notes = new ArrayList<String>(2);
        notes.add("USER=TheUser");
        notes.add("VERSION=2");
        Collection<String> notes2 = new ArrayList<String>(2);
        notes.add("USER=TheUser");
        notes.add("VERSION=1");
        DasTarget target = new DasTarget("oneTargetId", 20, 30, "oneTargetName");
        oneFeatures.add(new DasFeature(
                "oneFeatureIdOne",
                "one Feature Label One",
                new DasType("oneFeatureTypeIdOne", "oneFeatureCategoryOne", "CV:00001","one Feature DasType Label One"),
                new DasMethod("oneFeatureMethodIdOne","one Feature Method Label One","ECO:12345"),
                5,
                10,
                123.45,
                DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE,
                DasPhase.PHASE_NOT_APPLICABLE,
                notes,
                null,
                Collections.singleton(target),
                null,
                null
        ));
		oneFeatures.add(new DasFeature(
				featureId,
		        "DELETED",
		        new DasType("DELETED", null, null,null),
		        new DasMethod("DELETED",null,null),
		        5,
		        10,
		        123.45,
		        DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE,
		        DasPhase.PHASE_NOT_APPLICABLE,
		        notes2,
		        null,
		        null,
		        null,
		        null
		));
        return new DasAnnotatedSegment("one", 1, 34, "Up-to-date", "one_label", oneFeatures);	}


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
	@Override
	public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins,
			Range rows) throws BadReferenceObjectException,
			DataSourceException, UnimplementedFeatureException {
		throw new UnimplementedFeatureException("The rows-for-feature capability has not been implemented");
	}

	@Override
	public Collection<DasAnnotatedSegment> getFeatures(
			Collection<String> featureIdCollection, Integer maxbins, Range rows)
			throws UnimplementedFeatureException, DataSourceException {
		throw new UnimplementedFeatureException("The rows-for-feature capability has not been implemented");
	}

}
