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

import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created Using IntelliJ IDEA.
 * Date: 15-May-2007
 * Time: 14:12:18
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * This class is used to filter features, according to the details of the request from
 * the user.
 */
public class DasFeatureRequestFilter {

    private Collection<String> typeIds = null;

    private Collection<String> categoryIds = null;

    private Collection<String> featureIds = null;

    private Collection<String> groupIds = null;


    DasFeatureRequestFilter() {
        // Does nothing - Collections are lazy instantiated and populated by add methods.
    }

    void addTypeId (String typeId){
        if (typeId != null){
            if (typeIds == null) typeIds = new ArrayList<String>();
            typeIds.add (typeId);
        }
    }

    void addCategoryId (String categoryId){
        if (categoryId != null){
            if (categoryIds == null) categoryIds = new ArrayList<String>();
            categoryIds.add (categoryId);
        }
    }

    void addFeatureId (String featureId){
        if (featureId != null){
            if (featureIds == null) featureIds = new ArrayList<String>();
            featureIds.add (featureId);
        }
    }

    void addGroupId (String groupId){
        if (groupId != null){
            if (groupIds == null) groupIds = new ArrayList<String>();
            groupIds.add (groupId);
        }
    }

    boolean containsFeatureIds(){
        return featureIds != null && featureIds.size() > 0;
    }

    boolean containsGroupIds(){
        return groupIds != null && groupIds.size() > 0;
    }

    Collection<String> getFeatureIds(){
        return (featureIds == null) ? Collections.EMPTY_LIST : featureIds;
    }

    Collection<String> getGroupIds(){
        return (groupIds == null) ? Collections.EMPTY_LIST : groupIds;
    }

    /**
     * Returns true if the tested DasFeature passes all the filters.
     * @param feature being the DasFeature under test.
     * @return a boolean - true if the DasFeature passes the filter, false otherwise
     */
    public boolean featurePasses(DasFeature feature){

        if (!(featureIds == null || (feature.getFeatureId() != null && featureIds.contains(feature.getFeatureId())))){
            return false;
        }

        if (!(typeIds == null || (feature.getTypeId() != null && typeIds.contains(feature.getTypeId())))){
            return false;
        }

        if (!(categoryIds == null || (feature.getTypeCategory() != null && categoryIds.contains(feature.getTypeCategory())))){
            return false;
        }

        if (groupIds != null){
            if (feature.getGroups() == null){
                return false;
            }

            boolean matchesGroupId = false;
            for (DasGroup dasGroup : feature.getGroups()){
                if (dasGroup.getGroupId() != null && groupIds.contains(dasGroup.getGroupId())){
                    matchesGroupId = true;
                }
            }

            if (! matchesGroupId)
                return false;
        }

        return true;
    }
}
