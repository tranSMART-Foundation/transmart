/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author henstockpv
 */
public class SnpModel2PvalMap {
    private HashMap<String, Double> map = new HashMap<String, Double>();
    private static String DELIMITER = "_";
    
    public SnpModel2PvalMap() {
        
    }

    /**
     * Puts the key associated with snp and model --> pvalue
     * @param snp
     * @param model
     * @param pvalue 
     */
    public void put(SNP snp, Model model, double pvalue) {
        String key = getKey(snp, model);
        //System.out.println(snp.getLoc() + "\t" + model.toString() + key);
        map.put(key, pvalue);
    }

    /**
     * Alternative way of inserting a key with a snp_model string.  This is
     * not the recommended approach.  Use put(snp, model, pvalue)
     * @param snp_model_key
     * @param pvalue 
     */
    public void put(String snp_model_key, double pvalue) {
        map.put(snp_model_key, pvalue);
    }
    
    /**
     * This is not the preferred interface but added to copy values between
     * data sets
     * @param snp_model_key
     * @return 
     */
    public double get(String snp_model_key) {
        return map.get(snp_model_key);
    }
 
    /**
     * Returns the value associated with snp + model.  If not found, it returns
     * null
     * @param snp
     * @param model
     * @return 
     */
    public Double get(SNP snp, Model model) {
        String key = getKey(snp, model);
        return map.get(key);
    }
    
    /**
     * Returns the set of keys (internal representation) associated with the 
     * mapping
     * @return 
     */
    public Set<String> getKeySet() {
        return map.keySet();
    }
    
    /**
     * Returns true if there is ampping entry for key snp + model
     * @param snp
     * @param model
     * @return 
     */
    public boolean containsKey(SNP snp, Model model) {
        String key = getKey(snp, model);
        return map.containsKey(key);
    }
    
    /**
     * Creates a key from the snp and model by inserting DELIMITER between
     * their ID values
     * @param snp
     * @param model
     * @return 
     */
    private String getKey(SNP snp, Model model) {
        String key = snp.getRsId() + DELIMITER + model.getId();
        return key;
    }
    
    /**
     * Returns the modelID from the key
     * @param key
     * @return 
     */
    public int getModelID(String key) {
        String[] tokens = key.split(DELIMITER);
        return Integer.parseInt(tokens[1]);
    }

    /**
     * Returns the SNP's RsId from the key
     * @param key
     * @return 
     */
    public int getRsId(String key) {
        String[] tokens = key.split(DELIMITER);
        return Integer.parseInt(tokens[0]);
    }

    /**
     * Removes all the snp associated with the current model from the map
     * @param model 
     */
    public void removeAllSnpWithModel(Model model) {
        String modelIdStr = model.getId() + "";
        String searchStr = DELIMITER + modelIdStr;
        ArrayList<String> keysToRemove = new ArrayList<String>();
        for(String key : map.keySet()) {
            if(key.endsWith(searchStr)) {
                keysToRemove.add(key);
            }
        }
        for(String keyToRemove : keysToRemove) {
            map.remove(keyToRemove);
        }
    }
}
