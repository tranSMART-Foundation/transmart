/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.pfizer.mrbt.genomics.webservices;

import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author henstockpv
 */
public class RetrievalException extends Exception {
    private RetrievalMethod retrievalMethod = RetrievalMethod.NOT_SPECIFIED;
    private HashMap<String,String> callParameterMap;
    public RetrievalException(String message, HashMap<String,String> callParameterMap) {
        super(message);
        this.printStackTrace();
        this.callParameterMap = callParameterMap;
    }
    
    public RetrievalException(String message, RetrievalMethod retrievalMethod, HashMap<String,String> callParameterMap) {
        super(message);
        this.retrievalMethod = retrievalMethod;
        this.callParameterMap = callParameterMap;
    }
    
    public Set<String> getCallParameters() {
        return this.callParameterMap.keySet();
    }
    
    public String getCallParameterValue(String parameter) {
        return callParameterMap.get(parameter);
    }

    public RetrievalMethod getRetrievalMethod() {
        return retrievalMethod;
    }

    public void setRetrievalMethod(RetrievalMethod retrievalMethod) {
        this.retrievalMethod = retrievalMethod;
    }

    public HashMap<String, String> getCallParamMapping() {
        return callParameterMap;
    }

    public void setCallParameters(HashMap<String, String> callParameters) {
        this.callParameterMap = callParameters;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RetrievalException in ");
        sb.append(retrievalMethod.toString());
        sb.append("\n\t");
        sb.append(this.getMessage());
        sb.append("\n");
        for(String parameter : callParameterMap.keySet()) {
            sb.append("\t");
            sb.append(parameter);
            sb.append(" -> ");
            sb.append( getCallParameterValue(parameter));
            sb.append("\n");
        }
        return sb.toString();
    }
    
    
}
