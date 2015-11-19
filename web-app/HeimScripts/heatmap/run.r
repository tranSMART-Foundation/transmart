library(jsonlite)
library(reshape2)


main <- function(max_rows){
  df <- loaded_variables[[1]] # SmartR does not support multiple HDD nodes yet
  if(ncol(df) > 3){
    variances <- apply(df[,3:ncol(df)],1,var) # Calculating variance per probe
    df["variance"] <- variances
    df <- df[with(df, order(-variance)), ]
    df["variance"] <- NULL # we do not need variance in the end result
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
  df <- melt(df,id=names(df)[1:2])
  SIGNIFICANCE <- rep(1.0, nrow(df))
  raw <- df[,ncol(df)]
  ZSCORE <- (raw - mean(raw))/ sd(raw)
  names(df) <- c("PROBE","GENESYMBOL","PATIENTID","VALUE")
  df["ZSCORE"] <- ZSCORE
  df["SIGNIFICANCE"] <- SIGNIFICANCE
  return(df)
}

