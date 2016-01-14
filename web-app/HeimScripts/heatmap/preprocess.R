library("WGCNA")

# Pre-processing data saved in preprocessed
# Pre-processing parameters saved in global preprocessing_params
# They can be considered fresh if the variable preprocessed exists

main <- function(aggregate=FALSE){
  msgs = c("")
  df <- mergeFetchedData(loaded_variables)
  good.input <- ncol(df) > 3 && nrow(df) > 0 && sum(df$Bio.marker != "") > 0 # more than one sample, contains any rows, non empty Bio.marker column.
  if(aggregate && good.input){
    df <- dropEmptyGene(df)
    aggr  <- aggregate.probes(df)
    assign("preprocessed", aggr, envir = .GlobalEnv)
    discarded.rows <- nrow(df) - nrow(aggr)
    msgs <- paste("Total discarded rows:",discarded.rows)
  }
  else if(aggregate && !good.input){
    stop("Incorrect subset - in order to perform probe aggregation more than one samples are needed.")
  }else{
    msgs <- c("No preprocessing applied.")
      assign("preprocessed", df, envir = .GlobalEnv)
  }

  assign("preprocessing_params", list(aggregate=aggregate), envir = .GlobalEnv)

  list(finished=T,messages=msgs)
}

aggregate.probes <- function(df){
  if(nrow(df)<2){
    stop("Cannot aggregate probes: there only is data for a single probe (ie. only one row of data) or 
        there is insufficient bio.marker information for the selected probes to be able to match the probes to 
        biomarkers for aggregation (e.g. in case of micro-array data to match probe ID to gene symbol). 
        Suggestion: skip probe aggregation.")
  }
  measurements <- df[,3:ncol(df)]
  row.names(measurements) <- df[,1]
  collapsed <- collapseRows(measurements, df[,2], df[,1], "MaxMean",
                            connectivityBasedCollapsing = FALSE, #in Rmodules = TRUE. In our spec, not required
                            methodFunction = NULL, # It only needs to be specified if method="function"
                            #connectivityPower = 1, # ignored when connectivityBasedCollapsing = FALSE
                            selectFewestMissing = FALSE)
  
  #collapsed returns 0 if collapseRows does not work, otherwise it returns a list
  if(is.numeric(collapsed))
  {
    stop("Probe aggregation is not possible: there are too many missing data points in the data for succesfull probe
aggregation. Skip probe aggregation or select more genes/proteins/metabolites/... 

Note: for aggregation only the data from probes that have accompanying bio.marker information is used
(e.g. in case of micro-array data only the probes are used that have gene symbol information coupled to it).
This could mean the dataset used for aggregation is smaller than the initially selected dataset.")
  }
  collapsedMeasurements <- collapsed$datETcollapsed
  Bio.marker <- collapsed$group2row[,1] # first column of this matrix always contains gene
  Row.Label <- collapsed$group2row[,2]  # second column of this matrix always contains probe_id
  lastColIndex <- ncol(df)
  lastbutOne <- lastColIndex -1
  df <- data.frame(collapsedMeasurements)
  df["Bio.marker"] <- Bio.marker
  df["Row.Label"] <- Row.Label
  row.names(df) <- NULL # WGCNA adds row.names. We do not need them to be set
  return(df[,c(lastColIndex, lastbutOne , 1:(lastbutOne-1))])
 
}

dropEmptyGene <- function(d){
  d[!(d$Bio.marker == ""|
        is.null(d$Bio.marker) |
        is.na(d$Bio.marker) |
        is.nan(d$Bio.marker)
      ),]
}


### duplicated code from utils - when we get sourcing to work it will be moved


mergeFetchedData <- function(listOfHdd){
  df <- listOfHdd[[1]]
  
  #test if the different data.frames all contain the exact same set of probe IDs/metabolites/etc (independent of 
  # order of occurrence).
  row.Labels<- df$Row.Label
  
  for(i in 1:length(listOfHdd)){
    if(!all(listOfHdd[[i]]$Row.Label %in% row.Labels) | !all(row.Labels %in% listOfHdd[[i]]$Row.Label) ){
      assign("errors", "Mismatched probe_ids - different platform used?", envir = .GlobalEnv)
    }
  }
  
  #merge data.frames
  expected.rowlen <- nrow(df)
  labels <- names(listOfHdd)
  df <- add.subset.label(df,labels[1])
  
  if(length(listOfHdd) > 1){
    for(i in 2:length(listOfHdd)){
      df2 <- listOfHdd[[i]]
      label <- labels[i]
      df2 <- add.subset.label(df2,label)
      df <- merge(df, df2 ,by = c("Row.Label","Bio.marker"), all = T)
      if(nrow(df) != expected.rowlen){
        assign("errors", "Mismatched probe_ids - different platform used?", envir = .GlobalEnv)
      }
    }
  }
  return(df)
}

add.subset.label <- function(df,label){
  sample.names <- c("")
  if(ncol(df) == 3 ){
    sample.names <- colnames(df)[3] # R returns NA instead of column name for colnames(df[,3:ncol(df)])
  }else{
  measurements <- df[,3:ncol(df)]
  sample.names <- colnames(measurements)
  }
  for(sample.name in sample.names){
    new.name <- paste(sample.name,label,sep="_")
    colnames(df)[colnames(df)==sample.name] <- new.name
  }
  return(df)
}

