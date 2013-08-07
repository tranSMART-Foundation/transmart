package org.transmartproject.das.mydas

import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import transmart.mydas.DasService
import uk.ac.ebi.mydas.configuration.DataSourceConfiguration
import uk.ac.ebi.mydas.configuration.PropertyType
import uk.ac.ebi.mydas.datasource.AnnotationDataSource
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException
import uk.ac.ebi.mydas.exceptions.DataSourceException
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
import uk.ac.ebi.mydas.model.*

import javax.servlet.ServletContext

/**
 * Created with IntelliJ IDEA.
 * User: Ruslan Forostianov
 * Date: 31/07/2013
 * Time: 10:31
 * To change this template use File | Settings | File Templates.
 */
class AcghDS implements RangeHandlingAnnotationDataSource {

    DasService dasService
    Long resultInstanceId
    List<DasEntryPoint> entryPoints = null

    @Override
    void init(ServletContext servletContext, Map<String, PropertyType> stringPropertyTypeMap, DataSourceConfiguration dataSourceConfiguration) throws DataSourceException {
        resultInstanceId = dataSourceConfiguration.getDataSourceProperties().get("resultInstanceId").getValue().toLong();
        def ctx = servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
        this.dasService = ctx.dasService
    }

    @Override
    void destroy() {}

    @Override
    DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins) throws BadReferenceObjectException, DataSourceException {
        dasService.getAcghFeatures(resultInstanceId, [segmentId], maxbins).first()
    }

    @Override
    Collection<DasAnnotatedSegment> getFeatures(Collection<String> segmentIds, Integer maxbins) throws UnimplementedFeatureException, DataSourceException {
        dasService.getAcghFeatures(resultInstanceId, segmentIds, maxbins)
    }

    @Override
    DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins, uk.ac.ebi.mydas.model.Range range) throws BadReferenceObjectException, DataSourceException, UnimplementedFeatureException {
        dasService.getAcghFeatures(resultInstanceId, [segmentId], maxbins, range).first()
    }

    @Override
    Collection<DasAnnotatedSegment> getFeatures(Collection<String> segmentIds, Integer maxbins, uk.ac.ebi.mydas.model.Range range) throws UnimplementedFeatureException, DataSourceException {
        dasService.getAcghFeatures(resultInstanceId, segmentIds, maxbins, range)
    }

    @Override
    DasAnnotatedSegment getFeatures(String segmentId, int start, int stop, Integer maxbins) throws BadReferenceObjectException, CoordinateErrorException, DataSourceException {
        return dasService.getAcghFeatures(resultInstanceId, [segmentId], maxbins, new uk.ac.ebi.mydas.model.Range(start, stop)).first()
    }

    @Override
    DasAnnotatedSegment getFeatures(String segmentId, int start, int stop, Integer maxbins, Range rows) throws BadReferenceObjectException, CoordinateErrorException, DataSourceException, UnimplementedFeatureException {
        return dasService.getAcghFeatures(resultInstanceId, [segmentId], maxbins, rows).first()
    }

    @Override
    Collection<DasType> getTypes() throws DataSourceException {
        dasService.acghDasTypes
    }

    //Optional
    @Override
    Integer getTotalCountForType(DasType dasType) throws DataSourceException { null }

    //Optional
    @Override
    URL getLinkURL(String s, String s1) throws UnimplementedFeatureException, DataSourceException { null }

    @Override
    Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop) throws UnimplementedFeatureException, DataSourceException {
        if(entryPoints == null) {
            entryPoints = dasService.getAcghEntryPoints(resultInstanceId)
        }
        entryPoints
    }

    @Override
    String getEntryPointVersion() throws UnimplementedFeatureException, DataSourceException {
        dasService.acghEntryPointVersion
    }

    @Override
    int getTotalEntryPoints() throws UnimplementedFeatureException, DataSourceException {
        entryPoints.size()
    }

}
