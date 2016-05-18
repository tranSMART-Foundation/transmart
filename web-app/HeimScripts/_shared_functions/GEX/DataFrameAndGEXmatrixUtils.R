##########################################################################
## This file contains more generic functionalities when working with    ##
## "df-type"-data frame containing Microarray GEX data  as well as      ##
## GEX matrix variables containing measurements and sample ids as       ##
## colnames.                                                            ##
## "df-type": a data frame with GEX matrix data as well as Limmy diff   ##
## expr statistics or other statistics used to order and filter the df  ##
## GEX data frame (variance, coefficient of variation, ... )            ##
##########################################################################



## Ranking/ordering the data frame according to
## selected statistics and retaining
## the accordingly highest ranked probes
applyRanking <- function (df, ranking, max_rows) {
  nrows = min(max_rows, nrow(df))

  if (ranking %in% c("ttest", "logfold")) {
    df["SIGNIFICANCE_ABS"] <- abs(df["SIGNIFICANCE"])
    df <- df[with(df, order(-SIGNIFICANCE_ABS)), ]
    df["SIGNIFICANCE_ABS"] <- NULL
    df <- df[1:nrows, ]
    df <- df[with(df, order(SIGNIFICANCE, decreasing=TRUE)), ]
  } else if(ranking %in% c("pval", "adjpval")) {
    df <- df[with(df, order(SIGNIFICANCE)), ]
    df <- df[1:nrows, ]
  } else {
    df <- df[with(df, order(-SIGNIFICANCE)), ]
    df <- df[1:nrows, ]
  }

  df
}



applySorting <- function(df,sorting) {
  measurements <- getMeasurements(df)
  colNames <- names(measurements)
  subsets <- getSubset(colNames)
  nodes <- getNode(colNames)
  subjects <- getSubject(colNames)

  timelineValues <- getTimelineValues(nodes, fetch_params$ontologyTerms)


  if (sorting == "nodes") {
    inds <- order(subsets, timelineValues, nodes, subjects)
  } else {
    inds <- order(subsets, subjects, timelineValues, nodes)
  }

  measurements <- measurements[, inds]

  cbind(df[, c(1,2)], measurements)
}



## Generates a filtered data frame containing the GEX matrix data
## as well as the statistics used to order and filter the df GEX data frame.
## Ordering and filtering can be performed based on Limma-based
## statistics (B value, P-value, Adjusted P-value) but also other statistical
## functions like variance, mean, median
addStats <- function(df, sorting, ranking, max_rows) {
  measurements  <- getMeasurements(df)
  rankingMethod <- getRankingMethod(ranking)
  twoSubsets    <- hasTwoSubsets(measurements)

  logfold.values <- data.frame(LOGFOLD=numeric())
  ttest.values <- data.frame(TTEST=numeric())
  pval.values <- data.frame(PVAL=numeric())
  adjpval.values <- data.frame(ADJPVAL=numeric())
  bval.values <- data.frame(BVAL=numeric())
  rankingScore <- data.frame(SIGNIFICANCE=numeric())

  useLimma <- !is.function(rankingMethod)  # rankingMethod is either a function or a character "limma"
  # is it a valid GEX matrix for differential expression analysis containing enough not NA values?
  validLimmaMeasurements <- isValidLimmaMeasurements(measurements)

  if (ncol(df) > 3) {

    #this is the case for more than 1 sample
    df <- applySorting(df,sorting)  # no need for sorting in one sample case, we do it here only


    validLimmaMeasurements <- isValidLimmaMeasurements(measurements)


    if (twoSubsets && validLimmaMeasurements) {
      markerTable  <- getDEgenes(df)  # relies on columns being sorted,
    }                                 # with subset1 first, this is because of the way
    # design matrix is being constructed

    if (useLimma  && !twoSubsets) {  # cannot use any of the limma methods for single subset.
      stop( paste("Illegal ranking method: ", ranking, " two subsets needed.") )
    }


    if (useLimma && validLimmaMeasurements) {

      if (!ranking %in% colnames(markerTable) )
        stop(paste("Illegal ranking method selected: ", ranking) )

      logfold.values <- markerTable["logfold"]
      ttest.values <- markerTable["ttest"]
      pval.values <- markerTable["pval"]
      adjpval.values <- markerTable["adjpval"]
      bval.values <- markerTable["bval"]

      # Obtain the rankingScore based on selected diff expr statistics
      rankingScore <- markerTable[ranking]
    } else if (useLimma && !validLimmaMeasurements)  {
      # when differential expression rank criteria
      # is selected while it's not valid limma measurements,
      # provide empty data frame to the significance columns.
    } else {

      rankingScore <- apply(measurements, 1, rankingMethod, na.rm = TRUE )  # Calculating ranking per probe (per row)
    }

    means <- rowMeans(measurements, na.rm = T)  # this is just an
    # auxiliary column - it will not be
    # used for JSON.
    sdses <- apply(measurements,1 ,sd , na.rm = T)  # this is just
    # an auxiliary column -
    # it will not be used for JSON.

    cleanUpLimmaOutput(markerTableJson = markerTableJson)  # In order to prevent displaying the table from previous run.

    if (twoSubsets && validLimmaMeasurements) {
      markerTable["SIGNIFICANCE"] <- rankingScore
      markerTable                 <- applyRanking(markerTable, ranking, max_rows)
      markerTable["SIGNIFICANCE"] <- NULL
      writeMarkerTable(markerTable, markerTableJson = markerTableJson)
    }
  }

  df["MEAN"]         <- means
  df["SD"]           <- sdses
  df["SIGNIFICANCE"] <- rankingScore
  df["LOGFOLD"] <- logfold.values
  df["TTEST"] <- ttest.values
  df["PVAL"] <- pval.values
  df["ADJPVAL"] <- adjpval.values
  df["BVAL"] <- bval.values

  if (useLimma & !validLimmaMeasurements) {
    # dont apply ranking
  } else {
    # else apply
    df <- applyRanking(df, ranking, max_rows)
  }

  return(df)
}



