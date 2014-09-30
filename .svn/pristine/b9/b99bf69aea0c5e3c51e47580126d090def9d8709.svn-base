/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.bioservices;

import com.pfizer.tnb.api.server.util.QueryResult;
import com.pfizer.tnb.bsutil.BsServiceClientImpl;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author henstockpv
 */
public class Test744Only extends BsServiceClientImpl {
    protected static Logger log = Logger.getLogger(com.pfizer.mrbt.genomics.bioservices.AmiDemo.class.getName());    
    public Test744Only() {
        log.setLevel(Level.DEBUG);
        int service_id = 744;
        String queryStr = "https://bioservicesdev.pfizer.com/TouVis/user/DataServletFiltered?service=744&SERVICE_RENDERID=7&GENE_NAME=TNF&MODEL_ID=122,123,124,125&RANGE=100000&SOURCE_ID=3";
        System.out.println("QueryStr [" + queryStr + "]");
        QueryResult queryResults = getData(queryStr, service_id, -1, -1, true);
        System.out.println("QueryStr results: [" + queryResults.getData().size() + "]");

    }
    public static void main(String argv[]) {
        Test744Only test744Only = new Test744Only();
    }
}
