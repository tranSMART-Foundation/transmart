library(reshape2)
library(limma)

main <- function(max_rows = 100, sorting = "nodes", ranking = "coef") {
  max_rows <- as.numeric(max_rows)
  verifyInput(max_rows, sorting)
  df <- parseInput()
  df["Row.Label"] <-
    lapply(df["Row.Label"],fixString)  # remove illegal
                                       # characters from probe names. This will
                                       # prevent problems with CSS
                                       # selectors on the frontend.
  write.table(
      df,
      "heatmap_orig_values.tsv",
      sep = "\t",
      na = "",
      row.names = FALSE,
      col.names = TRUE
    )
  markerTable <- 
  df          <- addStats(df, sorting, ranking)
  df          <- df[1:min(max_rows,nrow(df)),]  #  apply max_rows
  fields      <- buildFields(df)
  extraFields <- buildExtraFields(fields)
  probes      <- df[,1]
  geneSymbols <- df[,2]
  #unique(fields["GENESYMBOL"])[,1] #[,1] in order to get a vector,
  # otherwise we get a dataframe
  patientIDs  <- unique(fields["PATIENTID"])[,1]
  
  significanceValues <- unique(fields["SIGNIFICANCE"])[,1]
  features <- unique(extraFields["FEATURE"])[,1]
  jsn <- list(
    "fields" = fields, "geneSymbols" = geneSymbols,
    "patientIDs" = patientIDs,
    "probes" = probes,
    "significanceValues" = significanceValues,
    "features" = features,
    "extraFields" = extraFields
  )
  writeRunParams(max_rows, sorting)
  measurements <- cleanUp(df)  # temporary stats like SD and MEAN need
                               # to be removed for clustering to work
  measurements <- measurements[,3:ncol(measurements)]
  measurements <- toZscores(measurements)
  if (nrow(measurements) > 1 &&
      ncol(measurements) > 1) {
    # cannot cluster
    # matrix which is less than 2x2
    jsn <- addClusteringOutput(jsn, measurements) #
  }
  jsn <- toJSON(jsn,
                pretty = TRUE)
  writeDataForZip(df, measurements, patientIDs)  # for later zip generation
  write(jsn,file = "heatmap.json")   # json file be served the same way
                                     # like any other file would - get name via
                                     # /status call and then /download
  
  msgs <- c("Finished successfuly")
  list(messages = msgs)
}

addStats <- function(df, sorting, ranking) {
  rankingMethod <- getRankingMethod(ranking)
  if (ncol(df) > 3) {
    #this is the case for more than 1 sample
    df <-
      applySorting(df,sorting)  # no need for sorting in one sample case
    rankingScore <-
      apply(df[,3:ncol(df)], 1, rankingMethod, na.rm = T)  # Calculating
                                                          # ranking per probe (per row)
    means <- rowMeans(df[,3:ncol(df)], na.rm = T)  # this is just an
                                                   # auxiliary column - it will not be
                                                   # used for JSON.
    sdses <- apply(df[,3:ncol(df)],1 ,sd , na.rm = T)  # this is just
                                                     # an auxiliary column -
                                                     # it will not be used for JSON.
    df["MEAN"]         <- means
    df["SD"]           <- sdses
    df["SIGNIFICANCE"] <- rankingScore
    df                 <- df[with(df, order(-SIGNIFICANCE)),]
  }
  else{
    #one sample
    variance <-
      1.0  # We cannot get any significance measure for just
           # one sample
    mean <-
      mean(df[,3], na.rm = T)  # For just one sample we take mean
                               # over all genes (whole column), not per gene
    sds <- sd(df[,3],na.rm = T)  # For just one sample we take mean
                                 # over all genes (whole column), not per gene
    df["MEAN"] <- rep(mean,nrow(df))
    df["SD"] <- rep(sds,nrow(df))
    df["SIGNIFICANCE"] <- rep(variance, nrow(df))
  }
  return(df)
}

# Coefficient of variation
coVar     <- function(x) ( sd(x)/mean(x) )

# Specific implementation of range.
normRange <- function(x) {
  x <- removeOutliers(x)
  max(x) - min(x)
}

# We define outliers as measurements falling outside .25 or .75
# quantiles by more than 1.5 of interquantile range.
removeOutliers <- function(x) {
  qnt <- quantile(x, probs=c(.25, .75), na.rm = TRUE)
  H <- 1.5 * IQR(x, na.rm = TRUE)
  result <- x[!(x < (qnt[1] - H))]  # Below .25 by more than 1.5 IQR
  result[!(result > (qnt[2] + H))]  # Above .75 by more than 1.5 IQR
}

