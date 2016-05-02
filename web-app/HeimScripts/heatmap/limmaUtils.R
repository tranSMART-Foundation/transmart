
## Differential expression analysis comparing Subset 1 to subset 2 
## using Limma package 
getDEgenes <- function(df) {
  measurements    <- getMeasurements(df)
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
    p       = fit$p.value[, contr],
    method  ='fdr'),
    bval    = fit$lods[, contr]
  )
  
  ## SE: For dev purposes
  # volcanoplot(fit, highlight = 10)
 
  
  cbind(df[,1:2], top.fit)
}

## Generate the design matrix
getDesign <- function(measurements) {
  subsets <- getSubset(colnames(measurements)) #s1 = 1, s2 = 2
  classVectorS1 <- subsets             #s1 = 1, s2 = 2
  classVectorS2 <- - subsets + 3       #s1 = 2, s2 = 1

#   SE: This matrix gives the opposite signs for FCs
#   SE: Check if there is no error in design matrix!!! 
#   testMatrix = cbind(S1=subsets, S2=subsets)
#   testMatrix[which(testMatrix[, "S1"]==2), "S1"]=0
#   testMatrix[which(testMatrix[, "S2"]==1), "S2"]=0
#   testMatrix[which(testMatrix[, "S2"]==2), "S2"]=1
#   return(testMatrix)

  cbind(S1=classVectorS1, S2=classVectorS2)
}

getSubset1Length <- function(measurements) {
  sum(grepl(pattern = SUBSET1REGEX, x = colnames(measurements))) # returns number of column names satisfying regexp.
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

# checking valid measurements on a matrix level
isValidLimmaMeasurements <- function (measurements) {
    sum(apply(measurements, 1, validMeasurementsRow)) > 0
}


####################################################################
## SE: Moved the functions here below from volcanoplot run.R file ##
####################################################################

## SE: Modified header of function to allow providing markerTableJson file name as input argument
writeMarkerTable <- function(markerTable, markerTableJson = "markerSelectionTable.json"){
  colnames(markerTable) <- c("rowLabel", "biomarker",
                             "log2FoldChange", "t", "pValue", "adjustedPValue", "B")
  jsn                   <- toJSON(markerTable, pretty = TRUE, digits = I(17))
  write(jsn, file = markerTableJson)
}

## Deleting the json-format topTable
## from last Limma diff expr analysis run
## SE: Modified header of function to allow providing markerTableJson file name as input argument
cleanUpLimmaOutput <- function(markerTableJson = "markerSelectionTable.json") {
  if (file.exists(markerTableJson)) {
    file.remove(markerTableJson)
  }
}






