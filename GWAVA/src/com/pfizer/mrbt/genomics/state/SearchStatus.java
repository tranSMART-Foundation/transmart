/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.pfizer.mrbt.genomics.state;

/**
 *
 * @author henstockpv
 */
public enum SearchStatus {
    SUCCESS ("Success"),
    FAILED  ("Failed"),
    WORKING  ("Working"),
    WAITING   ("Waiting"),
    NOT_FOUND("Not found");
    
    private String message;
    SearchStatus(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}
