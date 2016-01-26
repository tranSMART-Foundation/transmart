
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
  subsets <- getSubset(colnames(measurements)) #s1 = 0, s2 = 1
  classVectorS1 <- subsets + 1    #s1 = 1, s2 = 2
  classVectorS2 <- - subsets + 2  #s1 = 2, s2 = 1
  cbind(S1=classVectorS1, S2=classVectorS2)
}

getSubset1Length <- function(measurements) {
  sum(grepl(pattern = SUBSET1REGEX, x = colnames(measurements))) # returns number of column names satisfying regexp.
}
