library(jsonlite)
library(reshape2)


main <- function(max_rows=100){
  if(exists("preprocessed")){
    df <- preprocessed
  }
  else{
    df <- mergeFetchedData(loaded_variables)
  }
  # We take the last df to alleviate bug with new HDDs dropped being ignored - later on we will use label name explicitly.
  df["Row.Label"] <- lapply(df["Row.Label"],fixString) # remove illegal characters from probe names. This will prevent problems with CSS selectors on the frontend.
  if(ncol(df) > 3){
    #this is the case for more than 1 sample
    variances <- apply(df[,3:ncol(df)],1,var, na.rm = T) # Calculating variance per probe (per row)
    means <- rowMeans(df[,3:ncol(df)], na.rm = T) # this is just an auxiliary column - it will not be used for JSON.
    sdses <- apply(df[,3:ncol(df)],1,sd, na.rm = T) # this is just an auxiliary column - it will not be used for JSON.
    df["MEAN"] <- means
    df["SD"] <- sdses
    df["SIGNIFICANCE"] <- variances
    df <- df[with(df, order(-SIGNIFICANCE)), ]
  }
  else{
    #one sample
    variance <- 1.0 # We cannot get any significance measure for just one sample
    mean <- mean(df[,3], na.rm = T) #For just one sample we take mean over all genes (whole column), not per gene
    sds <- sd(df[,3],na.rm = T) #For just one sample we take mean over all genes (whole column), not per gene
    df["MEAN"] <- rep(mean,nrow(df))
    df["SD"] <- rep(sds,nrow(df))
    df["SIGNIFICANCE"] <- rep(variance, nrow(df))
  }

  df <- df[1:min(max_rows,nrow(df)),]
  fields <- buildFields(df)
  extraFields <- buildExtraFields(fields)
  geneSymbols <- df[,2]#unique(fields["GENESYMBOL"])[,1] #[,1] in order to get a vector, otherwise we get a dataframe
  patientIDs <-unique(fields["PATIENTID"])[,1]
  probes <- df[,1]#unique(fields["PROBE"])[,1]
  significanceValues <- unique(fields["SIGNIFICANCE"])[,1]
  features <- unique(extraFields["FEATURE"])[,1]
  jsn <- toJSON(list("fields"=fields, "geneSymbols"=geneSymbols,
                     "patientIDs"=patientIDs,
                     "probes"=probes,
                     "significanceValues"=significanceValues,
                     "features"=features,
                     "extraFields"=extraFields
                      ),
                pretty = TRUE)
  write(jsn,file = "heatmap.json") # json file be served the same way like any other file would - get name via /status call and then /download
  msgs <- c("Finished successfuly")
  if(exists("errors")){
    msgs <- errors
  }
  list(messages=msgs) # main function in every R script has to return a list (so a data.frame will also do)
}


buildFields <- function(df){
  df <- melt(df, na.rm = T, id=c("Row.Label","Bio.marker","SIGNIFICANCE","MEAN","SD")) # melt implicitly casts characters to factors to make your like more exciting, in order to encourage more adventures it does not have characters.as.factors=F param.
  ZSCORE <- (df$value - df$MEAN)/df$SD
  df["MEAN"] <- NULL
  df["SD"]   <-NULL
  names(df) <- c("PROBE","GENESYMBOL","SIGNIFICANCE","PATIENTID","VALUE")
  df["ZSCORE"] <- ZSCORE
  return(df)
}

fixString <- function(str) {
  str <- gsub("[^a-zA-Z0-9-]", "", str, perl=TRUE)
  return(str)
}

buildExtraFields <- function(df){
  FEATURE <- rep("Cohort", nrow(df) )
  PATIENTID <- as.character(df$PATIENTID)
  TYPE <- rep("binary",nrow(df))
  VALUE <- getSubset(PATIENTID)
  extraFields <- data.frame(FEATURE, PATIENTID, TYPE, VALUE)
}

getSubset <- function(patientIDs){
  splittedIds <- strsplit(patientIDs,"_s") # During merge, which is always run we append subset id, either _s1 or _s2 to PATIENTID.
  SUBSETS <- lapply(splittedIds, FUN=last_elem) # In proper patienid subset will always be  at the end. This select last element after _s
  SUBSETS <- sapply(SUBSETS, FUN=formatSubset)
}

#R has a function tail which is supposed to return last element of collection. But like everything in R it works in some unexpected way.
last_elem <- function(vect){
     vect[length(vect)]
}

#frontend expects 0 or 1 instead of 1 or 2 as subset number.
formatSubset <- function(subsetNumber){
  if(subsetNumber == "1"){
    return(0)
  }else if(subsetNumber == "2"){
    return(1)
  }
  else {
  stop(paste("Incorrect Assay ID: unexpected subset number: ",subsetNumber))
  }
}


### duplicated code from utils - when we get sourcing to work it will be moved


mergeFetchedData <- function(listOfHdd){
  df <- listOfHdd[[1]]
  expected.rowlen <- nrow(df)
  labels <- names(listOfHdd)
  df <- add.subset.label(df,labels[1])
  if(length(listOfHdd) > 1){
    for(i in 2:length(listOfHdd)){
      df2 <- listOfHdd[[i]]
      label <- labels[i]
      df2 <- add.subset.label(df2,label)
      df <- merge(df, df2 ,by=c("Row.Label","Bio.marker"))
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