## Returns data frame containing
## the intensity/expression measures as matrix
## and the header which corresponds to the sample
## names
getMeasurements <- function(df) {
  if (ncol(df) > 3){
    return(df[, 3:ncol(df)])
  } else {
    return( df[3] )
  }
}



## This function removes rows with duplicate probe id (). Only the data row with first
## occurence of the probe id is kept. Probe id and gene symbol get merged to uid variable.
mergeDuplicates <- function(df) {

  ## Which probes have multiple entries in df GEX matrix?
  ## This situation can happen when non unique probe ids get
  ## used like for example gene symbols
  dupl.where <- duplicated(df$Row.Label)
  dupl.rows <- df[dupl.where, ]
  df <- df[! dupl.where, ]

  uids <- paste(df$Row.Label, df$Bio.marker, sep="--")
  uids[df$Row.Label == dupl.rows$Row.Label] <- paste(uids[df$Row.Label == dupl.rows$Row.Label], dupl.rows$Bio.marker[df$Row.Label == dupl.rows$Row.Label], sep="--")
  df <- cbind(UID=uids, df[, -c(1,2)])

  df
}


# nodeID has usually this format: 'X123_highDimensional_n0_s1)
# this method pretifies it with the actual node label like this: '123_BreastCancer'
idToNodeLabel <- function(ids, ontologyTerms) {
  # extract patientID (123)
  patientIDs <- sub("_.+_n[0-9]+_s[0-9]+", "", ids, perl=TRUE) # remove the _highDimensional_n0_s1
  patientIDs <- sub("^X", "", patientIDs, perl=TRUE) # remove the X
  # extract subset (s1)
  subsets <- substring(ids, first=nchar(ids)-1, last=nchar(ids))
  # extract node label (Breast)
  nodes <- sub(".+?_", "", ids, perl=TRUE) # remove the X123_
  nodes <- as.vector(substring(nodes, first=1, last=nchar(nodes)-3))
  nodeLabels <- as.vector(sapply(nodes, function(node) return(ontologyTerms[[node]]$name)))
  # put everything together (123, Breast, s1)
  paste(patientIDs, nodeLabels, subsets, sep="_")
}


## Checking if a variable called preprocessed exists in R
## workspace, else loaded_variables is used to create data frame df.
## Column names in data frame get modified by replacing matrix id
## (e.g.: n0, n1, ...) by corresponding name in fetch_params list var
parseInput <- function() {
  
  ## Retrieving the input data frame
  if (exists("preprocessed")) {
    df <- preprocessed
  } else {
    df <- mergeFetchedData(loaded_variables)
  }
  
  ## Renaming the column names in the data frame:
  ## - removing "X" as prefix
  ## - replacing node id by node name
  ## (e.g. 'X144_n0_s1' -> '144_Breast_s2')
  colnames(df)[c(-1,-2)] = idToNodeLabel(colnames(df)[c(-1,-2)], fetch_params$ontologyTerms)
  
  return(df)
}



