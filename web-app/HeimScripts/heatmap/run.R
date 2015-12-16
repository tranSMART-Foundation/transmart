library(reshape2)
library(limma)

SUBSET1REGEX <- "_s1$"  # Regex identifying columns of subset 1.
markerTableJson <- "markerSelectionTable.json" # Name of the json file with limma outputs

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
  df          <- addStats(df, sorting, ranking, max_rows)
  df          <- df[1:min(max_rows, nrow(df)), ]  #  apply max_rows
  fields      <- buildFields(df)
  extraFields <- buildExtraFields(fields)
  probes      <- df[, 1]
  geneSymbols <- df[, 2]
  #unique(fields["GENESYMBOL"])[,1] #[,1] in order to get a vector,
  # otherwise we get a dataframe
  patientIDs  <- unique(fields["PATIENTID"])[,1]

  significanceValues <- unique(fields["SIGNIFICANCE"])[,1]
  features <- unique(extraFields["FEATURE"])[,1]
  jsn <- list(
    "fields"             = fields,
    "geneSymbols"        = geneSymbols,
    "patientIDs"         = patientIDs,
    "probes"             = probes,
    "significanceValues" = significanceValues,
    "features"           = features,
    "extraFields"        = extraFields
  )
  writeRunParams(max_rows, sorting)
  measurements <- cleanUp(df)  # temporary stats like SD and MEAN need
                               # to be removed for clustering to work
  measurements <- getMeasurements(measurements)
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

writeMarkerTable <- function(markerTable){
  colnames(markerTable) <- c("rowLabel", "biomarker",
                             "log2FoldChange", "t", "pValue", "adjustedPValue", "B")
  jsn                   <- toJSON(markerTable, pretty = TRUE)
  write(jsn, file = markerTableJson)
}

getMeasurements <- function(df) {
  if (ncol(df) > 3){
    return(df[, 3:ncol(df)])
  }
  else {
    return( df[3] )
  }
}


cleanUpLimmaOutput <- function() {
  if (file.exists(markerTableJson)) {
    file.remove(markerTableJson)
  }
}

hasTwoSubsets <- function(measurements) {
  getSubset1Length(measurements) < ncol(measurements)
}

applyRanking <- function (df, ranking, max_rows) {
  if( ranking %in% c("ttest", "logfold") ){
    df["SIGNIFICANCE_ABS"] <- abs(df["SIGNIFICANCE"])
    df <- df[with(df, order(-SIGNIFICANCE_ABS)), ]
    df["SIGNIFICANCE_ABS"] <- NULL
    df <- df[1:min(max_rows, nrow(df)), ]
    df <- df[with(df, order(-SIGNIFICANCE)), ]
    df["SIGNIFICANCE"] <- abs(df["SIGNIFICANCE"])
  } else {
    df <- df[with(df, order(-SIGNIFICANCE)), ]
  }
  df
}

addStats <- function(df, sorting, ranking, max_rows) {
  measurements  <- getMeasurements(df)
  rankingMethod <- getRankingMethod(ranking)
  twoSubsets    <- hasTwoSubsets(measurements)
  if (ncol(df) > 3) {
    #this is the case for more than 1 sample
    df <-
      applySorting(df,sorting)  # no need for sorting in one sample case, we do it here only
    if ( twoSubsets ){
      markerTable  <- getDEgenes(df)  # relies on columns being sorted,
    }                                 # with subset1 first, this is because of the way
                                      # design matrix is being constructed


    useLimma <- !is.function(rankingMethod)  # rankingMethod is either a function or a character "limma"
    if (useLimma  && !twoSubsets) {  # cannot use any of the limma methods for single subset.
      stop( paste("Illegal ranking method: ", ranking, " two subsets needed.") )
    }
    if (useLimma ) {
      if (!ranking %in% colnames(markerTable) ) {
          stop(paste("Illegal ranking method selected: ", ranking) )
      }
      rankingScore <- markerTable[ranking]

    } else {
      rankingScore <-
        apply(measurements, 1, rankingMethod, na.rm = TRUE )  # Calculating
    }                                          # ranking per probe (per row)
    means <- rowMeans(measurements, na.rm = T)  # this is just an
                                                # auxiliary column - it will not be
                                                # used for JSON.
    sdses <- apply(measurements,1 ,sd , na.rm = T)  # this is just
                                                    # an auxiliary column -
                                                    # it will not be used for JSON.
    df["MEAN"]         <- means
    df["SD"]           <- sdses
    df["SIGNIFICANCE"] <- rankingScore
    df                 <- applyRanking(df, ranking, max_rows)
    cleanUpLimmaOutput()  # In order to prevent displaying the table from previous run.
    if (twoSubsets) {
      markerTable["SIGNIFICANCE"] <- rankingScore
      markerTable                 <- applyRanking(markerTable, ranking, max_rows)
      markerTable["SIGNIFICANCE"] <- NULL
      writeMarkerTable(markerTable)
    }
  }
  else {
    #one sample
    variance <-
      1.0  # We cannot get any significance measure for just
           # one sample
    if ( nrow(measurements) > 1) {
        mean <-
          mean(df[,3], na.rm = T)  # For just one sample we take mean
                                   # over all genes (whole column), not per gene
        sds <- sd(df[,3],na.rm = T)  # For just one sample we take mean
                                     # over all genes (whole column), not per gene
    } else {
       mean <- 1
       sds  <- 1
    }

    df["MEAN"] <- rep(mean,nrow(df))
    df["SD"] <- rep(sds,nrow(df))
    df["SIGNIFICANCE"] <- rep(variance, nrow(df))
  }
  return(df)
}