bval <- function(x) {

}

getRankingMethod <- function(rankingMethodName){
  if (rankingMethodName == "variance") {
       return(var)
  } else if (rankingMethodName == "coef") {
       return(coVar)
  } else if (rankingMethodName == "range") {
       return(normRange)
  } else if (rankingMethodName == "mean") {
       return(mean)
  } else if (rankingMethodName == "median") {
       return(median)
  } else if (rankingMethodName == "bval") {
       return(bval)
  } else if (rankingMethodName == "pval") {
       return(pval)
  } else if (rankingMethodName == "adj_pval") {
       return(adj_pval)
  } else if (rankingMethodName == "logfold") {
       return(logfold)
  } else if (rankingMethodName == "t-test") {
       return(t_test)
  }
  else {
    stop(paste("Unsupported ranking method: ", rankingMethodName))
  }
}

parseInput <- function() {
  if (exists("preprocessed")) {
    df <- preprocessed
  }
  else {
    df <- mergeFetchedData(loaded_variables)
  }
  return(df)
}

toZscores <- function(measurements) {
  measurements <- scale(t(measurements))
  t(measurements)
}

cleanUp <- function(df) {
  df["MEAN"] <- NULL
  df["SD"]  <- NULL
  df["SIGNIFICANCE"] <- NULL
  df
}

verifyInput <- function(max_rows, sorting) {
  if (max_rows <= 0) {
    stop("Max rows argument needs to be higher than zero.")
  }
  if (!(sorting == "nodes" || sorting == "subjects")) {
    stop("Unsupported sorting type. Only nodes and subjects allowed")
  }
}

applySorting <- function(df,sorting) {
  measurements <- df[,3:ncol(df)]
  colNames <- names(measurements)
  
  subsets <- getSubset(colNames)
  nodes <- getNode(colNames)
  subjects <- getSubject(colNames)
  if (sorting == "nodes") {
    colNames <- paste(subsets, nodes, subjects, sep = "")
  }else{
    colNames <- paste(subsets, subjects, nodes, sep = "")
  }
  inds <- sort(colNames, index.return = TRUE)$ix
  
  measurements <- measurements[,inds]
  cbind(df[,c(1,2)], measurements)
}

getNode <- function(patientIDs) {
  splittedIds <-
    strsplit(patientIDs,"_") # During merge, which is always
                             # run we append subset id, either
                             # _s1 or _s2 to PATIENTID.
  sapply(splittedIds, FUN = tail_elem,n = 2) # In proper patienid subset will
                                             # always be  at the end.
                                             # This select last but one elemnt
                                             # - the node
}

getSubject <- function(patientIDs) {
  splittedIds <- strsplit(patientIDs,"_")
  sapply(splittedIds, FUN = discardNodeAndSubject)
}

discardNodeAndSubject <- function(label) {
  label <- strsplit(label,"_")
  endOfSubject <-
    length(label) - 2  #last too elements are node and subset.
  label <- label[1:endOfSubject]
  paste(label, collapse = "_")
}

buildFields <- function(df) {
  df <- melt(
    df, na.rm = T, id = c("Row.Label",
                          "Bio.marker",
                          "SIGNIFICANCE",
                          "MEAN",
                          "SD")
  )  # melt implicitly casts
     # characters to factors to make your like more exciting,
     # in order to encourage more adventures it does not
     # have characters.as.factors=F param.
  df$variable <- as.character(df$variable)
  ZSCORE      <- (df$value - df$MEAN) / df$SD
  df["MEAN"]  <- NULL
  df["SD"]    <- NULL
  names(df)   <-
    c("PROBE","GENESYMBOL","SIGNIFICANCE","PATIENTID","VALUE")
  df["ZSCORE"] <- ZSCORE
  return(df)
}

writeDataForZip <- function(df, zScores, pidCols) {
  df      <- df[ , -which(names(df) %in% pidCols)]  # Drop patient columns
  df      <- cbind(df,zScores)                      # Replace with zScores
  write.table(
    df,
    "heatmap_data.tsv",
    sep = "\t",
    na = "",
    row.names = FALSE,
    col.names = TRUE
  )
}

