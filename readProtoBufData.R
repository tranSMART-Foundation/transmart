
library(RProtoBuf)

# assume we are running from within this file's directory
# if not:
#setwd('~/devel/RInterface')

# Only currently used for assayId, we want to preserve those accurately
options("RProtoBuf.int64AsString" = TRUE)

readProtoFiles('highdim.proto')
#protoBufFile<-"Protobuf file samples//sample.protobuf" ##contains data of type DOUBLE
protoBufFile<-"Protobuf file samples//mrna-all_data.protobuf" ##contains data of type GENERAL



reset <- function(fileName) {
	f <<- file(fileName, open='r+b')
	ff <<- ConnectionInputStream(f)
}


run <- function(fileName) {
	reset(fileName)
	parseProtoBuf(ff)
}

# Convert a protobuf stream to a data frame
parseProtoBuf <- function(conn) {
	data <- readHeader(conn)
  dataType<-data[["dataType"]]
  data<-data[["assaydata"]]
 
  capacity <- 100
	count <- length(data)
	data <- c(data, vector('list', capacity - count))
	while(! is.null(hdrow <- readRow(conn, dataType))) {
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
	#do.call(data.frame, c(data, list(row.names='assayId')))
}


readHeader <- function(connection) {
	size <- ReadVarint32(connection)
	print(paste('header size:', size))
	msg <- ReadRaw(connection, size=size)
	hdr <- read(highdim.HighDimHeader, msg)
	typeName <- name(highdim.HighDimHeader$RowType$value(hdr$rowsType))
	#if(typeName != "DOUBLE") {
	#	stop("HighDim data not of type DOUBLE")
	#}

	assays <- hdr$assay

	fieldnames <- c(
		'assayId', 'patientId', 'sampleTypeName', 'timepointName',
		'tissueTypeName', 'sampleCode')

	assaydata<-tryCatch(lapply(fieldnames, function(f) vapply(assays, function(a) a[[f]], '') ), 
           error = function(e) getAssayDataAvoidingInt64Issue(assays, fieldnames))
  
	names(assaydata) <- fieldnames
	#return(lassaydata)	
	return(list(assaydata=assaydata, dataType=typeName))
}


#workaround to catch problems with Int64 values. In some cases int64 types are unsupported and an int64 type value cannot be directly accessed. 
getAssayDataAvoidingInt64Issue <- function(assays, fieldnames)
{
  lapply(fieldnames, 
         function(f) vapply(assays, FUN=getField, FUN.VALUE="c", fieldname=f) )
  
  
}

getField<-function(assayDescr,fieldname )
{
  assayAsStrings<-strsplit(as.character(assayDescr),split="\n")[[1]]
  desiredFieldString<-grep(fieldname, assayAsStrings, value=T)
  separatedString<-scan(text=desiredFieldString, what="c", quiet=T)
  if(length(separatedString)!=2){stop(paste("error: could not retrieve value of field", fieldname))}
  desiredField<-separatedString[2]
  return(desiredField)
}

readRow <- function(connection, dataType) {
	size <- tryCatch(ReadVarint32(connection),
			 error= function(e) NULL)
	if (is.null(size)) {
		return(NULL)
	}
	print(paste('row size:', size))
	msg <- ReadRaw(connection, size=size)
	row <- read(highdim.Row, msg)
  
  if(dataType=="DOUBLE")
  {
    rowdata <- list(row$doubleValue) #for double values
    names(rowdata) <- row$label
    #return(rowdata) HERE???
  }
  
  if(dataType=="GENERAL")
  {
    #print(c("msg:",msg))
    print(c("row:",row))
    write(c("rowTostring:", toString(row)),"") ##for now we don't do anything with the biomarker info. Include?
    rowMsg<-row$mapValue #for general values
    #print(c("rowMsg",rowMsg))
    mapValues<-list()
    
    for(i in 1:length(rowMsg))
    {
      mapValues<-c(mapValues,rowMsg[[i]][["value"]])
    }
    
   # GIVE LABEL TO THE MAPVALUES (as done for the data with DOUBLE values; names(rowdata)<-row$label )
  #  DEFINE ROWDATA so it can be returned
    
  }  

  return(rowdata) #MAKE SURE RECEIVING FUNCTION (where it returns the value TO) CAN DO SOMETHING WITH THE rowoutput for "GENERAL" data. 
                  #ADAPT THE RECEIVING FUNCTION SO IT CAN MAKE A DATAFRAME OUT OF THE DATA (split the 4 elements (values) per map so they make up 4 rows/columns?)
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
