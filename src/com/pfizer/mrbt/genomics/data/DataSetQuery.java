/*
 * Utility class to keep track of DataSets and query IDs for tracking the
 * query outcomes
 */

package com.pfizer.mrbt.genomics.data;

/**
 *
 * @author henstockpv
 */
public class DataSetQuery {
    private DataSet dataSet;
    private int queryId;

    public DataSetQuery(DataSet dataSet, int queryId) {
        this.dataSet = dataSet;
        this.queryId = queryId;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public int getQueryId() {
        return queryId;
    }
    
}
