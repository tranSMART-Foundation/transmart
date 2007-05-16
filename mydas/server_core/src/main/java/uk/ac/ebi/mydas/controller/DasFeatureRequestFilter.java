package uk.ac.ebi.mydas.controller;

import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasGroup;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Returns true if the tested DasFeature passes all the filters.
     * @param feature being the DasFeature under test.
     * @return a boolean - true if the DasFeature passes the filter, false otherwise
     */
    boolean featurePasses(DasFeature feature){

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
