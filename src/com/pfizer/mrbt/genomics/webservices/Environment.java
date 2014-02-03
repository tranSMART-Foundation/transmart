/*
 * Environment Stage/Dev/? to be extended as necessary
 */

package com.pfizer.mrbt.genomics.webservices;

/**
 *
 * @author henstockpv
 */
public enum Environment {
    PRODUCTION ("Production"),
    STAGE ("Stage"),
    DEV   ("Dev");
    
    private final String displayStr;
    Environment(String displayStr) {
        this.displayStr = displayStr;
    }
    
    public String getDisplayStr() {
        return this.displayStr;
    }
}

