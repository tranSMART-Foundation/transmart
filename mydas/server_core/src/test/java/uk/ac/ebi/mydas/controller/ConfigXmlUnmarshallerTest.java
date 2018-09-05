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

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import uk.ac.ebi.mydas.configuration.ConfigurationManager;
import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.configuration.ServerConfiguration;
import uk.ac.ebi.mydas.exceptions.ConfigurationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;

import javax.xml.bind.JAXBException;

/**
 * Created Using IntelliJ IDEA.
 * Date: 08-May-2007
 * Time: 14:03:06
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class ConfigXmlUnmarshallerTest extends TestCase {

    /**
	 * Define a static logger variable so that it references the
	 * Logger instance named "XMLUnmarshaller".
	 */
	private static Logger logger = Logger.getLogger(ConfigXmlUnmarshallerTest.class);

    public ConfigXmlUnmarshallerTest() {
        super();
    }

    public ConfigXmlUnmarshallerTest(String string) {
        super(string);
    }

    /**
     * This test case performs a complete test of the XML Unmarshaller, and the constructors
     * and getter methods of the
     * DataSourceConfiguration, DataSourceManager and GlobalConfiguration classes.
     *
     * After loading the test XML file, performs a comprehensive check of every object and
     * every getter for every object in the resulting configuration.
     * @throws ConfigurationException in the event of a failure in reading the XML file.
     */
    public void testUnMarshaller() throws ConfigurationException{
		ConfigurationManager cm = new ConfigurationManager();
        try{
            ClassLoader thisClassLoader = this.getClass().getClassLoader();
        	try {
				cm.unmarshal(thisClassLoader.getResourceAsStream("MydasServerConfig.xml"));
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            ServerConfiguration serverConfig = cm.getServerConfiguration();
            Assert.assertNotNull("The unmarshaller has returned a null ServerConfiguration.",
                    serverConfig);
            Assert.assertNotNull("The GlobalConfiguration is null",
                    serverConfig.getGlobalConfiguration());
            Assert.assertEquals("The baseurl is not as expected",
                    "http://www.ebi.ac.uk/das-srv/uniprot/das/",
                    serverConfig.getGlobalConfiguration().getBaseURL());
            Assert.assertTrue("The gzipped value should be true", serverConfig.getGlobalConfiguration().isGzipped());
            Assert.assertEquals("The default stylesheet is not set as expected",
                    "mydasStyle.style",
                    serverConfig.getGlobalConfiguration().getDefaultStyleSheet());
            Assert.assertEquals("The number of global properties is not as expected.",
                    2,
                    serverConfig.getGlobalConfiguration().getGlobalParameters().size());
            //Properties also includes visibility (since 1.6.1)
            Map<String, PropertyType> globalProps = serverConfig.getGlobalConfiguration().getGlobalParameters();
            Assert.assertEquals("Missing global property",
                    globalProps.get("TestKeyGlobal1").getValue(),
                    "TestValueGlobal1");
            Assert.assertEquals("Visibility should be false in TestKeyGlobal1 property",
                    globalProps.get("TestKeyGlobal1").isVisibility(),
                    false);
            Assert.assertEquals("Missing global property",
                    globalProps.get("TestKeyGlobal2").getValue(),
                    "TestValueGlobal2");
            Assert.assertEquals("Visibility should be true in TestKeyGlobal2 property",
                    globalProps.get("TestKeyGlobal2").isVisibility(),
                    true);
            Assert.assertEquals("The number of dsns is not as expected",
                    2,
                    serverConfig.getDataSourceConfigs().size());
            //DSN collection
            Collection<DataSourceConfiguration> dsnCollection = serverConfig.getDataSourceConfigs();
            boolean found1 = false;
            boolean found2 = false;
            for (DataSourceConfiguration dsnConfig : dsnCollection){
                Assert.assertTrue("An unexpected dsn ID has been found",
                        ("http://www.ebi.ac.uk/dsnId1".equals (dsnConfig.getId()) || "http://www.ebi.ac.uk/dsnId2".equals (dsnConfig.getId())));
                if ("http://www.ebi.ac.uk/dsnId1".equals (dsnConfig.getId())){
                    found1 = true;
                    Assert.assertEquals("Unexpected dsn name",
                            dsnConfig.getName(),
                            "dsnName1");
                    Assert.assertEquals("Unexpected dsn version",
                            dsnConfig.getVersion(),
                            "2010-03-01");
                    Assert.assertEquals("Unexpected mapmaster",
                            dsnConfig.getMapmaster(),
                            "http://dsnId1.mapmaster");
                    Assert.assertEquals("Unexpected description",
                            dsnConfig.getDescription(),
                            "dsnDescription1");
                    Assert.assertEquals("Unexpected description-href",
                            dsnConfig.getDescriptionHref().toString(),
                            "http://dsnDescriptionHref1");
                    Assert.assertEquals("Unexpected stylesheet",
                            dsnConfig.getStyleSheet(),
                            "override_style");
                    Assert.assertEquals("Unexpected number of properties",
                            2,
                            dsnConfig.getDataSourceProperties().size());
                    //Properties
                    Map<String, PropertyType> dsnProps = dsnConfig.getDataSourceProperties();
                    Assert.assertEquals("Missing dsn property",
                            dsnProps.get("dsn1key1").getValue(),
                            "dsn1value1");
                    Assert.assertEquals("Missing dsn property",
                            dsnProps.get("dsn1key2").getValue(),
                            "dsn1value2");
                    Assert.assertEquals("Visibility should be true in dsn1key2 property",
                            dsnProps.get("dsn1key2").isVisibility(),
                            true);
                    Assert.assertTrue("dna-command-enabled not as expected", dsnConfig.isDnaCommandEnabled());
                    //Assert.assertTrue("features-strictly-enclosed not as expected", dsnConfig.isFeaturesStrictlyEnclosed());
                    Assert.assertTrue("use-feature-id-for-feature-label", dsnConfig.isUseFeatureIdForFeatureLabel());
                    Assert.assertTrue("include-types-with-zero-count", dsnConfig.isIncludeTypesWithZeroCount());
                }
                else if ("http://www.ebi.ac.uk/dsnId2".equals (dsnConfig.getId())){
                    found2 = true;
                    Assert.assertEquals("Unexpected dsn name",
                            dsnConfig.getName(),
                            "dsnName2");
                    Assert.assertEquals("Unexpected dsn version",
                            dsnConfig.getVersion(),
                            "2010-03-01");
                    Assert.assertEquals("Unexpected mapmaster",
                            dsnConfig.getMapmaster(),
                            "http://dsnId2.mapmaster");
                    Assert.assertEquals("Unexpected description",
                            dsnConfig.getDescription(),
                            "dsnDescription2");
                    Assert.assertEquals("Unexpected description-href",
                            dsnConfig.getDescriptionHref().toString(),
                            "http://dsnDescriptionHref2");
                    Assert.assertNull("Unexpected stylesheet",
                            dsnConfig.getStyleSheet());
                    Assert.assertEquals("Unexpected number of properties",
                            0,
                            dsnConfig.getDataSourceProperties().size());
                    Assert.assertFalse("dna-command-enabled not as expected", dsnConfig.isDnaCommandEnabled());
                    Assert.assertFalse("use-feature-id-for-feature-label", dsnConfig.isUseFeatureIdForFeatureLabel());
                    Assert.assertFalse("include-types-with-zero-count", dsnConfig.isIncludeTypesWithZeroCount());
                }
            }
            Assert.assertTrue("Did not find dsn1 definition.", found1);
            Assert.assertTrue("Did not find dsn2 definition.", found2);
        }
        finally{
//            if (reader != null){
//                try {
//                    reader.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }
}
