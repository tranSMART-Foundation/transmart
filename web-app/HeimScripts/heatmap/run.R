library(reshape2)


main <- function(max_rows=100, sorting="nodes"){
  max_rows <- as.numeric(max_rows)
  verifyInput(max_rows, sorting)
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
    df <- applySorting(df,sorting) # no need for sorting in one sample case
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
  df <- cleanUp(df) # temporary stats like SD and MEAN need to be removed for clustering to work
  extraFields <- buildExtraFields(fields)
  probes <- na.omit(df[,1])
  geneSymbols <- df[,2][1:length(probes)]#unique(fields["GENESYMBOL"])[,1] #[,1] in order to get a vector, otherwise we get a dataframe
  patientIDs <-unique(fields["PATIENTID"])[,1]

  significanceValues <- unique(fields["SIGNIFICANCE"])[,1]
  features <- unique(extraFields["FEATURE"])[,1]
  jsn <- list("fields"=fields, "geneSymbols"=geneSymbols,
                              "patientIDs"=patientIDs,
                              "probes"=probes,
                              "significanceValues"=significanceValues,
                              "features"=features,
                              "extraFields"=extraFields
                               )
  measurements <- df[,3:ncol(df)]
  jsn <- addClusteringOutput(jsn, measurements)
  jsn <- toJSON(jsn,
                pretty = TRUE)
  write(jsn,file = "heatmap.json") # json file be served the same way like any other file would - get name via /status call and then /download
  writeDataForZip(df, patientIDs) # for later zip generation
  writeRunParams(max_rows, sorting)
  msgs <- c("Finished successfuly")
  if(exists("errors")){
    msgs <- errors
  }
  list(messages=msgs) # main function in every R script has to return a list (so a data.frame will also do)
}

cleanUp <- function(df){
  df["MEAN"] <- NULL
  df["SD" ]  <- NULL
  df["SIGNIFICANCE"] <- NULL
  df
}

verifyInput <- function(max_rows, sorting){
  if(max_rows <= 0 ){
    stop("Max rows argument needs to be higher than zero.")
  }
  if(!(sorting == "nodes" || sorting == "subjects")){
    stop("Unsupported sorting type. Only nodes and subjects allowed")
  }
}

applySorting <- function(df,sorting){
  measurements <- df[,3:ncol(df)]
  colNames <- names(measurements)

  subsets <- getSubset(colNames)
  nodes <- getNode(colNames)
  subjects <- getSubject(colNames)
  if(sorting == "nodes"){
    colNames <- paste(subsets, nodes, subjects, sep="")
  }else{
    colNames <- paste(subsets, subjects, nodes, sep="")
  }
  inds <- sort(colNames, index.return=TRUE )$ix

  measurements <- measurements[,inds]
  cbind(df[,c(1,2)], measurements)
}

getNode <- function(patientIDs){
  splittedIds <- strsplit(patientIDs,"_") # During merge, which is always run we append subset id, either _s1 or _s2 to PATIENTID.
  sapply(splittedIds, FUN=tail_elem,n= 2) # In proper patienid subset will always be  at the end. This select last but one elemnt - the node
}