writeRunParams <- function(max_rows, sorting) {
  params <- list(max_rows = max_rows,
                 sorting  = sorting)
  if (exists("preprocessed") && exists("preprocessing_params")) {
    params <- c(params, preprocessing_params)
  }
  write(toJSON(params, pretty = TRUE), 'params.json')
}

fixString <- function(str) {
  gsub("[^a-zA-Z0-9-]", "", str, perl = TRUE)
}

buildExtraFields <- function(df) {
  FEATURE <- rep("Cohort", nrow(df))
  PATIENTID <- as.character(df$PATIENTID)
  TYPE <- rep("binary",nrow(df))
  VALUE <- getSubset(PATIENTID)
  extraFields <- data.frame(FEATURE, PATIENTID, TYPE, VALUE,
   stringsAsFactors = FALSE)
}

getSubset <- function(patientIDs) {
  splittedIds <- strsplit(patientIDs,"_s") # During merge,
  # which is always run we append subset id, either
  # _s1 or _s2 to PATIENTID.
  SUBSETS <- lapply(splittedIds, FUN = tail_elem) # In proper patienid
  # subset will always be  at the end.
  # This select last element after _s
  SUBSETS <- sapply(SUBSETS, FUN = formatSubset)
}

tail_elem <- function(vect, n = 1) {
  vect[length(vect) - n + 1]
}

#frontend expects 0 or 1 instead of 1 or 2 as subset number.
formatSubset <- function(subsetNumber) {
  if (subsetNumber == "1") {
    return(0)
  }else if (subsetNumber == "2") {
    return(1)
  }
  else {
    stop(paste(
      "Incorrect Assay ID: unexpected subset number: ",subsetNumber
    ))
  }
}


### duplicated code from utils - when we get sourcing to work it will be moved


mergeFetchedData <- function(listOfHdd) {
  df <- listOfHdd[[1]]
  expected.rowlen <- nrow(df)
  labels <- names(listOfHdd)
  df <- add.subset.label(df,labels[1])
  if (length(listOfHdd) > 1) {
    for (i in 2:length(listOfHdd)) {
      df2 <- listOfHdd[[i]]
      label <- labels[i]
      df2 <- add.subset.label(df2,label)
      df <- merge(df, df2 ,by = c("Row.Label","Bio.marker"))
      if (nrow(df) != expected.rowlen) {
        stop("Mismatched probe_ids - different platform used?")
      }
    }
  }
  return(df)
}

add.subset.label <- function(df,label) {
  sample.names <- c("")
  if (ncol(df) == 3) {
    sample.names <-
      colnames(df)[3] # R returns NA instead of column name
                      # for colnames(df[,3:ncol(df)])
  }else{
    measurements <- df[,3:ncol(df)]
    sample.names <- colnames(measurements)
  }
  for (sample.name in sample.names) {
    new.name <- paste(sample.name,label,sep = "_")
    colnames(df)[colnames(df) == sample.name] <- new.name
  }
  return(df)
}

### end of duplicated code

computeDendrogram <-
  function(distances, linkageMethod) {
    as.dendrogram(hclust(distances, method = linkageMethod))
  }

dendrogramToJSON <- function(d) {
  totalMembers <- attributes(d)$members
  add_json <- function(x, start) {
    members <- attributes(x)$members
    height <- attributes(x)$height
    index <- (start - 1):(start + members - 2)
    index <- paste(index, collapse = ' ')
    jsonString <<- paste(
      jsonString,
      sprintf(
        '{"height":"%s", "index":"%s", "children":[',
        height,
        index
      )
    )
    if (is.leaf(x)) {
      jsonString <<- paste(jsonString, ']}')
    } else {
      add_json(x[[1]], start)
      jsonString <<- paste(jsonString, ",")
      leftMembers <- attributes(x[[1]])$members
      add_json(x[[2]], start + leftMembers)
      jsonString <<- paste(jsonString, "]}")
    }
  }
  jsonString <- ""
  add_json(d, 1)
  return(jsonString)
}


