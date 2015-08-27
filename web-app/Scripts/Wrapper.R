tryCatch(
	{
		args <- commandArgs(trailingOnly = TRUE)
		scriptPath <- args[1]
		lowDimPath <- args[2]
		highDimPath_cohort1 <- args[3]
		highDimPath_cohort2 <- args[4]
		idMappingPath <- args[5]
		settingsPath <- args[6]
		outputPath <- args[7]
		errorPath <- args[8]

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
				highDimData_cohort1 <- as.data.frame(fread(highDimPath_cohort1, header=TRUE, sep='\t', showProgress=FALSE), stringsAsFactors=FALSE)
			)
		}

		if (file.exists(highDimPath_cohort2)) {
			if (! suppressMessages(require(data.table))) {
				stop("SmartR requires the R package 'data.table'")
			}
			suppressWarnings(
				highDimData_cohort2 <- as.data.frame(fread(highDimPath_cohort2, header=TRUE, sep='\t', showProgress=FALSE), stringsAsFactors=FALSE)
			)
		}

		if (file.exists(idMappingPath)) {
			idMapping <- fromJSON(readChar(idMappingPath, file.info(idMappingPath)$size))
			if (file.exists(highDimPath_cohort1)) {
				highDimData_cohort1$'PATIENT ID' <- sapply(highDimData_cohort1$'PATIENT ID', function(id) idMapping[[toString(id)]])
			}
			if (file.exists(highDimPath_cohort2)) {
				highDimData_cohort2$'PATIENT ID' <- sapply(highDimData_cohort2$'PATIENT ID', function(id) idMapping[[toString(id)]])
			}
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