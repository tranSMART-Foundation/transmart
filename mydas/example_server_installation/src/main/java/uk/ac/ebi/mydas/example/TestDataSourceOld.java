package uk.ac.ebi.mydas.example;

import java.net.MalformedURLException;
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
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.extendedmodel.DasUnknownFeatureSegment;
import uk.ac.ebi.mydas.model.*;

/**
 * @author 4ndr01d3
 *
 */
public class TestDataSourceOld implements AnnotationDataSource {

	CacheManager cacheManager = null;
	ServletContext svCon;
	Map<String, PropertyType> globalParameters;
	DataSourceConfiguration config;

	public void destroy() {
		// In this case, nothing to do.
	}

	public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbeans)
	throws BadReferenceObjectException, DataSourceException {
		try{
			if (segmentId.equals ("one")){
				Collection<DasFeature> oneFeatures = new ArrayList<DasFeature>(2);
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
						Collections.singleton("This is a note relating to feature one of segment one."),
						Collections.singletonMap(new URL("http://code.google.com/p/mydas/"), "mydas project home page."),
						Collections.singleton(target),
						null,
						null
				));
				return new DasAnnotatedSegment("one", 1, 34, "Up-to-date", "one_label", oneFeatures);
			}
			else throw new BadReferenceObjectException(segmentId, "Not found");
		} catch (MalformedURLException e) {
			throw new DataSourceException("Tried to create an invalid URL for a LINK element.", e);
		}
	}

	public Collection<DasAnnotatedSegment> getFeatures(
			Collection<String> featureIdCollection, Integer maxbins)
			throws UnimplementedFeatureException, DataSourceException {
		Collection<DasAnnotatedSegment> segments = new ArrayList<DasAnnotatedSegment>(featureIdCollection.size());
		for (String featureId: featureIdCollection){
			if (featureId.equals("oneFeatureIdOne")){
				Collection<DasFeature> oneFeatures = new ArrayList<DasFeature>(1);
				DasTarget target = new DasTarget("oneTargetId", 20, 30, "oneTargetName");
				try {
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
							Collections.singleton("This is a note relating to feature one of segment one."),
							Collections.singletonMap(new URL("http://code.google.com/p/mydas/"), "mydas project home page."),
							Collections.singleton(target),
							null,
							null
					));
				} catch (MalformedURLException e) {}
				segments.add(new DasAnnotatedSegment("one", 1, 34, "Up-to-date", "one_label", oneFeatures));
			} else {
				segments.add(new DasUnknownFeatureSegment(featureId));
			}
		}
		return segments;
	}

	public URL getLinkURL(String field, String id)
	throws UnimplementedFeatureException, DataSourceException {
		throw new UnimplementedFeatureException("No implemented");
	}

    public String getEntryPointVersion() throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("No implemented");
    }

    public Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop) throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("No implemented");
    }

    public int getTotalEntryPoints() throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("No implemented");
    }

    public Integer getTotalCountForType(DasType type)
	throws DataSourceException {
    	if (type.getId()=="oneFeatureTypeIdTwo")
    		return new Integer(1);
    	return null;
	}

	public Collection<DasType> getTypes() throws DataSourceException {
		Collection<DasType> types = new ArrayList<DasType>(1);
		types.add (new DasType("oneFeatureTypeIdOne", "oneFeatureCategoryOne", "CV:00001",null));
		return types;
	}

	public void init(ServletContext servletContext,
			Map<String, PropertyType> globalParameters,
			DataSourceConfiguration dataSourceConfig)
	throws DataSourceException {
		this.svCon = servletContext;
		this.globalParameters = globalParameters;
		this.config = dataSourceConfig;
	}

	public void registerCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

}
