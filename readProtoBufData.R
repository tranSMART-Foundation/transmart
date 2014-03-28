
library(RProtoBuf)

# assume we are running from within this file's directory
# if not:
setwd('~/devel/RInterface')

# Only currently used for assayId, we want to preserve those accurately
options("RProtoBuf.int64AsString" = TRUE)

readProtoFiles('highdim.proto')

f <- file('sample.protobuf', open='r+b')

ff <- ConnectionInputStream(f)

reset <- function() {
  f <<- file('sample.protobuf', open='r+b')
  ff <<- ConnectionInputStream(f)
}

run <- function() {
  reset()

parseProtoBuf <- function(conn)
	hdr <- readHeader(conn)
	capacity <- 100
	count <- 0
  rows <- vector('list', capacity)
  while(! is.null(hdrow <- readRow(conn))) {
	  # geometric doubling
	  if (count == capacity) {
	    capacity <- capacity * 2
			newrows <- vector('list', capacity)
			newrows[1:count] <- rows
			rows <- newrows
		}
		count <- count + 1
		rows[count] <- hdrow
  }
  messages <- list(header=hdr, rows=rows)
	toDataFrame(messages)
}

readHeader <- function(connection) {
  size <- ReadVarint32(connection)
  print(paste('size:', size))
  msg <- ReadRaw(connection, size=size)
  hdr <- read(highdim.HighDimHeader, msg)
  typeName <- name(highdim.HighDimHeader$RowType$value(hdr$rowsType))
  if(typeName != "DOUBLE") {
    stop("HighDim data not of type DOUBLE")
  }
  hdr
}

readRow <- function(connection) {
  size <- tryCatch(ReadVarint32(connection),
                   error= function(e) NULL)
  if (is.null(size)) {
    return(NULL)
  }
  print(paste('size:', size))
  msg <- ReadRaw(connection, size=size)
  row <- read(highdim.Row, msg)
  row
}

toDataFrame <- function(messages) {
  header <- messages$header
	assays <- messages$assay
  rows <- messages$rows

	fieldnames = c(
	  'assayId', 'patientId', 'sampleTypeName', 'timepointName',
		'tissueTypeName', 'sampleCode')

	fields <- c(
	    lapply(fieldnames, function(f) vapply(assay, function(a) a[[f]], '') )
			lapply(rows, function(r) r$doubleValue)
		)
	names(fields) <- c(fieldnames, vapply(rows, function(r) { r$label }, '') )

	do.call(data.frame, c(fields, row.names=patientId))
}