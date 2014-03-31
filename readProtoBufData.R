
library(RProtoBuf)

# assume we are running from within this file's directory
# if not:
#setwd('~/devel/RInterface')

# Only currently used for assayId, we want to preserve those accurately
options("RProtoBuf.int64AsString" = TRUE)

readProtoFiles('highdim.proto')

reset <- function() {
	f <<- file('sample.protobuf', open='r+b')
	ff <<- ConnectionInputStream(f)
}

reset()

run <- function() {
	reset()
	parseProtoBuf(ff)
}

# Convert a protobuf stream to a data frame
parseProtoBuf <- function(conn) {
	data <- readHeader(conn)
	capacity <- 100
	count <- length(data)
	data <- c(data, vector('list', capacity - count))
	while(! is.null(hdrow <- readRow(conn))) {
		# geometric doubling
		if (count == capacity) {
			print(paste('doubling capacity from', capacity))
			data <- c(data, vector('list', capacity))
			capacity <- capacity * 2
		}
		count <- count + 1
		data[count] <- hdrow
		names(data)[count] <- names(hdrow)
	}
	data <- data[1:count]
	do.call(data.frame, c(data, list(row.names='assayId')))
}

readHeader <- function(connection) {
	size <- ReadVarint32(connection)
	print(paste('header size:', size))
	msg <- ReadRaw(connection, size=size)
	hdr <- read(highdim.HighDimHeader, msg)
	typeName <- name(highdim.HighDimHeader$RowType$value(hdr$rowsType))
	if(typeName != "DOUBLE") {
		stop("HighDim data not of type DOUBLE")
	}

	assays <- hdr$assay

	fieldnames = c(
		'assayId', 'patientId', 'sampleTypeName', 'timepointName',
		'tissueTypeName', 'sampleCode')

	assaydata <- lapply(fieldnames, function(f) vapply(assays, function(a) a[[f]], '') )
	names(assaydata) <- fieldnames
	return(assaydata)
}

readRow <- function(connection) {
	size <- tryCatch(ReadVarint32(connection),
			 error= function(e) NULL)
	if (is.null(size)) {
		return(NULL)
	}
	print(paste('row size:', size))
	msg <- ReadRaw(connection, size=size)
	row <- read(highdim.Row, msg)
	rowdata <- list(row$doubleValue)
	names(rowdata) <- row$label
	return(rowdata)
}

toDataFrame <- function(messages) {
	header <- messages$header
	assays <- messages$assay
	rows <- messages$rows

	fields <- c(
			lapply(fieldnames, function(f) vapply(assay, function(a) a[[f]], '') ),
			lapply(rows, function(r) r$doubleValue)
		)
	names(fields) <- c(fieldnames, vapply(rows, function(r) { r$label }, '') )

	do.call(data.frame, c(fields, row.names=patientId))
}
