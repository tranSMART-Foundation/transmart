/*
 * Utility class to hold ID and name of gene source and ID
 */
package com.pfizer.mrbt.genomics.bioservices;

/**
 *
 * @author henstockpv
 */
public class GeneSourceOption {
    private int id;
    private String name;
    public GeneSourceOption(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