addClusteringOutput <- function(jsn, measurements) {
  euclideanDistancesRow <- dist(measurements, method = "euclidean")
  manhattanDistancesRow <- dist(measurements, method = "manhattan")
  euclideanDistancesCol <- dist(t(measurements), method = "euclidean")
  manhattanDistancesCol <- dist(t(measurements), method = "manhattan")
  
  colDendrogramEuclideanComplete <- computeDendrogram( euclideanDistancesCol, 'complete')
  colDendrogramEuclideanSingle <- computeDendrogram( euclideanDistancesCol, 'single')
  colDendrogramEuclideanAverage <- computeDendrogram( euclideanDistancesCol, 'average')
  rowDendrogramEuclideanComplete <- computeDendrogram( euclideanDistancesRow, 'complete')
  rowDendrogramEuclideanSingle <- computeDendrogram( euclideanDistancesRow, 'single')
  rowDendrogramEuclideanAverage <- computeDendrogram( euclideanDistancesRow, 'average')
  
  colDendrogramManhattanComplete <- computeDendrogram( manhattanDistancesCol, 'complete')
  colDendrogramManhattanSingle <- computeDendrogram( manhattanDistancesCol, 'single')
  colDendrogramManhattanAverage <- computeDendrogram( manhattanDistancesCol, 'average')
  rowDendrogramManhattanComplete <- computeDendrogram( manhattanDistancesRow, 'complete')
  rowDendrogramManhattanSingle <- computeDendrogram( manhattanDistancesRow, 'single')
  rowDendrogramManhattanAverage <- computeDendrogram( manhattanDistancesRow, 'average')
  
  jsn$hclustEuclideanComplete <- list(
    order.dendrogram(colDendrogramEuclideanComplete) - 1,
    order.dendrogram(rowDendrogramEuclideanComplete) - 1,
    dendrogramToJSON(colDendrogramEuclideanComplete),
    dendrogramToJSON(rowDendrogramEuclideanComplete)
  )
  
  jsn$hclustEuclideanSingle <- list(
    order.dendrogram(colDendrogramEuclideanSingle)  - 1,
    order.dendrogram(rowDendrogramEuclideanSingle) - 1,
    dendrogramToJSON(colDendrogramEuclideanSingle),
    dendrogramToJSON(rowDendrogramEuclideanSingle)
  )
  
  jsn$hclustEuclideanAverage <- list(
    order.dendrogram(colDendrogramEuclideanAverage) - 1,
    order.dendrogram(rowDendrogramEuclideanAverage) - 1,
    dendrogramToJSON(colDendrogramEuclideanAverage),
    dendrogramToJSON(rowDendrogramEuclideanAverage)
  )
  
  jsn$hclustManhattanComplete <- list(
    order.dendrogram(colDendrogramManhattanComplete) - 1,
    order.dendrogram(rowDendrogramManhattanComplete) - 1,
    dendrogramToJSON(colDendrogramManhattanComplete),
    dendrogramToJSON(rowDendrogramManhattanComplete)
  )
  
  jsn$hclustManhattanSingle <- list(
    order.dendrogram(colDendrogramManhattanSingle) - 1,
    order.dendrogram(rowDendrogramManhattanSingle) - 1,
    dendrogramToJSON(colDendrogramManhattanSingle),
    dendrogramToJSON(rowDendrogramManhattanSingle)
  )
  
  jsn$hclustManhattanAverage <- list(
    order.dendrogram(colDendrogramManhattanAverage) - 1,
    order.dendrogram(rowDendrogramManhattanAverage) - 1,
    dendrogramToJSON(colDendrogramManhattanAverage),
    dendrogramToJSON(rowDendrogramManhattanAverage)
  )
  return(jsn)
}

### Limma part. It will be moved into a separate
# module when we get R scripts sourcing to work here.

getDEgenes <- function(subset1, subset2){

}

classVectorS1 <- c(rep(1, ncol(valueMatrix.cohort1[, -1])), rep(2, ncol(valueMatrix[, -1]) - ncol(valueMatrix.cohort1[, -1])))
    classVectorS2 <- rev(classVectorS1)
    design <- cbind(S1=classVectorS1, S2=classVectorS2)
    contrast.matrix = makeContrasts(S1-S2, levels=design)
    fit <- lmFit(log2Matrix[, -1], design)
    fit <- contrasts.fit(fit, contrast.matrix)
    fit <- eBayes(fit)
    contr = 1
    top.fit = data.frame(
            logFC=fit$coefficients[, contr],
            t=fit$t[, contr],
            P.Value=fit$p.value[, contr],
            adj.P.val=p.adjust(p=fit$p.value[, contr], method='fdr'),
            B=fit$lods[, contr]
    )
    significanceValues <- top.fit[[significanceMeassure]]
