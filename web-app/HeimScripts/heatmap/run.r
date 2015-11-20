library(jsonlite)
library(reshape2)




main <- function(max_rows=100){
  df <- loaded_variables[[length(loaded_variables)]] # SmartR does not support multiple HDD nodes yet
  # We take the last df to alleviate bug with new HDDs dropped being ignored - later on we will use label name explicitly.
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
    mean <- mean(ex_df[,3], na.rm = T) #For just one sample we take mean over all genes (whole column), not per gene
    sds <- sd(df[,3],na.rm = T) #For just one sample we take mean over all genes (whole column), not per gene
    df["MEAN"] <- rep(mean,nrow(df))
    df["SD"] <- rep(sds,nrow(df))
    df["SIGNIFICANCE"] <- rep(variance, nrow(df))
  }

  df <- df[1:max_rows,]
  fields <- buildFields(df)
  geneSymbols <- unique(fields["GENESYMBOL"])[,1] #[,1] in order to get a vector, otherwise we get a dataframe
  patientIDs <-unique(fields["PATIENTID"])[,1]
  probes <- unique(fields["PROBE"])[,1]
  significanceValues <- unique(fields["SIGNIFICANCE"])[,1]
  jsn <- toJSON(list("fields"=fields, "geneSymbols"=geneSymbols,
                     "patientIDs"=patientIDs,
                     "probes"=probes,
                     "significanceValues"=significanceValues ),
                pretty = TRUE)
  write(jsn,file = "heatmap.json") # json file be served the same way like any other file would - get name via /status call and then /download
  list(filename="heatmap.json") # main function in every R script has to return a list (so a data.frame will also do)
}


buildFields <- function(df){
  df <- melt(df,id=c("Row.Label","Bio.marker","SIGNIFICANCE","MEAN","SD"))
  ZSCORE <- (df$value - df$MEAN)/df$SD
  df["MEAN"] <- NULL
  df["SD"]   <-NULL
  names(df) <- c("PROBE","GENESYMBOL","SIGNIFICANCE","PATIENTID","VALUE")
  df["ZSCORE"] <- ZSCORE
  return(df)
}

