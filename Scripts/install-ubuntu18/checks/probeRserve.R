# barrowed from the file R/cran_pkg.R in transmart-data
# specifically, https://github.com/tranSMART-Foundation/transmart/tree/master/transmart-data/R/cran_pkg.R

required.packages <- c("reshape", "reshape2", "ggplot2", "data.table", "Cairo",
		"snowfall", "gplots", "foreach", "doParallel", "visreg",
		"pROC", "jsonlite", "RUnit", "shiny");

missing.packages <- function(required) {
	return(required[
		!(required %in% installed.packages()[,"Package"])]);
}

thePossiblyMissingPackages <- missing.packages(required.packages)

if (length(thePossiblyMissingPackages)) {
	warning('Some packages not installed');
	quit(save="no",status=1);
}

quit(save="no",status=0);
