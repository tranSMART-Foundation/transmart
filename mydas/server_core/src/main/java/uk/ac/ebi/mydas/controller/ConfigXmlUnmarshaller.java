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

import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import uk.ac.ebi.mydas.exceptions.ConfigurationException;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created Using IntelliJ IDEA.
 * Date: 08-May-2007
 * Time: 11:09:54
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class ConfigXmlUnmarshaller {

    /*
    All of the following private static final String values are the names of elements
    and attributes in the XML.
     */
    private static final String ELEMENT_SIMPLE_DAS_SERVER = "mydasserver";

    private static final String ELEMENT_GLOBAL = "global";

    private static final String ELEMENT_BASEURL = "baseurl";

    private static final String ELEMENT_GZIPPED = "gzipped";

    private static final String ELEMENT_DEFAULT_STYLESHEET = "default-stylesheet";

    private static final String ELEMENT_PROPERTY = "property";

    private static final String ELEMENT_DATASOURCES = "datasources";

    private static final String ELEMENT_DATASOURCE = "datasource";

    private static final String ELEMENT_CLASS = "class";

    private static final String ELEMENT_STYLESHEET = "stylesheet";

    private static final String ATTRIBUTE_KEY = "key";

    private static final String ATTRIBUTE_VALUE = "value";

    private static final String ATTRIBUTE_ID = "id";

    private static final String ATTRIBUTE_NAME = "name";

    private static final String ATTRIBUTE_VERSION = "version";

    private static final String ATTRIBUTE_MAPMASTER = "mapmaster";

    private static final String ATTRIBUTE_DESCRIPTION = "description";

    private static final String ATTRIBUTE_DESCRIPTION_HREF = "description-href";


    /**
	 * Define a static logger variable so that it references the
	 * Logger instance named "XMLUnmarshaller".
	 */
	private static final Logger logger = Logger.getLogger(ConfigXmlUnmarshaller.class);

    /**
     * The Factory for the XmlPullParser.
     */
    private static XmlPullParserFactory XPP_FACTORY = null;

    /**
     * String defining the namespace for mydasserverconfig.xml.
     */
    private static final String NAMESPACE = null;

    public ConfigXmlUnmarshaller(){
        if (XPP_FACTORY == null) {
			try {
				XPP_FACTORY = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
				XPP_FACTORY.setNamespaceAware(true);
			} catch (XmlPullParserException xppe) {
				logger.warn(xppe.getMessage(), xppe);
			}
		}
    }

    /**
	 * This method unmarshalls an XML document from the specified Reader into
     * a ServiceConfiguration object that encapsulates all of the configuration.
	 *
	 * @param aReader Reader which reads from the XML file.
	 * @return a ServerConfiguration instance that contains both the global configuration and
     * the configuration of each data source..
	 * @throws ConfigurationException when the reading or parsing failed.
	 */
	public ServerConfiguration unMarshall(Reader aReader) throws ConfigurationException {
        ServerConfiguration serverConfiguration = null;
        try {

			XmlPullParser xpp = XPP_FACTORY.newPullParser();
			xpp.setInput(aReader);
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
					case XmlPullParser.START_DOCUMENT:
						if (logger.isDebugEnabled()) {
							logger.debug("Document start encountered.");
						}
						eventType = xpp.next();
						break;
					case XmlPullParser.START_TAG:
						String start = xpp.getName();
						// Note that we check the version here.
						if (ELEMENT_SIMPLE_DAS_SERVER.equals(start))
						{
							serverConfiguration = processServerConfig(xpp);
							eventType = xpp.getEventType();
						}
						break;
					case XmlPullParser.END_TAG:
						if (logger.isDebugEnabled()) {
							logger.debug("Found end tag " + xpp.getName() + " in namespace " + xpp.getName() + ".");
						}
						eventType = xpp.next();
						break;
					case XmlPullParser.TEXT:
						if (logger.isDebugEnabled()) {
							logger.debug("Found text '" + xpp.getText().trim() + "'.");
						}
						eventType = xpp.next();
						break;
					default:
						eventType = xpp.next();
						break;
				}
			}
		} catch (XmlPullParserException xppe) {
			throw new ConfigurationException("There is a fatal problem with the configuration XML file that has caused XML parsing to fail.", xppe);
		} catch (IOException ioe){
            throw new ConfigurationException("An IOException has been thrown when attempting to parse the configuration XML file.", ioe);
        }

        return serverConfiguration;
    }

    /**
     * This method parses the /mydasserver element.
     * @param xpp the current XmlPullParser instance
     * @return a ServerConfiguration object that contains all of the configuration (both global and individual data sources)
     * @throws XmlPullParserException in the event of a fatal error in the XML file, e.g. not being well-formed.
     * @throws IOException in the event of a problem with reading the XML file.
     * @throws ConfigurationException in the event of a problem with the content of the XML file,
     * e.g. not being valid against the XML schema.
     */
    private ServerConfiguration processServerConfig(XmlPullParser xpp)
            throws XmlPullParserException, IOException, ConfigurationException{

        GlobalConfiguration globalConfig = null;
        Map<String, DataSourceConfiguration> dataSourceConfigMap = null;

        while (! (xpp.next() == XmlPullParser.END_TAG && ELEMENT_SIMPLE_DAS_SERVER.equals(xpp.getName()))) {
            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                final String tagName = xpp.getName();
				if (ELEMENT_GLOBAL.equals(tagName)) {
                    globalConfig = processGlobalElement(xpp);
                } else if (ELEMENT_DATASOURCES.equals(tagName)) {
                    dataSourceConfigMap = processDataSources(xpp);
                }
            }
        }

        // TODO Check status of the datamodel, throw errors if need be....

        return new ServerConfiguration(globalConfig, dataSourceConfigMap);
    }


    /**
     * This method parses the /mydasserver/global element .
     * @param xpp the current XmlPullParser instance
     * @return a GlobalConfiguration object that contains the global (non DataSource specific) configuration.
     * @throws XmlPullParserException in the event of a fatal error in the XML file, e.g. not being well-formed.
     * @throws IOException in the event of a problem with reading the XML file.
     * @throws ConfigurationException in the event of a problem with the content of the XML file,
     * e.g. not being valid against the XML schema.
     */
    private GlobalConfiguration processGlobalElement(XmlPullParser xpp) throws IOException, XmlPullParserException, ConfigurationException {
        String baseURL = null;
        String defaultStylesheet = null;
        boolean gzipped = false;
        Map<String, String> globalParameters = new HashMap<String, String>();

        while (! (xpp.next() == XmlPullParser.END_TAG && ELEMENT_GLOBAL.equals(xpp.getName()))) {
            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                final String tagName = xpp.getName();
				if (ELEMENT_BASEURL.equals(tagName)) {
                    String url = processSimpleElementTextOnly (xpp, ELEMENT_BASEURL, true);
                    if (url == null || url.length() == 0){
                        throw new ConfigurationException ("Please check your XML configuration file.  No value has been given for the /mydasserver/global/baseurl element.");
                    }
                    // There is supposed to be a slash on the end of the baseURL.  If not, add it...
                    if (! url.endsWith("/")){
                        baseURL = url + '/';
                    }
                    else {
                        baseURL = url;
                    }
                    // Then check that the baseURL ends with /das/
                    if (! baseURL.endsWith("/das/")){
                        logger.error ("The value (URL) given for /mydasserver/global/baseurl in the MydasServerConfig.xml file (given as '" + url + "') should end with /das/");
                        throw new ConfigurationException("The value (URL) given for /mydasserver/global/baseurl in the MydasServerConfig.xml file (given as '" + url + "') should end with /das/");
                    }
                }
                else if (ELEMENT_GZIPPED.equals(tagName)) {
                    gzipped = "true".equalsIgnoreCase(processSimpleElementTextOnly (xpp, ELEMENT_GZIPPED, true));
                }
                else if (ELEMENT_DEFAULT_STYLESHEET.equals(tagName)) {
                    defaultStylesheet = processSimpleElementTextOnly (xpp, ELEMENT_DEFAULT_STYLESHEET, true);
                }
                else if (ELEMENT_PROPERTY.equals(tagName)){
                    processProperty(xpp, globalParameters);
                }
            }
        }



        if (defaultStylesheet == null || defaultStylesheet.length() == 0){
            throw new ConfigurationException ("Please check your XML configuration file.  No value has been given for the /mydasserver/global/default-stylesheet element.");
        }

        return new GlobalConfiguration(baseURL, gzipped, defaultStylesheet, globalParameters);
    }


    /**
     * This method parses the /mydasserver/datasources element .
     * @param xpp the current XmlPullParser instance
     * @return a List<DataSourceConfiguration> collection that contains all of the data source specific configuration.
     * @throws XmlPullParserException in the event of a fatal error in the XML file, e.g. not being well-formed.
     * @throws IOException in the event of a problem with reading the XML file.
     * @throws ConfigurationException in the event of a problem with the content of the XML file,
     * e.g. not being valid against the XML schema.
     */
    private Map<String, DataSourceConfiguration> processDataSources(XmlPullParser xpp)
            throws IOException, XmlPullParserException, ConfigurationException {

        Map<String, DataSourceConfiguration> dataSourceConfigList = new HashMap<String, DataSourceConfiguration>();

        while (! (xpp.next() == XmlPullParser.END_TAG && ELEMENT_DATASOURCES.equals(xpp.getName()))) {
            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                final String tagName = xpp.getName();
				if (ELEMENT_DATASOURCE.equals(tagName)) {
                    DataSourceConfiguration dsnConfig = processDataSource(xpp);
                    dataSourceConfigList.put(dsnConfig.getId(), dsnConfig);
                }
            }
        }

        if (dataSourceConfigList.size() == 0){
            throw new ConfigurationException("Please check your XML configuration file.  At least one data source must be configure under the /mydasserver/datasources element.");
        }

        return dataSourceConfigList;
    }

    /**
     * This method parses a single /mydasserver/datasources/datasource element .
     * @param xpp the current XmlPullParser instance
     * @return a List<DataSourceConfiguration> collection that contains all of the data source specific configuration.
     * @throws XmlPullParserException in the event of a fatal error in the XML file, e.g. not being well-formed.
     * @throws IOException in the event of a problem with reading the XML file.
     * @throws ConfigurationException in the event of a problem with the content of the XML file,
     * e.g. not being valid against the XML schema.
     */
    private DataSourceConfiguration processDataSource(XmlPullParser xpp)
            throws IOException, XmlPullParserException, ConfigurationException{

        // Retrieve all the attributes of the datasource element first.
        String id = xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_ID);
        String name = xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_NAME);
        String version = nullifyEmptyString(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_VERSION));
        String mapmaster = xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_MAPMASTER);
        String description = nullifyEmptyString(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_DESCRIPTION));
        String hrefString = xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_DESCRIPTION_HREF);
        URL descriptionHref;
        try{
            descriptionHref = (hrefString == null || hrefString.length() == 0)
                ? null
                : new URL(hrefString);
        }
        catch (MalformedURLException murle){
            logger.error ("MalformedURLException thrown when attempting to build a URL from : '" + hrefString + "'");
            throw new ConfigurationException ("Please check the XML configuration file.  The URL '" + hrefString + "' that has have given in the /mydasserver/datasources/datasource/@description-href attribute is not valid.", murle);
        }

        // Now sort out the rest of the bits from child elements.
        String styleSheet = null;
        Map<String, String> dataSourceProperties = new HashMap<String, String>();
        String className = null;

        while (! (xpp.next() == XmlPullParser.END_TAG && ELEMENT_DATASOURCE.equals(xpp.getName()))) {
            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                final String tagName = xpp.getName();
				if (ELEMENT_CLASS.equals(tagName)) {
                    className = processSimpleElementTextOnly(xpp, ELEMENT_CLASS, true);
                }
                else if (ELEMENT_STYLESHEET.equals(tagName)){
                    styleSheet = nullifyEmptyString(processSimpleElementTextOnly(xpp, ELEMENT_STYLESHEET, false));
                }
                else if (ELEMENT_PROPERTY.equals(tagName)){
                    processProperty(xpp, dataSourceProperties);
                }
            }
        }

        // Check for incomplete data.
        if (className == null || className.length() == 0){
            throw new ConfigurationException("Please check your XML configuration file.  No value has been given for one of the /mydasserver/datasources/datasource/class elements.");
        }

        if (id == null || id.length() == 0){
            throw new ConfigurationException("Please check your XML configuration file.  No value has been given for one of the mandatory /mydasserver/datasources/datasource/@id attributes.");
        }

        if (name == null || name.length() == 0){
            throw new ConfigurationException("Please check your XML configuration file.  No value has been given for one of the mandatory /mydasserver/datasources/datasource/@name attributes.");
        }

        if (mapmaster == null || mapmaster.length() == 0){
            throw new ConfigurationException("Please check your XML configuration file.  No value has been given for one of the mandatory /mydasserver/datasources/datasource/@mapmaster attributes.");
        }


        return new DataSourceConfiguration(id, name, version, mapmaster, description, descriptionHref, styleSheet, dataSourceProperties, className);
    }


    /**
     * Helper method that adds a property (key, value pair) to the Map of properties
     * passed in as argument.
     * @param xpp the current XmlPullParser instance.
     * @param propertyMap the Map of properties to be populated.
     */
    private void processProperty(XmlPullParser xpp, Map<String, String> propertyMap) {
        String key = xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_KEY);
        String value = xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_VALUE);
        if (key != null && value != null && key.length() > 0 && value.length() > 0){
            propertyMap.put(key, value);
        }
    }

    /**
     * For optional elements and attributes, this method ensures that they consistently return null, rather
     * than an empty String.
     * @param value being the String (or null) to check.
     * @return a String with 1 or more characters, or null.
     */
    private String nullifyEmptyString(String value){
        if (value != null && value.length() == 0){
            return null;
        }
        else {
            return value;
        }
    }

        /**
         * Parses a simple element that contains only text.
         * If there is no text, returns null, otherwise the text as a String.
         *
         * @param xpp being the parser currently in use.
         * @param elementName being the name of the element being parsed.
         * @param contentMandatory being a boolean indicating if content must be present.
         * If the content is mandatory but is not found, an XMLPullParser exception will be thrown.
         * @return the content of the element, or null if there is none found.
         * @throws IOException if a problem occurs when xpp.getText() is called.
         * @throws XmlPullParserException if any parsing errors occur.
         */
	private String processSimpleElementTextOnly(XmlPullParser xpp, String elementName, boolean contentMandatory) throws XmlPullParserException, IOException {
		if (xpp.getEventType() == XmlPullParser.START_TAG && xpp.isEmptyElementTag()) {
			if (contentMandatory) {
				// Nothing in there - and there should be!
				throw new XmlPullParserException("The mzData element " + elementName + " should have some content, but does not. XML file line " + xpp.getLineNumber());
			}
			// Nothing in there, and that's fine.
			return null;
		}

		String text = null;
		// Progress through the file until the end tag of the sample element is reached.
		while (! (xpp.next() == XmlPullParser.END_TAG && elementName.equals(xpp.getName()))) {
			if (xpp.getEventType() == XmlPullParser.TEXT) {
				String currentText = xpp.getText();
				if (currentText != null && !"".equals(currentText.trim())) {
					text = currentText.trim();
				}
			}
		}
		return text;
	}
}
