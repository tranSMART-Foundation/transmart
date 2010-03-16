package uk.ac.ebi.mydas.configuration;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import uk.ac.ebi.mydas.exceptions.ConfigurationException;

/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 * 
 * This class is in charge of the unmarshalling of the configuration file.
 *
 */
public class ConfigurationManager {

	private static final Logger logger = Logger.getLogger(ConfigurationManager.class);
	
	/**
	 * Document that includes the configuration in the JAXb generated model
	 */
	private Mydasserver configurationDocument;

	/**
	 * Unmashall an inputstream that contains the configuration file and create the model for it in to the Mydasserver attribute
	 * @param inputStream Stream with the content of a configuration file who follows the XML scheme
	 * @throws JAXBException in case the unmarshalling fails
	 */
	public void unmarshal( InputStream inputStream ) throws JAXBException {
//		String packageName = docClass.getPackage().getName();
		JAXBContext jc = JAXBContext.newInstance( "uk.ac.ebi.mydas.configuration" );
		Unmarshaller u = jc.createUnmarshaller();
		this.configurationDocument = (Mydasserver)u.unmarshal( inputStream );
	}
	/**
	 * Maps the information of the configurationDocument into a ServerConfiguration object 
	 * @return an object that includes both global and sources configuration
	 * @throws ConfigurationException
	 */
	public ServerConfiguration getServerConfiguration() throws ConfigurationException{
        GlobalConfiguration globalConfig = this.getGlobalConfiguration();
        Map<String, DataSourceConfiguration> dataSourceConfigMap = getDataSourceConfigMap();
        ServerConfiguration serverConfig=new ServerConfiguration(globalConfig,dataSourceConfigMap);
		return serverConfig;		
	}
	/**
	 * Maps the global information of the configurationDocument into a GlobalConfiguration object 
	 * @return an object that includes the global configuration
	 */
	private GlobalConfiguration getGlobalConfiguration(){
        String baseURL = this.configurationDocument.getGlobal().getBaseurl().getValue();
        String defaultStylesheet = this.configurationDocument.getGlobal().getDefaultStylesheet();
        boolean gzipped =(this.configurationDocument.getGlobal().getGzipped()!=null)?this.configurationDocument.getGlobal().getGzipped().value:false;
        boolean slashDasToDsn = this.configurationDocument.getGlobal().getSlashDasPointsToDsn()!=null?this.configurationDocument.getGlobal().getSlashDasPointsToDsn().value:false;
        String dsnXSLT = this.configurationDocument.getGlobal().getDsnXsltUrl();
        String dnaXSLT = this.configurationDocument.getGlobal().getDnaXsltUrl();
        String entryPointXSLT = this.configurationDocument.getGlobal().getEntryPointsXsltUrl();
        String sequenceXSLT = this.configurationDocument.getGlobal().getSequenceXsltUrl();
        String featuresXSLT = this.configurationDocument.getGlobal().getFeaturesXsltUrl();
        String typesXSLT = this.configurationDocument.getGlobal().getTypesXsltUrl();
        Map<String, String> globalParameters = new HashMap<String, String>();
        for (PropertyType property: this.configurationDocument.getGlobal().getProperty())
        	globalParameters.put(property.key, property.value);
        
        return new GlobalConfiguration(baseURL,
                defaultStylesheet,
                gzipped,
                slashDasToDsn,
                dsnXSLT,
                dnaXSLT,
                entryPointXSLT,
                sequenceXSLT,
                featuresXSLT,
                typesXSLT,
                globalParameters);
	}
	/**
	 * Maps the information of sources of the configurationDocument into a set of DataSourceConfiguration objects
	 * 
	 * WARNING: this method have changed just to support the new format of the configuration, 
	 * but it requires more work on it because here is working under the assumption of 1 version and 1 coordinate system.  
	 * 
	 * @return hash table that has as keys the id of the source, and as value a DataSourceConfiguration object
	 * @throws ConfigurationException in case the XML configuration file has some empty fields or malformed URLs
	 */
	private Map<String, DataSourceConfiguration> getDataSourceConfigMap() throws ConfigurationException{
        Map<String, DataSourceConfiguration> dataSourceConfigList = new HashMap<String, DataSourceConfiguration>();
        for (uk.ac.ebi.mydas.configuration.Mydasserver.Datasources.Datasource datasource:this.configurationDocument.getDatasources().getDatasource()){
            String id = datasource.getUri().toString();
            String name = datasource.getTitle();
            String mapmaster = datasource.getVersion().get(0).getCoordinates().get(0).uri;
            String hrefString = datasource.getDocHref();
            try{
                new URL(hrefString);
            }
            catch (MalformedURLException murle){
                logger.error ("MalformedURLException thrown when attempting to build a URL from : '" + hrefString + "'");
                throw new ConfigurationException ("Please check the XML configuration file.  The URL '" + hrefString + "' that has have given in the /mydasserver/datasources/datasource/@description-href attribute is not valid.", murle);
            }
            
            String className = datasource.getVersion().get(0).getClazz();
            Map<String, String> dataSourceProperties = new HashMap<String, String>();
            for (PropertyType property: datasource.getProperty())
            	dataSourceProperties.put(property.key, property.value);
            // Check for incomplete data.

            if (className == null){
                throw new ConfigurationException("Please check your XML configuration file.  No value has been given for one of the /mydasserver/datasources/datasource/class elements.");
            }

            if (id == null){
                throw new ConfigurationException("Please check your XML configuration file.  No value has been given for one of the mandatory /mydasserver/datasources/datasource/@id attributes.");
            }

            if (name == null){
                throw new ConfigurationException("Please check your XML configuration file.  No value has been given for one of the mandatory /mydasserver/datasources/datasource/@name attributes.");
            }

            if (mapmaster == null){
                throw new ConfigurationException("Please check your XML configuration file.  No value has been given for one of the mandatory /mydasserver/datasources/datasource/@mapmaster attributes.");
            }
            for (int i=0;i<datasource.getVersion().size();i++){
	            DataSourceConfiguration dsnConfig=new DataSourceConfiguration(datasource,i);
	            dataSourceConfigList.put(dsnConfig.getId(), dsnConfig);
            }
        }
        	
        return dataSourceConfigList;
		
	}

}
