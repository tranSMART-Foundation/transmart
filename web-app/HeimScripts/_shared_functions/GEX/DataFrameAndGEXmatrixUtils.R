##########################################################################
## This file contains more generic functionalities when working with    ##
## "df-type"-data frame containing Microarray GEX data  as well as      ##
## GEX matrix variables containing measurements and sample ids as       ##
## colnames.                                                            ##
## "df-type": a data frame with GEX matrix data as well as Limmy diff   ##
## expr statistics or other statistics used to order and filter the df  ##
## GEX data frame (variance, coefficient of variation, ... )            ##
##########################################################################


## Loading functions ##
utils <- paste(remoteScriptDir, "/_shared_functions/Generic/utils.R", sep="")
source(utils)



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


## Sorting of measurement columns
## in input data frame
applySorting <- function(df,sorting) {
  
  measurements <- getMeasurements(df)
  colNames <- names(measurements)
  subsets <- getSubset(colNames)
  nodes <- getNode(colNames)
  subjects <- getSubject(colNames)

  
  timelineValues <- getTimelineValues(nodes, fetch_params$ontologyTerms)

  
   ## Converting subjects vector to integer if required for ordering
   subjects_are_integer.logical = all(grepl("^\\d+$", subjects, perl = TRUE))
 
   if(subjects_are_integer.logical)
     subjects = as.integer(subjects)

   
  ## Performing the sorting of columns
    ## - according to nodes
  if (sorting == "nodes") {
    inds <- order(subsets, timelineValues, nodes, subjects)
  
    ## - or subjects
  } else if (sorting == "subjects") {
    inds <- order(subsets, subjects, timelineValues, nodes)
    ## - else just stop the script as sth is wrong in the input params for this function
  } else{
    stop(paste("applySorting: Sorting can only be performed according to nodes/subjects. Please check your input:", sorting))
  }

  measurements <- measurements[, inds]

  ## Returning the data frame with reordered columns
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
  means = data.frame(MEAN=numeric())
  sdses = data.frame(SD=numeric())
  
  
  # rankingMethod is either a function or character string "limma"
  useLimma <- !is.function(rankingMethod) 
  
  # Is the GEX matrix valid for differential expression analysis this means containing enough not NA values?
  validLimmaMeasurements <- isValidLimmaMeasurements(measurements)


  #this is the case for more than 1 sample available in the data frame
  if (ncol(df) > 3) {


    df <- applySorting(df,sorting)  # no need for sorting in one sample case, we do it here only

    

    validLimmaMeasurements <- isValidLimmaMeasurements(measurements)


    if (twoSubsets && validLimmaMeasurements) {
      markerTable  <- getDEgenes(df)
      
      logfold.values <- markerTable["logfold"]
      ttest.values <- markerTable["ttest"]
      pval.values <- markerTable["pval"]
      adjpval.values <- markerTable["adjpval"]
      bval.values <- markerTable["bval"]
    }                                 
    
    

    ## Cannot use limma diff expr analysis for a single subset only.
    if (useLimma  && !twoSubsets) { 
      stop( paste("Illegal ranking method: ", ranking, " two subsets needed.") )
    }

    ## rankingScore provided based on Limma stat output
    if (useLimma && validLimmaMeasurements)  {

      if (!ranking %in% colnames(markerTable) )
        stop(paste("Illegal ranking method selected: ", ranking) )
      
      # Obtain the rankingScore based on selected diff expr statistics
      rankingScore <- markerTable[ranking]
      
    } 
    
 
    ## In case that the user does not want limma
    if(!useLimma){
        rankingScore <- apply(measurements, 1, rankingMethod, na.rm = TRUE )  # Calculating ranking per probe (per row)
    }
    
    
    cleanUpLimmaOutput(markerTableJson = markerTableJson)  # In order to prevent displaying the table from previous run.
    
    
    ## Copy of markerTable that will be used for file dump
    if(exists("markerTable")){
      markerTable_forFileDump = markerTable
      markerTable_forFileDump["SIGNIFICANCE"] <- rankingScore
      markerTable_forFileDump                 <- applyRanking(markerTable_forFileDump, ranking, max_rows)
      markerTable_forFileDump["SIGNIFICANCE"] <- NULL
      writeMarkerTable(markerTable_forFileDump, markerTableJson = markerTableJson)
    }
    

    # this is just an auxiliary column - it will not be used for JSON.
    sdses <- apply(measurements,1 ,sd , na.rm = T)  
  
  } else{
    ## In case there is only one sample some ranking methods won't work
    if(ranking %in% c("mean", "median")){
      rankingScore <- apply(measurements, 1, rankingMethod, na.rm = TRUE )  # Calculating ranking per probe (per row)
    } else{
      stop(paste("This dataset contains only one sample. Illegal ranking method selected: ", ranking))
    }
 
    
  }
  
  # this is just an auxiliary column - it will not be used for JSON.
  means <- rowMeans(measurements, na.rm = T)
  
  
  df["MEAN"]         <- means
  df["SD"]           <- sdses
  df["SIGNIFICANCE"] <- rankingScore
  df["LOGFOLD"] <- logfold.values
  df["TTEST"] <- ttest.values
  df["PVAL"] <- pval.values
  df["ADJPVAL"] <- adjpval.values
  df["BVAL"] <- bval.values

  if (useLimma & !validLimmaMeasurements) {
    # don't apply ranking but throw an error message
    stop(paste("The GEX matrix does not contain enough valid measurements to",
                "perform differential expression analysis.",
                "Please check the high dimensional input data"))
  } else {
    # else apply ranking
    df <- applyRanking(df, ranking, max_rows)
  }
  
  return(df)
}


