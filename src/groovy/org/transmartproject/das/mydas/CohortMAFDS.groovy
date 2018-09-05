package org.transmartproject.das.mydas

import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import uk.ac.ebi.mydas.configuration.DataSourceConfiguration
import uk.ac.ebi.mydas.configuration.PropertyType
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource
import uk.ac.ebi.mydas.exceptions.DataSourceException

import javax.servlet.ServletContext

/**
 * Created by rnugraha on 26-09-13.
 */
class CohortMAFDS extends VcfDS implements RangeHandlingAnnotationDataSource {

    @Override
    void init(ServletContext servletContext, Map<String, PropertyType> stringPropertyTypeMap, DataSourceConfiguration dataSourceConfiguration) throws DataSourceException {
        super.init(servletContext, stringPropertyTypeMap, dataSourceConfiguration);
        vcfService = servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT).cohortMAFService;
    }


}
