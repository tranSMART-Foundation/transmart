library(jsonlite)
library(reshape2)


main <- function(max_rows=50){
  df <- loaded_variables[[1]] # SmartR does not support multiple HDD nodes yet
  if(ncol(df) > 3){
    variances <- apply(df[,3:ncol(df)],1,var) # Calculating variance per probe
    df["SIGNIFICANCE"] <- variances
    df["MEAN"] <- rowMeans(df[,3:ncol(df)], na.rm = T)
    df["SD"] <- apply(df[,3:ncol(df)],1,sd, na.rm = T)
    df <- df[with(df, order(-SIGNIFICANCE)), ]
  }
  else{
    df["SIGNIFICANCE"] <- rep(1.0, nrow(df))
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

