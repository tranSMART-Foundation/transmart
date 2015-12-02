


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
  sample.names <- colnames(df[,3:ncol(df)])
  for(sample.name in sample.names){
    new.name <- paste(sample.name,label,sep="_")
    colnames(df)[colnames(df)==sample.name] <- new.name
  }
  return(df)
}