## This function generates a data frame containing the complete set
## of rankings according to the following statistics:
## -- coefficient of variation
## -- variance
## -- mean
## -- median
## -- log Fold change
## -- P-value
## -- Adjusted P-value
## -- B value
## Statistics are only computed if data allows it. Else the corresponding values
## for this  column in the results data frame remains set to NA. Ranking is performed
## for all statistics from highest to lowest value.
getAllStatRanksForExtDataFrame = function(df){
  
  ## We expect in this case that Limma has been performed and the
  ## data frame contains valid statistical data for Limma
  hasLimma =  ifelse(isValidLimmaMeasurements(getMeasurements(df)), TRUE, FALSE)
  

  ## Checking that the stat colnames exist for Limma analysis
  colnamesStat = c("LOGFOLD", "TTEST", "PVAL", "ADJPVAL", "BVAL")
  containsAllColnamesStat = ifelse(length(which(colnames(df) %in% colnamesStat))==length(colnamesStat), TRUE, FALSE)
  
  
  ## SE: HERE WE CAN CHECK IF limma-specific columns contain numeric data!!!!!
  
  
  ## Checking if coefficient of variation and variance can be calculated
  performCoef = isValidCoefMeasurements(getMeasurements(df))
  performVariance = isValidVarianceMeasurements(getMeasurements(df))
  performRange = isValidRangeMeasurements(getMeasurements(df))
  
  
  ## Statistics that are provided in the output ranking data frame
  ## coef, variance, range, means, median, logfold, ttest, pval, adjpval and bval
  ## so altogether 11 columns including uid
  
  
  rankingStat.df = data.frame(UID = df$UID, COEF = NA, VARIANCE = NA,
                              RANGE = NA, MEAN = NA,
                              MEDIAN = NA, TTEST = NA,
                              LOGFOLD = NA, PVAL = NA,
                              ADJPVAL = NA, BVAL = NA)
  
  
  
  ### RANKING INFO ... ###
  
  ## - for limma
  if(hasLimma && containsAllColnamesStat){
    
    ## Vectors storing the ordered item position
    ## in input vector in drecreasing order ...
    idx_logfold_rank.vec = order(df$LOGFOLD, decreasing = TRUE)
    idx_ttest_rank.vec = order(df$TTEST, decreasing = TRUE)
    idx_pval_rank.vec = order(df$PVAL, decreasing = TRUE)
    idx_adjpval_rank.vec = order(df$ADJPVAL, decreasing = TRUE)
    idx_bval_rank.vec = order(df$BVAL, decreasing = TRUE)
    
    ## ... and applying this data to obtain the corresponding
    ## ranks according to UID
    for(i in 1:length(idx_logfold_rank.vec)){
      rankingStat.df$LOGFOLD[idx_logfold_rank.vec[i]] = i
      rankingStat.df$TTEST[idx_ttest_rank.vec[i]] = i
      
      rankingStat.df$PVAL[idx_pval_rank.vec[i]] = i
      rankingStat.df$ADJPVAL[idx_adjpval_rank.vec[i]] = i
      rankingStat.df$BVAL[idx_bval_rank.vec[i]] = i
    }
    
  }
    
  ## - for other statistics (coefficient of variance, variance,
  ##   range, mean, median)
    
  stat_tests = logical( length = 5)
  stat_tests[stat_tests==FALSE] = TRUE
    
  names(stat_tests) = c("coeffVar", "var", "normRange", "mean", "median")
    
    
  stat_tests2col = c("COEF", "VARIANCE", "RANGE", "MEAN", "MEDIAN")
  names(stat_tests2col) = names(stat_tests)
    
  ## Checking if Coefficient of variation and Variance can
  ## be computed
  if(!performCoef)
    stat_tests["coeffVar"] = FALSE
    
  if(!performVariance)
    stat_tests["var"] = FALSE
    

  if(!performRange)
    stat_tests["normRange"] = FALSE
  
      
  ## Getting the measurements data frame
  ## to perform ranking on
  measurements.df = getMeasurements(df)
    
    
  ## Performing ranking for all statistics (besides Limma specific ones)
  for(i in 1:length(stat_tests)){
    if(stat_tests[i]){
      stat_test = names(stat_tests)[i]
        
      idx_stat_test_rank.vec = order(apply(measurements.df, 1, stat_test), decreasing = TRUE)
        
      for(j in 1:length(idx_stat_test_rank.vec)){
        rankingStat.df[[stat_tests2col[stat_test]]][idx_stat_test_rank.vec[j]] = j
      }
        
    }
      
  }
  return(rankingStat.df)
}




