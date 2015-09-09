if (! suppressMessages(require(jsonlite))) {
	stop("SmartR requires the R package 'jsonlite'")
}
if (! suppressMessages(require(data.table))) {
	stop("SmartR requires the R package 'data.table'")
}

readJSONFile <- function(path) {
	data <- data.frame()
	if (file.exists(path)) {
		data <- fromJSON(readChar(path, file.info(path)$size))								
	}
	data
}

readHighDimFile <- function(path) {
	data <- data.frame()
	if (file.exists(path)) {		
		suppressWarnings(
			data <- as.data.frame(fread(
						path, 
						header=TRUE,
						sep='\t',
						showProgress=FALSE), stringsAsFactors=FALSE)
		)
		data <- transform(data, VALUE=as.numeric(VALUE))
	}
	data
}

lowDimData_cohort1 <- readJSONFile(lowDimPath_cohort1)		
lowDimData_cohort2 <- readJSONFile(lowDimPath_cohort2)

highDimData_cohort1 <- readHighDimFile(highDimPath_cohort1)
highDimData_cohort2 <- readHighDimFile(highDimPath_cohort2)

settings <- fromJSON(settings)

output <- list()

source(scriptPath)

toString(toJSON(output))
