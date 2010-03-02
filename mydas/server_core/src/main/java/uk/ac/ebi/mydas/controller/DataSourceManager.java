/*
 * Copyright 2007 Philip Jones, EMBL-European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * For further details of the mydas project, including source code,
 * downloads and documentation, please see:
 *
 * http://code.google.com/p/mydas/
 *
 */

package uk.ac.ebi.mydas.controller;

import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import org.apache.log4j.Logger;

import uk.ac.ebi.mydas.configuration.ConfigurationManager;
import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.Mydasserver;
import uk.ac.ebi.mydas.configuration.ServerConfiguration;
import uk.ac.ebi.mydas.exceptions.ConfigurationException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBException;

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

    private final ServletContext svCon;

    private ServerConfiguration serverConfiguration;
    private ConfigurationManager configManager;

    protected DataSourceManager(ServletContext servletContext) {
        this.svCon = servletContext;
    }

    public void init(GeneralCacheAdministrator cacheAdministrator, String configurationFileName) throws IOException, ConfigurationException {
        loadConfiguration(configurationFileName);
        initialiseDataSources(cacheAdministrator);
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
    	configManager = new ConfigurationManager();
        try{
        	//Changing the reader of the configuration file for a JaxB based component
        	configManager.unmarshal(svCon.getResourceAsStream(fileName));
        	serverConfiguration=configManager.getServerConfiguration();
		} catch (JAXBException e) {
			e.printStackTrace();
        }
    }

    /**
     * If the ServerConfiguration has been loaded, this method will attempt to initialise
     * the data sources.
     * @throws uk.ac.ebi.mydas.exceptions.ConfigurationException in the event that the loadConfiguration method has not been called
     * (a logic error) or has failed to load the expected objects.
     * @param cacheAdministrator being a reference to the GeneralCacheAdministrator.
     */
    private void initialiseDataSources(GeneralCacheAdministrator cacheAdministrator) throws ConfigurationException {
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
                if (dsnConfig.isOK()){
                    // Register a CacheManager object with the dsn, so it can control caching in the servlet.
                    dsnConfig.getDataSource().registerCacheManager(new CacheManager(cacheAdministrator, dsnConfig));
                }
                else {
                    logger.error("Data Source Failed to Load and Initialise: " + dsnConfig.toString());
                }
                // Register the
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

	public ConfigurationManager getConfigManager() {
		return configManager;
	}

}