## Returns data frame containing
## the intensity/expression measures as matrix
## and the header which corresponds to the sample
## names. In case the extended data frame generated
## by function addStats gets provided the corresponding
## stat columns get removed.
getMeasurements <- function(df) {
  
  ## This is the first col containing measurements
  idx_first_data_col = 3
  
  ## Removing the statistics columns in case the
  ## extended df containing stat info has been provided as input
  idx = -which(colnames(df) %in% c("MEAN", "SD",
                                   "SIGNIFICANCE", "LOGFOLD",
                                   "TTEST", "PVAL", "ADJPVAL", "BVAL"))
  
  
  ## In case it is an extended data frame containing stat info
  if(length(idx)>0){
    ## Removing the stat columns
    df = df[,idx]
    ## first col containing measurements is getting
    ## adapted to this data frame type
    idx_first_data_col = 2
  }
     
  ## Data columns get returned
  if (ncol(df) > idx_first_data_col){
    return(df[, idx_first_data_col:ncol(df)])
  } else {
    return( df[idx_first_data_col] )
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
  
  uids[df$Row.Label == dupl.rows$Row.Label] <- paste(uids[df$Row.Label == dupl.rows$Row.Label],
                                                     dupl.rows$Bio.marker[df$Row.Label == dupl.rows$Row.Label],
                                                     sep="--")
  
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


## Checking if the GEX matrix contains
## at least two samples
hasMinTwoSamples <- function(measurements) {
  if(dim(measurements)[2]>1){
    return(TRUE)
  } else{
    return(FALSE)
  }
}


## Wrapper function to check if GEX matrix is valid
## to calculate coefficient of variation
isValidCoefMeasurements <- function(measurements){
  return(hasMinTwoSamples(measurements))
}

## Wrapper function to check if GEX matrix is valid
## to calculate variance
isValidVarianceMeasurements <- function(measurements){
  return(hasMinTwoSamples(measurements))
}

## Wrapper function to check if GEX matrix is valid
## to calculate range
isValidRangeMeasurements <- function(measurements){
  return(hasMinTwoSamples(measurements))
}


getSubset <- function(patientIDs) {
  splittedIds <- strsplit(patientIDs,"_s") # During merge,
  # which is always run we append subset id, either
  # _s1 or _s2 to PATIENTID.
  subsets <- sapply(splittedIds, FUN = tail_elem) # In proper patienid

}


## Returns the nth last element from a vector
## Integers are casted from string to integer if necessary
## This function is helpful after performing strsplit 
tail_elem <- function(vect, n = 1) {
  
 value = vect[length(vect) - n + 1]
 
 if(length(grep("^\\d+$", value, value = FALSE, perl = TRUE))>0){
  as.integer(value)
 } else{
   return(value)
 }
 
 
 
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
  
  ## SE: Minimum 3 valid measures are needed for Limma
  ##sum(!is.na(row)) > 3 & subsetHasNonNA('s1', row) &  subsetHasNonNA('s2', row) 
  sum(!is.na(row)) > 2 & subsetHasNonNA('s1', row) &  subsetHasNonNA('s2', row)
  
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