getSubject <- function(patientIDs){
  splittedIds <- strsplit(patientIDs,"_")
  sapply(splittedIds, FUN=tail_elem,n= 3)
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

writeDataForZip <- function(df, patientIDs) {
  pidCols    <- as.character(patientIDs)
  t          <- df
  t[pidCols] <- lapply(t[pidCols], function(v) { (v - df$MEAN) / df$SD })
  allCols    <- c(c("Row.Label", "Bio.marker", "MEAN", "SD", "SIGNIFICANCE"), pidCols)
  write.table(t[allCols], "heatmap_data.tsv", sep="\t", na="", row.names=F, col.names=T)

  allCols <- c(c("Row.Label"), pidCols)
  write.table(df[allCols], "heatmap_orig_values.tsv", sep="\t", na="", row.names=F, col.names=T)
}

writeRunParams <- function(max_rows, sorting) {
  params <- list(
    max_rows = max_rows,
    sorting  = sorting
  )

  if (exists("preprocessed") && exists("preprocessing_params")) {
    params <- c(params, preprocessing_params)
  }

  write(toJSON(params, pretty=TRUE), 'params.json')
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
  SUBSETS <- lapply(splittedIds, FUN=tail_elem) # In proper patienid subset will always be  at the end. This select last element after _s
  SUBSETS <- sapply(SUBSETS, FUN=formatSubset)
}

tail_elem <- function(vect, n = 1){
     vect[length(vect) -n + 1 ]
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

computeDendrogram <- function(zScoreMatrix, distanceMeassure, linkageMethod) {
    as.dendrogram(hclust(dist(zScoreMatrix, method=distanceMeassure), method=linkageMethod))
}

dendrogramToJSON <- function(d) {
    totalMembers <- attributes(d)$members
    add_json <- function(x, start, left) {
        members <- attributes(x)$members
        height <- attributes(x)$height
        index <- (start - 1):(start + members - 2)
        index <- paste(index, collapse=' ')
        jsonString <<- paste(jsonString, sprintf('{"height":"%s", "index":"%s", "children":[', height, index))
        if (is.leaf(x)){
            jsonString <<- paste(jsonString, ']}')
        } else {
            add_json(x[[1]], start, TRUE)
            jsonString <<- paste(jsonString, ",")
            leftMembers <- attributes(x[[1]])$members
            add_json(x[[2]], start + leftMembers, FALSE)
            jsonString <<- paste(jsonString, "]}")
        }
    }
    jsonString <- ""
    add_json(d, TRUE)
    return(jsonString)
}


addClusteringOutput <- function(jsn, measurements){

  colDendrogramEuclideanComplete <- computeDendrogram(t(measurements), 'euclidean', 'complete')
  colDendrogramEuclideanSingle <- computeDendrogram(t(measurements), 'euclidean', 'single')
  colDendrogramEuclideanAverage <- computeDendrogram(t(measurements), 'euclidean', 'average')
  rowDendrogramEuclideanComplete <- computeDendrogram(measurements, 'euclidean', 'complete')
  rowDendrogramEuclideanSingle <- computeDendrogram(measurements, 'euclidean', 'single')
  rowDendrogramEuclideanAverage <- computeDendrogram(measurements, 'euclidean', 'average')

  colDendrogramManhattanComplete <- computeDendrogram(t(measurements), 'manhattan', 'complete')
  colDendrogramManhattanSingle <- computeDendrogram(t(measurements), 'manhattan', 'single')
  colDendrogramManhattanAverage <- computeDendrogram(t(measurements), 'manhattan', 'average')
  rowDendrogramManhattanComplete <- computeDendrogram(measurements, 'manhattan', 'complete')
  rowDendrogramManhattanSingle <- computeDendrogram(measurements, 'manhattan', 'single')
  rowDendrogramManhattanAverage <- computeDendrogram(measurements, 'manhattan', 'average')

  jsn$hclustEuclideanComplete <- list(
      order.dendrogram(colDendrogramEuclideanComplete) -1,
      order.dendrogram(rowDendrogramEuclideanComplete) -1,
      dendrogramToJSON(colDendrogramEuclideanComplete),
      dendrogramToJSON(rowDendrogramEuclideanComplete))

  jsn$hclustEuclideanSingle <- list(
      order.dendrogram(colDendrogramEuclideanSingle)  -1,
      order.dendrogram(rowDendrogramEuclideanSingle) - 1,
      dendrogramToJSON(colDendrogramEuclideanSingle),
      dendrogramToJSON(rowDendrogramEuclideanSingle))

  jsn$hclustEuclideanAverage <- list(
      order.dendrogram(colDendrogramEuclideanAverage) - 1,
      order.dendrogram(rowDendrogramEuclideanAverage) - 1,
      dendrogramToJSON(colDendrogramEuclideanAverage),
      dendrogramToJSON(rowDendrogramEuclideanAverage))

  jsn$hclustManhattanComplete <- list(
      order.dendrogram(colDendrogramManhattanComplete) - 1,
      order.dendrogram(rowDendrogramManhattanComplete) - 1,
      dendrogramToJSON(colDendrogramManhattanComplete),
      dendrogramToJSON(rowDendrogramManhattanComplete))

  jsn$hclustManhattanSingle <- list(
      order.dendrogram(colDendrogramManhattanSingle) - 1,
      order.dendrogram(rowDendrogramManhattanSingle) - 1,
      dendrogramToJSON(colDendrogramManhattanSingle),
      dendrogramToJSON(rowDendrogramManhattanSingle))

  jsn$hclustManhattanAverage <- list(
      order.dendrogram(colDendrogramManhattanAverage) - 1,
      order.dendrogram(rowDendrogramManhattanAverage) - 1,
      dendrogramToJSON(colDendrogramManhattanAverage),
      dendrogramToJSON(rowDendrogramManhattanAverage))
     return(jsn)
}
