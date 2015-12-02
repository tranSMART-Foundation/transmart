library("WGCNA")


main <- function(aggregate=FALSE){
  msgs = c("")
  df <- loaded_variables[[length(loaded_variables)]]
  good.input <- ncol(df) > 3
  if(aggregate && good.input){
    df <- dropEmptyGene(df)
    aggr  <- aggregate.probes(df)
    assign("preprocessed", aggr, envir = .GlobalEnv)
    discarded.rows <- nrow(df) - nrow(aggr)
    msgs <- paste("Total discarded rows:",discarded.rows)
  }
  else if(aggregate && !good.input){
    msgs <- c("Incorrect subset - in order to perform probe aggregation more than one samples are needed.")
  }else{
    msgs <- c("No preprocessing option selected.")
  }
  list(finished=T,messages=msgs)
}

aggregate.probes <- function(df){
  measurements <- df[,3:ncol(df)]
  row.names(measurements) <- df[,1]
  collapsed <- collapseRows(measurements, df[,2], df[,1], "MaxMean",
                            connectivityBasedCollapsing = FALSE, #in Rmodules = TRUE. In our spec, not required
                            methodFunction = NULL, # It only needs to be specified if method="function"
                            #connectivityPower = 1, # ignored when connectivityBasedCollapsing = FALSE
                            selectFewestMissing = TRUE)
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

dropEmptyGene <- function(d){
  d[!(d$Bio.marker == ""|
        is.null(d$Bio.marker) |
        is.na(d$Bio.marker) |
        is.nan(d$Bio.marker)
      ),]
}
