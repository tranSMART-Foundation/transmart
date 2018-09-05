package uk.ac.ebi.mydas.examples;



import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.datasource.AnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.extendedmodel.DasUnknownFeatureSegment;
import uk.ac.ebi.mydas.model.*;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;

/**
 * Data Source that reads a GFF 2 file which path has been specified in the 
 * configuration file as a property of the datasource element
 */
public class GFFFileDataSource implements AnnotationDataSource { 

	ServletContext svCon;
	Map<String, PropertyType> globalParameters;
	DataSourceConfiguration config;
	String path;
	private Collection<DasAnnotatedSegment> segments;
	private Collection<DasType> types;


	/**
	 * The path is recovery from the configuration, the file is then parsed and 
	 * keep in memory as a DasSegment collection object that is queried for each method
	 */
	public void init(ServletContext servletContext, Map<String, PropertyType> globalParameters, DataSourceConfiguration dataSourceConfig) throws DataSourceException {
		this.svCon = servletContext;
		this.globalParameters = globalParameters;
		this.config = dataSourceConfig;
		path = config.getDataSourceProperties().get("gff_file").getValue();
		try {
			GFF2Parser parser = new GFF2Parser(new FileInputStream(servletContext.getRealPath(path)));
			segments = parser.parse();
            List<DasFeature> lstFeatures = new ArrayList<DasFeature>();
            DasType dasType = new DasType("RNAi reagent", "RNAi reagent cat", null, "RNAi reagent label");
            DasMethod dasMethod = new DasMethod("method id", "method label", "method cvid");
            lstFeatures.add(new DasFeature("feature id", "features lable", dasType, dasMethod, 1, 1, 0.0, DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE, null, null, null, null, null));
            segments.add(new DasAnnotatedSegment("my_segment_1", 1, 1, "version 1", "my segment label", lstFeatures, 1));
			types = parser.getTypes();
            types.add(dasType);
                        System.out.println("Finished initialisation============================");
		} catch (FileNotFoundException e) {
			throw new DataSourceException("The data source cannot be loaded. The file couldn't be oppened",e);
		} catch (Exception e) {
			throw new DataSourceException("The data source cannot be loaded because of parsing problems",e);
		}
	}

	/**
	 * Nothing to destroy
	 */
	public void destroy() { }

	/**
	 * Look into the list of segments for the one with the same ID. if is not there it throws a BadReferenceObjectException
	 */
	public DasAnnotatedSegment getFeatures(String segmentId,Integer maxbins) throws BadReferenceObjectException, DataSourceException {
		for(DasAnnotatedSegment segment:segments){
			if (segment.getSegmentId().equals(segmentId))
				return segment;
		}
		throw new BadReferenceObjectException("The id is not in the file", segmentId);
	}

	/**
	 * return the already built list of types.
	 */
	public Collection<DasType> getTypes() throws DataSourceException {
		return types;
	}

	/**
	 * 
	 */
	public Collection<DasAnnotatedSegment> getFeatures(Collection<String> featureIdCollection, Integer maxbins)
	throws UnimplementedFeatureException, DataSourceException {
		Collection<DasAnnotatedSegment> segmentsResponse =new ArrayList<DasAnnotatedSegment>();
		for (String featureId:featureIdCollection){
			boolean found=false;
			for (DasAnnotatedSegment segment:segments)
				for(DasFeature feature:segment.getFeatures())
					if(feature.getFeatureId().equals(featureId)){
						segmentsResponse.add(new DasAnnotatedSegment(segment.getSegmentId(),segment.getStartCoordinate(),segment.getStopCoordinate(),segment.getVersion(),segment.getSegmentLabel(),Collections.singleton(feature)));
						found=true;
					}
			if(!found)
				segmentsResponse.add(new DasUnknownFeatureSegment(featureId));
		}
		return segmentsResponse;
	}

	/**
	 * count the number of times that the type id appears in all the segments
	 */
	public Integer getTotalCountForType(DasType type) throws DataSourceException {
		int count=0;
		for (DasAnnotatedSegment segment:segments)
			for(DasFeature feature:segment.getFeatures())
				if(type.getId().equals(feature.getType().getId()))
					count++;
		return count;
	}

	/**
	 */
	public URL getLinkURL(String field, String id) throws UnimplementedFeatureException, DataSourceException {
		throw new UnimplementedFeatureException("No implemented");
	}

    public String getEntryPointVersion() throws DataSourceException {
        return "1.0";
    }

    /**
     * This method is provided just for testing purposes, it does not retrieve real data.
     * @param start Initial row position on the entry points collection for this server
     * @param stop Final row position ont the entry points collection for this server
     * @return A sub ordered collection of entry points from the start row to the stop row 
     * @throws DataSourceException
     */
    public Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop) throws DataSourceException {
        ArrayList<DasEntryPoint> entryPoints = new ArrayList<DasEntryPoint>();
        if ((start != null) && (stop != null)) {
            for (int i = 1; i <= 10; i++) {
                if ( (start <= i) && (i <= stop) ) {
                    if (i <= 5)
                        entryPoints.add(
                            new DasEntryPoint(
                                "EP_" + i, null, null, null, "1.0",
                                DasEntryPointOrientation.NO_INTRINSIC_ORIENTATION,
                                "Scaffold Entry Point", true));
                    else
                        entryPoints.add(
                            new DasEntryPoint(
                                "EP_" + i, 1, 10, null, "1.0",
                                DasEntryPointOrientation.NO_INTRINSIC_ORIENTATION,
                                "Scaffold Entry Point", true));
                }
            }
        } else {
            for (int i = 1; i <= 10; i++) {
                if (i <= 5)
                        entryPoints.add(
                            new DasEntryPoint(
                                "EP_" + i, null, null, null, "1.0",
                                DasEntryPointOrientation.NO_INTRINSIC_ORIENTATION,
                                "Scaffold Entry Point", true));
                    else
                        entryPoints.add(
                            new DasEntryPoint(
                                "EP_" + i, 1, 10, null, "1.0",
                                DasEntryPointOrientation.NO_INTRINSIC_ORIENTATION,
                                "Scaffold Entry Point", true));
            }
        }
        return entryPoints;
    }

    public int getTotalEntryPoints() throws DataSourceException {
        return 10;
    }

	@Override
	public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins,
			Range rows) throws BadReferenceObjectException,
			DataSourceException, UnimplementedFeatureException {
		//  Auto-generated method stub
		return null;
	}

	@Override
	public Collection<DasAnnotatedSegment> getFeatures(
			Collection<String> featureIdCollection, Integer maxbins, Range rows)
			throws UnimplementedFeatureException, DataSourceException {
		//  Auto-generated method stub
		return null;
	}
}
