tryCatch(
	{
		args <- commandArgs(trailingOnly = TRUE)
		scriptPath <- args[1]
		lowDimPath <- args[2]
		highDimPath_cohort1 <- args[3]
		highDimPath_cohort2 <- args[4]
		settingsPath <- args[5]
		outputPath <- args[6]
		errorPath <- args[7]

		if (! suppressMessages(require(jsonlite))) {
			stop("SmartR requires the R package 'jsonlite'")
		}

		if (file.exists(lowDimPath)) {
			lowDimData <- fromJSON(readChar(lowDimPath, file.info(lowDimPath)$size))
		}

		if (file.exists(highDimPath_cohort1)) {
			if (! suppressMessages(require(data.table))) {
				stop("SmartR requires the R package 'data.table'")
			}
			suppressWarnings(
				highDimData_cohort1 <- as.data.frame(fread(
							highDimPath_cohort1, 
							header=TRUE,
							sep='\t',
							showProgress=FALSE), stringsAsFactors=FALSE)
			)
			highDimData_cohort1 <- transform(highDimData_cohort1, VALUE=as.numeric(VALUE))
		}

		if (file.exists(highDimPath_cohort2)) {
			if (! suppressMessages(require(data.table))) {
				stop("SmartR requires the R package 'data.table'")
			}
			suppressWarnings(
				highDimData_cohort2 <- as.data.frame(fread(
							highDimPath_cohort2, 
							header=TRUE,
							sep='\t',
							showProgress=FALSE), stringsAsFactors=FALSE)
			)
			highDimData_cohort2 <- transform(highDimData_cohort2, VALUE=as.numeric(VALUE))
		}

		settings <- fromJSON(readChar(settingsPath, file.info(settingsPath)$size))

		output <- list()

		source(scriptPath)

		write(toJSON(output, pretty=TRUE), outputPath)
    },
    error=function(err) {
    	write(err$message, file=errorPath)
    },
    warning=function(warn) {
    	write(warn$message, file=errorPath)
    },
    finally={

    }
)    