# copied from the file R/cran_pkg.R in transmart-data
# specifically, https://github.com/tranSMART-Foundation/transmart/tree/master/transmart-data/R/cran_pkg.R

required.packages <- c("reshape", "reshape2", "ggplot2", "data.table", "Cairo",
		"snowfall", "gplots", "foreach", "doParallel", "visreg",
		"pROC", "jsonlite", "RUnit", "shiny", "Rserve", "WGCNA", "CGHtest", "CGHtestpar");

missing.packages <- function(required) {
	return(required[
		!(required %in% installed.packages()[,"Package"])]);
}

thePossiblyMissingPackages <- missing.packages(required.packages)

if (length(thePossiblyMissingPackages)) {
	warning('Some R packages not installed: ',thePossiblyMissingPackages);
	quit(save="no",status=1);
}

required.biocpackages <- c("impute", "multtest", "CGHbase", "edgeR", "DESeq2", "limma", "snpStats", "preprocessCore", "GO.db", "AnnotationDbi", "QDNAseq");

missing.biocpackages <- function(required) {
	return(required[
		!(required %in% installed.packages()[,"Package"])]);
}

thePossiblyMissingBiocPackages <- missing.biocpackages(required.biocpackages)

if (length(thePossiblyMissingBiocPackages)) {
	warning('Some BioConductor packages not installed: ',thePossiblyMissingBiocPackages);
	quit(save="no",status=1);
}

quit(save="no",status=0);
