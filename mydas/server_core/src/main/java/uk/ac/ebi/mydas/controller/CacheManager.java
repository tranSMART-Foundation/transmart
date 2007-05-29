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

/**
 * Created Using IntelliJ IDEA.
 * Date: 23-May-2007
 * Time: 14:03:29
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * The CacheManager is passed to the data source to allow it to control caching
 * in the mydas servlet.  At present, this is limited to a single method allowing
 * the data source to empty the cache (just for the DSN that this
 * CacheManager is registered with).
 */
public class CacheManager {

    /**
     * Reference to the GeneralCacheAdministrator object.
     */
    private final GeneralCacheAdministrator cacheAdministrator;

    /**
     * Reference to the data source configuration.
     */
    private final DataSourceConfiguration dsnConfig;

    /**
     * Constructor for the CacheManager object, that can includes a reference to the GeneralCacheAdministrator
     * and the data source configuration.
     * @param cacheAdministrator
     * @param dsnConfig
     */
    CacheManager (GeneralCacheAdministrator cacheAdministrator, DataSourceConfiguration dsnConfig){
        this.dsnConfig = dsnConfig;
        this.cacheAdministrator = cacheAdministrator;
    }

    /**
     * This method empties the cache (implemented using Open Symphony Cache) for the data source
     * that this CacheManager is registered with.
     */
    public void emptyCache() {
        if (dsnConfig.isOK()){
            cacheAdministrator.flushGroup(dsnConfig.getName());
        }
    }

    /**
     * <b>Use with care</b> - gives the datasource access to the OSCache GeneralCacheManager being
     * used by the mydas server.
     * @return gives the datasource access to the OSCache GeneralCacheManager being
     * used by the mydas server.
     */
    public GeneralCacheAdministrator getCacheAdministrator(){
        return cacheAdministrator;
    }
}