# Coefficient of variation
coeffVar     <- function(x, na.rm = TRUE) ( sd(x, na.rm)/mean(x, na.rm) )

# Specific implementation of range.
normRange <- function(x, na.rm = TRUE) {
  # NAs are already removed but we keep this parameter so that apply does not have to be called differently
  # for normRange
  x <- removeOutliers(x)
  max(x) - min(x)
}

# We define outliers as measurements falling outside .25 or .75
# quantiles by more than 1.5 of interquantile range.
removeOutliers <- function(x) {
  qnt    <- quantile(x, probs=c(.25, .75), na.rm = TRUE)
  H      <- 1.5 * IQR(x, na.rm = TRUE)
  result <- x[!(x < (qnt[1] - H))]  # Below .25 by more than 1.5 IQR
  result <- result[!(result > (qnt[2] + H))]  # Above .75 by more than 1.5 IQR
  na.omit(result)
}

getRankingMethod <- function(rankingMethodName) {
  if (rankingMethodName == "variance") {
    return(var)
  } else if (rankingMethodName == "coef") {
    return(coeffVar)
  } else if (rankingMethodName == "range") {
    return(normRange)
  } else if (rankingMethodName == "mean") {
    return(mean)
  } else if (rankingMethodName == "median") {
    return(median)
  } else {
    return("Limma")
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
  measurements <- getMeasurements(df)
  colNames <- names(measurements)
  subsets <- getSubset(colNames)
  nodes <- getNode(colNames)
  subjects <- getSubject(colNames)
  if (sorting == "nodes") {
    colNames <- paste(subsets, nodes, subjects, sep = "")
  } else {
    colNames <- paste(subsets, subjects, nodes, sep = "")
  }
  inds <- sort(colNames, index.return = TRUE)$ix
  measurements <- measurements[, inds]
  cbind(df[, c(1,2)], measurements)
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

getDEgenes <- function(df) {
  measurements    <- getMeasurements(df)
  enoughSamples   <- ncol(measurements) > 3 && ncol(measurements) - getSubset1Length(measurements) > 1
  if (!enoughSamples){
    stop("Not enough samples to find differentially expressed genes.")
  }
  design          <- getDesign(measurements)
  contrast.matrix <- makeContrasts( S1-S2, levels = design )
  fit             <- lmFit(measurements, design)
  fit             <- contrasts.fit(fit, contrast.matrix)
  fit             <- eBayes(fit)
  contr           <- 1  #  We need a vector, not a df, so we'll do [, contr] on all stats
  top.fit         <- data.frame (
    logfold = fit$coefficients[, contr],
    ttest   = fit$t[, contr],
    pval    = fit$p.value[, contr],
    adjpval = p.adjust(
      p      = fit$p.value[, contr],
      method ='fdr'),
    bval    = fit$lods[, contr]
  )
  cbind(df[,1:2], top.fit)
}

getDesign <- function(measurements) {
  subset1Length <- getSubset1Length(measurements)
  classVectorS1 <- c(rep(1, subset1Length), rep(2, ncol(measurements) - subset1Length ))
  classVectorS2 <- rev(classVectorS1)
  cbind(S1=classVectorS1, S2=classVectorS2)
}

getSubset1Length <- function(measurements) {
  sum(grepl(pattern = SUBSET1REGEX, x = colnames(measurements))) # returns number of column names satisfying regexp.
}

### End of limma part
