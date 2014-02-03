/*
 * Utility class to hold ID and name of dbSnp service options
 */
package com.pfizer.mrbt.genomics.bioservices;

/**
 *
 * @author henstockpv
 */
public class DbSnpSourceOption implements Comparable {
    private int id;
    private String name;
    public DbSnpSourceOption(int id, String name) {
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
    
    /**
     * Returns comparison except that it sorts it in reverse order so last name
     * is first.
     */
    @Override
    public int compareTo(Object other) {
        return -name.compareTo(((DbSnpSourceOption) other).name);
    }
}
