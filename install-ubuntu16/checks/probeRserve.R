# barrowed from the file R/cran_pkg.R in transmart-data
# specificially, https://github.com/tranSMART-Foundation/transmart-data/blob/v1.2.4/R/cran_pkg.R

required.packages <- c("reshape2", "ggplot2", "data.table", "Cairo",
		"snowfall", "gplots", "Rserve", "foreach", "doParallel");

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
