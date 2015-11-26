library("WGCNA")


main <- function(aggregate=FALSE){
    if(aggregate){
        df <- loaded_variables[[length(loaded_variables)]]
        loaded_variables[[length(loaded_variables)]] <- aggregate.probes(df)
    }
}

aggregate.probes <- function(df){
    measurements <- df[,3:ncol(df)]
    rownames(measurements) <- df[,1]
    collapsed <- collapseRows(measurements, df[,2], df[,1], "MaxMean")
    collapsedMeasurements <- collapsed$datETcollapsed
    Bio.marker <- collapsed$group2row[,1] # first column of this matrix always contains gene
    Row.Label <- collapsed$group2row[,2]  # second column of this matrix always contains probe_id
    lastColIndex <- ncol(df)
    lastbutOne <- lastColIndex -1
    df <- data.frame(collapsedMeasurements)
    df["Bio.marker"] <- Bio.marker
    df["Row.Label"] <- Row.Label
    row.names(df) <- NULL # WGCNA adds row.names. We do not need them to be set
    df[,c(lastColIndex, lastbutOne , 1:(lastbutOne-1))]
}
