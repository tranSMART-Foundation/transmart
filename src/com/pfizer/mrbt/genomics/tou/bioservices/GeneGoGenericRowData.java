package com.pfizer.mrbt.genomics.tou.bioservices;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.pfizer.mrbt.genomics.tou.bioservices.GeneGoBioServConstants;

public class GeneGoGenericRowData {
    
    private Map<String,String> rowData;
    
    public GeneGoGenericRowData(){
        this.rowData = new HashMap<String,String>();
    }
    public GeneGoGenericRowData(Map<String,String> data){
        this.rowData = new HashMap<String,String>();
        rowData.putAll(data);
    }
    public Set<String> getColumnHeaders(){
        if(rowData.keySet() != null ){
            return rowData.keySet();
        }
        return null;
    }
    public void setRowData(Map<String,String> data){
        rowData.putAll(data);
    }
    public String getValueForKey(String key){
        if(rowData.containsKey(key)){
            return (String) rowData.get(key);
        }else
            return null;
    }
    @Override
    public String toString(){
        if(rowData.isEmpty()){
            return "";
        }else{
            StringBuffer sb = new StringBuffer();
            sb.append(getValueForKey(GeneGoBioServConstants.INTERACTION_ID));
            sb.append("|");
            sb.append(getValueForKey(GeneGoBioServConstants.SOURCE_NAME));
            sb.append("|");
            sb.append(getValueForKey(GeneGoBioServConstants.SOURCE_TYPE));
            sb.append("|");
            sb.append(getValueForKey(GeneGoBioServConstants.SOURCE_ID));
            sb.append("|");
            sb.append(getValueForKey(GeneGoBioServConstants.TARGET_NAME));
            sb.append("|");
            sb.append(getValueForKey(GeneGoBioServConstants.TARGET_TYPE));
            sb.append("|");
            sb.append(getValueForKey(GeneGoBioServConstants.TARGET_ID));
            sb.append("|");
            sb.append(getValueForKey(GeneGoBioServConstants.MECH_TYPE));
            sb.append("|");
            sb.append(getValueForKey(GeneGoBioServConstants.INTXN_TYPE));
            sb.append("|");
            sb.append(getValueForKey(GeneGoBioServConstants.ORGANISM));
            sb.append("|");
            sb.append(getValueForKey(GeneGoBioServConstants.TAXON_ID));
            sb.append("|");
            sb.append(getValueForKey(GeneGoBioServConstants.PUBMED));
            sb.append("|");
            sb.append(getValueForKey(GeneGoBioServConstants.DESC));
            return sb.toString();     
        }
    }
}
