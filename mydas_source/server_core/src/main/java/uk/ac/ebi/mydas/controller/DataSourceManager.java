package uk.ac.ebi.mydas.controller;

import org.apache.log4j.Logger;
import uk.ac.ebi.mydas.exceptions.ConfigurationException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 16:29:45
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class DataSourceManager {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named "AbstractKrakenDataSource".
     */
    private static final Logger logger = Logger.getLogger(DataSourceManager.class);

    private ServletContext svCon;

    private ServerConfiguration serverConfiguration;

    protected DataSourceManager(ServletContext servletContext) {
        this.svCon = servletContext;
    }

    public void init(String configurationFileName) throws IOException, ConfigurationException {
        loadConfiguration(configurationFileName);
        initialiseDataSources();
    }

    /**
     * Loads the XML configuration, including both global configuration and configuration of
     * individual data sources.
     * @param fileName being the name of the configuration XML file.
     * @throws uk.ac.ebi.mydas.exceptions.ConfigurationException if the XML file is badly formed or does
     * not validate against the schema.
     * @throws java.io.IOException in the event of a problem with reading the file.
     */
    private void loadConfiguration(String fileName) throws IOException, ConfigurationException {
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(svCon.getResourceAsStream(fileName)));
            ConfigXmlUnmarshaller unmarshaller = new ConfigXmlUnmarshaller();
            serverConfiguration = unmarshaller.unMarshall(reader);
        }
        finally{
            if (reader != null){
                reader.close();
            }
        }
    }

    /**
     * If the ServerConfiguration has been loaded, this method will attempt to initialise
     * the data sources.
     * @throws uk.ac.ebi.mydas.exceptions.ConfigurationException in the event that the loadConfiguration method has not been called
     * (a logic error) or has failed to load the expected objects.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException in the event that a problem
     * occurs when initialising an individual DataSource.
     */
    private void initialiseDataSources() throws ConfigurationException {
        if (serverConfiguration == null){
            throw new ConfigurationException ("An attempt to initialise the data sources has been made, but there is no valid ServerConfiguration object.");
        }
        if (serverConfiguration.getGlobalConfiguration() == null){
            throw new ConfigurationException ("An attempt to initilise the data sources has been made, but the Global Configuration has not been loaded.");
        }
        // Iterate over the DSN configs and attempt to initialise each in turn.
        for (DataSourceConfiguration dsnConfig : serverConfiguration.getDataSourceConfigMap().values()){
            try{
                // Load and initialise the DSN.
                if (dsnConfig.loadDataSource()){
                    dsnConfig.getDataSource().init(svCon, serverConfiguration.getGlobalConfiguration().getGlobalParameters(), dsnConfig);
                }
                if (! dsnConfig.isOK()){
                    logger.error("Data Source Failed to Load and Initialise: " + dsnConfig.toString());
                }
            } catch (DataSourceException e) {
                // This particular data source has failed to initalise.  Still try to do the rest and log this failure.
                logger.error("Data Source Failed to Load and Initialise: " + dsnConfig.toString());
            }
        }
    }

    /**
     * Calls the destroy method on all of the registered
     * DataSources.
     *
     * If any of them throw exceptions, just logs this and continues
     * on to the rest.
     */
    public void destroy() {
        for (DataSourceConfiguration dataSourceConfiguration : serverConfiguration.getDataSourceConfigMap().values()){
            try{
                if (dataSourceConfiguration.isOK()){
                    dataSourceConfiguration.getDataSource().destroy();
                }
            }
            catch (Exception e){
                // Don't want to barfe out here - this datasource may have
                // failed, but should continue on to try and destroy any / all
                // other data sources.
                logger.error("Exception thrown by dataSourceConfiguration " + dataSourceConfiguration.getName(), e);
            }
        }
    }

    /**
     * Getter for the loaded ServerConfiguration object.
     * @return the loaded ServerConfiguration object.
     */
    public ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }
}
