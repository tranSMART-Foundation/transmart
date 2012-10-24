package uk.ac.ebi.mydas.examples;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletContext;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.extendedmodel.DasUnknownFeatureSegment;
import uk.ac.ebi.mydas.model.*;

public class GenotypeDataSource implements RangeHandlingAnnotationDataSource{
	ServletContext svCon;
	Map<String, PropertyType> globalParameters;
	DataSourceConfiguration config;
	GenotypeManager genotypeManager;

	public void init(ServletContext servletContext,
			Map<String, PropertyType> globalParameters,
			DataSourceConfiguration dataSourceConfig)
			throws DataSourceException {
		this.svCon = servletContext;
		this.globalParameters = globalParameters;
		this.config = dataSourceConfig;
		   String databaseUrl="";
		   String databasePass="";
		   String databaseUser="";
		if (config.getDataSourceProperties().containsKey("databaseUrl")){
			   databaseUrl=config.getDataSourceProperties().get("databaseUrl").getValue();
		   }
		if (config.getDataSourceProperties().containsKey("databaseUser")){
			   databaseUser=config.getDataSourceProperties().get("databaseUser").getValue();
		   }
		if (config.getDataSourceProperties().containsKey("databasePass")){
			   databasePass=config.getDataSourceProperties().get("databasePass").getValue();
		   }
		if(databaseUrl.equals("") || databaseUrl==null || databaseUser.equals("") || databaseUser==null|| databasePass.equals("") || databasePass==null){
			throw new DataSourceException("a database url must be set such in the configuration for example: ");
		}
		try {
			System.out.println("connection params="+databaseUrl+" user:"+databaseUser+" pass:"+databasePass);
			genotypeManager=new GenotypeManager(databaseUrl, databaseUser, databasePass);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void destroy() {
		genotypeManager.close();
	}

	public DasAnnotatedSegment getFeatures(String segmentId, int start, int stop, Integer maxbins) 
			throws BadReferenceObjectException, CoordinateErrorException, DataSourceException {
		if (maxbins==null)
			maxbins=-1;
		return genotypeManager.getSubmodelBySegmentId(segmentId, start, stop,maxbins);
	}
	public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins) 
			throws BadReferenceObjectException, DataSourceException {
		if (maxbins==null)
			maxbins=-1;
		return genotypeManager.getSubmodelBySegmentId(segmentId, -1, -1,maxbins);
	}

	
	private boolean lookInside(DasComponentFeature component,String featureId,Collection<DasAnnotatedSegment> segmentsResponse,DasAnnotatedSegment segment) throws DataSourceException{
		if (component.hasSubParts()){
			for (DasComponentFeature subcomponent: component.getReportableSubComponents()){
				if(subcomponent.getFeatureId().equals(featureId)){
					segmentsResponse.add(new DasAnnotatedSegment(segment.getSegmentId(),segment.getStartCoordinate(),segment.getStopCoordinate(),segment.getVersion(),segment.getSegmentLabel(),Collections.singleton((DasFeature)subcomponent)));
					return true;
				}else
					if(this.lookInside(subcomponent, featureId, segmentsResponse, segment))
						return true;
			}
		}
		return false;
	}

	public Collection<DasType> getTypes() throws DataSourceException {
		return genotypeManager.getTypes();
	}
	public URL getLinkURL(String field, String id)
			throws UnimplementedFeatureException, DataSourceException {
		throw new UnimplementedFeatureException("No implemented");
	}


    public String getEntryPointVersion() throws UnimplementedFeatureException, DataSourceException {
        return genotypeManager.getDatabase();
    }

   

    public Integer getTotalCountForType(DasType type)
			throws DataSourceException {
		return genotypeManager.getTotalCountForType(type.getId());
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

	@Override
	public DasAnnotatedSegment getFeatures(String segmentId, int start,
			int stop, Integer maxbins, Range rows)
			throws BadReferenceObjectException, CoordinateErrorException,
			DataSourceException, UnimplementedFeatureException {
		//  Auto-generated method stub
		return null;
	}

	@Override
	public Collection<DasAnnotatedSegment> getFeatures(
			Collection<String> featureIdCollection, Integer maxbins)
			throws UnimplementedFeatureException, DataSourceException {
		//  Auto-generated method stub
		return null;
	}

	@Override
	public Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop)
			throws UnimplementedFeatureException, DataSourceException {
		//  Auto-generated method stub
		return null;
	}

	@Override
	public int getTotalEntryPoints() throws UnimplementedFeatureException,
			DataSourceException {
		//  Auto-generated method stub
		return 0;
	}
}
