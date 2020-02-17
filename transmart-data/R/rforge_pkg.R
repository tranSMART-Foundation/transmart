required.packages <- c("Rserve");
missing.packages <- function(required) {
	return(required[
		!(required %in% installed.packages()[,"Package"])]);
}
new.packages <- missing.packages(required.packages)
if (!length(new.packages))
	q();
if (length(intersect(new.packages, c("Rserve")))) {
	install.packages('Rserve',,'http://www.rforge.net/')
}

if (length(new.packages)) {
	install.packages(new.packages, repos=Sys.getenv("CRAN_MIRROR"));
}

if (length(missing.packages(required.packages))) {
	warning('Some packages not installed');
	quit(1);
}
