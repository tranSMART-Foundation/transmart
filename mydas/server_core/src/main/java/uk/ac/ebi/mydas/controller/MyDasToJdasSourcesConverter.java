/*
 * Copyright 2012 jw12.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.mydas.controller;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.biodas.jdas.creators.CreateSources;
import org.biodas.jdas.schema.sources.*;
import uk.ac.ebi.mydas.configuration.Mydasserver.Datasources.Datasource;
import uk.ac.ebi.mydas.configuration.Mydasserver.Datasources.Datasource.Version;
import uk.ac.ebi.mydas.configuration.Mydasserver.Datasources.Datasource.Version.Capability;
import uk.ac.ebi.mydas.configuration.Mydasserver.Datasources.Datasource.Version.Coordinates;
import uk.ac.ebi.mydas.configuration.PropertyType;

/**
 *
 * @author jw12
 */
class MyDasToJdasSourcesConverter {
    CreateSources createSources=new CreateSources();
    ObjectFactory factory=new ObjectFactory();
    
    public SOURCES convertMyDasToJdasSources(DataSourceManager dataSourceManager, HttpServletRequest request, HttpServletResponse response, String queryString, String source){
        
                       // All fine.
        // Get the list of dsn from the DataSourceManager
       
        List<String> dsns = dataSourceManager.getServerConfiguration().getDsnNames();
                List<String> versionsadded = new ArrayList<String>();
                List<SOURCE> srcs=new ArrayList<SOURCE>();
                for (String dsn : dsns) {
                    if (source == null || source.equals(dsn)) {
                        
                    SOURCE src=factory.createSOURCE();
                        if (!versionsadded.contains(dsn)) {
                            Datasource dsnConfig2 = dataSourceManager.getServerConfiguration().getDataSourceConfig(dsn).getConfig();
                            src.setUri(dsnConfig2.getUri());
                            if (dsnConfig2.getDocHref() != null && dsnConfig2.getDocHref().length() > 0) {
                                src.setDocHref(dsnConfig2.getDocHref());
                            }
                            src.setTitle(dsnConfig2.getTitle());
                            src.setDescription(dsnConfig2.getDescription());
                            MAINTAINER m=factory.createMAINTAINER();
                            m.setEmail(dsnConfig2.getMaintainer().getEmail());
                            src.setMAINTAINER(m);
                            for (Version version : dsnConfig2.getVersion()) {
                                VERSION v=factory.createVERSION();
                                versionsadded.add(version.getUri());
                                v.setUri(version.getUri());
                                v.setCreated(version.getCreated().toString());
                                for (Coordinates coordinates : version.getCoordinates()) {
                                    COORDINATES coord=factory.createCOORDINATES();
                                    coord.setUri(coordinates.getUri());
                                    coord.setSource(coordinates.getSource());
                                    coord.setAuthority(coordinates.getAuthority());
                                    if ((coordinates.getTaxid() != null) && (coordinates.getTaxid().length() > 0)) {
                                        coord.setTaxid(coordinates.getTaxid());
                                    }
                                    if ((coordinates.getVersion() != null) && (coordinates.getVersion().length() > 0)) {
                                        coord.setVersion(coordinates.getVersion());
                                    }
                                    coord.setTestRange(coordinates.getTestRange());
                                    coord.setContent(coordinates.getValue());
                                    v.getCOORDINATES().add(coord);
                                }
                                for (Capability capability : version.getCapability()) {
                            CAPABILITY cap = factory.createCAPABILITY();
                                    cap.setType(capability.getType());
                                    if ((capability.getQueryUri() != null) && (capability.getQueryUri().length() > 0)) {
                                        cap.setQueryUri(capability.getQueryUri());
                                    }
                                    v.getCAPABILITY().add(cap);
                                }
                                //1.6.1 Properties come from version and are not allowed in data sources (not out of the version anyway)
                                //1.61. Only properties with visibility true will be reported in source command response
                                for (PropertyType pt : version.getProperty()) {
                                    if (pt.isVisibility()) {
                                PROP prop = factory.createPROP();
                                        prop.setName(pt.getKey());
                                        prop.setValue(pt.getValue());
                                        }
                                }
                                src.getVERSION().add(v);
                            }
                            
                        }
                        srcs.add(src);
                    }
                }
                SOURCES sources=createSources.createSources(srcs);
                return sources;
    }
}
