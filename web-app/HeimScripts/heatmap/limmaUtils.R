
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
      p      = fit$p.value[, contr],
      method ='fdr'),
    bval    = fit$lods[, contr]
  )
  cbind(df[,1:2], top.fit)
}

getDesign <- function(measurements) {
  subsets <- getSubset(colnames(measurements)) #s1 = 0, s2 = 1
  classVectorS1 <- subsets + 1    #s1 = 1, s2 = 2
  classVectorS2 <- - subsets + 2  #s1 = 2, s2 = 1
  cbind(S1=classVectorS1, S2=classVectorS2)
}

getSubset1Length <- function(measurements) {
  sum(grepl(pattern = SUBSET1REGEX, x = colnames(measurements))) # returns number of column names satisfying regexp.
}

# to check if a subset contains at least one non missing value
subsetHasNonNA <- function (subset, row) {
   # select the  measurements of a subset by matching the subset label with column names
   # each measurements column has subset information as suffix
   # eg: X1000314002_n0_s2, X1000314002_n0_s1
   subsetMeasurement <- row[grep(paste(c(subset, '$'), collapse=''), names(row))]
   # check if there's non missing values
   sum(!is.na(subsetMeasurement)) > 0
}

# to check if row  contains at least one row that contains 3 non missing values and if a subset contains at least one
# non missing value
validMeasurementsRow <- function (row) {
  sum(!is.na(row)) > 2 & subsetHasNonNA('s1', row) &  subsetHasNonNA('s2', row)
}

# checking valid measurements
isValidLimmaMeasurements <- function (measurements) {
    sum(apply(measurements, 1, validMeasurementsRow)) > 0
}