mergeFetchedData <- function(listOfHdd){
  df <- listOfHdd[[1]]

  #test if the different data.frames all contain the exact same set of probe IDs/metabolites/etc, independent of order.
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



add.subset.label <- function(df,label) {
  sample.names <- c("")
  if (ncol(df) == 3) {
    sample.names <-
      colnames(df)[3] # R returns NA instead of column name
    # for colnames(df[,3:ncol(df)])
  }else{
    measurements <- getMeasurements(df)
    sample.names <- colnames(measurements)
  }
  for (sample.name in sample.names) {
    new.name <- paste(sample.name,label,sep = "_")
    colnames(df)[colnames(df) == sample.name] <- new.name
  }
  return(df)
}



## Removing existing statistic data from
## from df object
cleanUp <- function(df) {
  df["MEAN"] <- NULL
  df["SD"] <- NULL
  df["SIGNIFICANCE"] <- NULL
  df["LOGFOLD"] <- NULL
  df["TTEST"] <- NULL
  df["PVAL"] <- NULL
  df["ADJPVAL"] <- NULL
  df["BVAL"] <- NULL
  df
}



## Ranking/ordering the data frame according to
## selected statistics and retaining
## the accordingly highest ranked probes
applyRanking <- function (df, ranking, max_rows) {
  nrows = min(max_rows, nrow(df))

  if (ranking %in% c("ttest", "logfold")) {
    df["SIGNIFICANCE_ABS"] <- abs(df["SIGNIFICANCE"])
    df <- df[with(df, order(-SIGNIFICANCE_ABS)), ]
    df["SIGNIFICANCE_ABS"] <- NULL
    df <- df[1:nrows, ]
    df <- df[with(df, order(SIGNIFICANCE, decreasing=TRUE)), ]
  } else if(ranking %in% c("pval", "adjpval")) {
    df <- df[with(df, order(SIGNIFICANCE)), ]
    df <- df[1:nrows, ]
  } else {
    df <- df[with(df, order(-SIGNIFICANCE)), ]
    df <- df[1:nrows, ]
  }

  df
}



buildFields <- function(df) {
  df <- melt(df, na.rm=T, id=c("UID", "MEAN", "SD", "SIGNIFICANCE", "LOGFOLD", "TTEST", "PVAL", "ADJPVAL", "BVAL"))
  # melt implicitly casts
  # characters to factors to make your life more exciting,
  # in order to encourage more adventures it does not
  # have characters.as.factors=F param.
  df$variable <- as.character(df$variable)
  ZSCORE      <- (df$value - df$MEAN) / df$SD
  df["MEAN"]  <- NULL
  df["SD"]    <- NULL
  df["SIGNIFICANCE"] <- NULL

  names(df)   <- c("UID", "LOGFOLD", "TTEST", "PVAL", "ADJPVAL", "BVAL", "PATIENTID", "VALUE")
  df["ZSCORE"] <- ZSCORE
  df["SUBSET"] <- getSubset(df$PATIENTID)

  return(df)
}



buildExtraFields <- function(df) {
  FEATURE <- rep("Cohort", nrow(df))
  PATIENTID <- as.character(df$PATIENTID)
  TYPE <- rep("subset",nrow(df))
  VALUE <- df$SUBSET
  extraFields <- data.frame(FEATURE, PATIENTID, TYPE, VALUE, stringsAsFactors = FALSE)
}



## Filtering out the rows in df
## where no Gene Symbol/Biomarker is
## defined
dropEmptyGene <- function(d){
  d[!(d$Bio.marker == ""|
        is.null(d$Bio.marker) |
        is.na(d$Bio.marker) |
        is.nan(d$Bio.marker)),]
}



getSubset1Length <- function(measurements) {
  sum(grepl(pattern = SUBSET1REGEX, x = colnames(measurements))) # returns number of column names satisfying regexp.
}



hasTwoSubsets <- function(measurements) {
  getSubset1Length(measurements) < ncol(measurements)
}



getSubset <- function(patientIDs) {
  splittedIds <- strsplit(patientIDs,"_s") # During merge,
  # which is always run we append subset id, either
  # _s1 or _s2 to PATIENTID.
  subsets <- sapply(splittedIds, FUN = tail_elem) # In proper patienid

}



tail_elem <- function(vect, n = 1) {
  as.integer(vect[length(vect) - n + 1])
}



# to check if a subset contains at least one non missing value
subsetHasNonNA <- function (subset, row) {
  # select the  measurements of a subset by matching the subset label with column names
  # each measurements column has subset information as suffix
  # eg:??X1000314002_n0_s2, X1000314002_n0_s1
  subsetMeasurement <- row[grep(paste(c(subset, '$'), collapse=''), names(row))]
  # check if there's non missing values
  sum(!is.na(subsetMeasurement)) > 0
}



# to check if the row contains 4 non missing values and if each subset contains at least one
# non missing value
validMeasurementsRow <- function (row) {
  sum(!is.na(row)) > 3 & subsetHasNonNA('s1', row) &  subsetHasNonNA('s2', row)
}



## Convert GEX matrix to Z-scores
toZscores <- function(measurements) {
  measurements <- scale(t(measurements))
  t(measurements)
}



# Coefficient of variation
coeffVar     <- function(x, na.rm = TRUE) {
  c_sd <- sd(x, na.rm = na.rm)
  c_mean <- mean(x, na.rm = na.rm)
  if (c_mean == 0) {c_mean <- 0.0001} # override mean with 0.0001 when it's zero
  c_sd/c_mean
}



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



## Returns the statistical ranking function based
## on the provided input string except for Diff expr analysis
## related input strings where string "Limma" is simply returned.
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


