
library(RProtoBuf)

# assume we are running from within this file's directory
# if not:
#setwd('~/devel/RInterface')

# Only currently used for assayId, we want to preserve those accurately
options("RProtoBuf.int64AsString" = TRUE)

readProtoFiles('highdim.proto')


#protoBufFile<-"Protobuf file samples//sample.protobuf" ##contains data of type DOUBLE
protoBufFile<-"Protobuf file samples//mrna-all_data.protobuf" ##contains data of type GENERAL





##In the case of data of type general: ALL NUMERICAL VALUES ARE GIVEN AS STRINGS . CONVERT THE NUMERICAL VALUES TO NUMERICALS(DOUBLES)? or do we expect the client to be able to do that?
#NOTE DO WE WANT DO SOMETHING WITH THE (optional) INFORMATION ABOUT BIOMARKERS?
# WE STILL NEED TO CLOSE THE CONNECTION THAT IS OPENED IN THE RESET FUNCTION


## EXAMPLES: run("Protobuf file samples//mrna-all_data.protobuf")
##           run("Protobuf file samples//sample.protobuf")
### SET VARIABLE BELOW TO F IF YOU DON'T WANT TO SEE THE STRING REPRESENTATION OF THE CONTENTS OF THE HEADER AND ROW MESSAGES (which is how they are stored in the protobuf format)
printHeaderAndRowMsgsAsTxt<-T ##REMOVE THIS IN FINAL CODE!





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
	headInfo <- readHeader(conn)
  dataType <- headInfo[["dataType"]]
  mapColumn <- headInfo[["mapColumn"]]
  data <- headInfo[["assaydata"]]
 
  capacity <- 100
	count <- length(data)
	data <- c(data, vector('list', capacity - count))
	while(! is.null(hdrow <- readRow(conn, dataType, mapColumn))) {
		# geometric doubling
		if (count + length(hdrow) > capacity) {
			print(paste('doubling capacity from', capacity))
			data <- c(data, vector('list', capacity))
			capacity <- capacity * 2
		}
		data[(count+1):(count+length(hdrow))] <- hdrow
		names(data)[(count+1):(count+length(hdrow))] <- names(hdrow)
		count <- count + length(hdrow)
	}
	data <- data[1:count]
	do.call(data.frame, c(data, list(row.names=data[["assayId"]])))
}


readHeader <- function(connection) {
	size <- ReadVarint32(connection)
	print(paste('header size:', size))
	msg <- ReadRaw(connection, size=size)
	hdr <- read(highdim.HighDimHeader, msg)
	typeName <- name(highdim.HighDimHeader$RowType$value(hdr$rowsType))
  
	if(typeName != "DOUBLE" && typeName != "GENERAL") {
		stop("HighDim data not of type DOUBLE or GENERAL")
	}
	assays <- hdr$assay

	fieldnames <- c(
		'assayId', 'patientId', 'sampleTypeName', 'timepointName',
		'tissueTypeName', 'sampleCode') ### NOTE: SOME OF THESE ARE OPTIONAL- if a field, e.g. patientId, doesn't exist then for this field the value NULL or NA will be returned 

	assaydata<-tryCatch(lapply(fieldnames, function(f) vapply(assays, function(a) a[[f]], '') ), 
           error = function(e) getAssayDataAvoidingInt64Issue(assays, fieldnames))
  
	names(assaydata) <- fieldnames
  
  if(typeName=="GENERAL")
  {
    mapColumn<-hdr$mapColumn
  }else{mapColumn<-NA}
  
  if(printHeaderAndRowMsgsAsTxt) ##REMOVE THIS IN FINAL CODE!!!
  {
    write(paste("header to string:",toString(hdr)),"") # writes the header object in string format, for debugging purposes. REMOVE THIS IN FINAL CODE
    print(hdr$mapColumn)
  }
 
	
  
  return(list(assaydata=assaydata, dataType=typeName, mapColumn=mapColumn))
}


#workaround to catch problems with Int64 values. In some cases int64 types are unsupported and an int64 type value cannot be directly accessed. 
getAssayDataAvoidingInt64Issue <- function(assays, fieldnames)
{
  assaydata<-list()
  for (fieldname in fieldnames)
  {
    if(fieldname=="assayId")
    {
      assaydata<-c(assaydata, list(vapply(assays, FUN=getField, FUN.VALUE="c", fieldname=fieldname)))
    }else
    {
      assaydata<-c(assaydata, list(vapply(assays, function(a) a[[fieldname]], ''))) 
    }
  }
  return(assaydata)
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


#NOTE DO WE WANT DO SOMETHING WITH THE (optional) INFORMATION ABOUT BIOMARKERS?
readRow <- function(connection, dataType, mapColumn) {
	size <- tryCatch(ReadVarint32(connection),
			 error= function(e) NULL)
	if (is.null(size)) {
		return(NULL)
	}
	print(paste('row size:', size))
	msg <- ReadRaw(connection, size=size)
	row <- read(highdim.Row, msg)
  
  if(printHeaderAndRowMsgsAsTxt)## REMOVE THIS BIT IN FINAL CODE
  {
    write(c("rowTostring:", toString(row)),"") ##for now we don't do anything with the biomarker info. Include?
  }
	
  if(dataType=="DOUBLE")
  {
    rowdata <- list(row$doubleValue) #for double values
    names(rowdata) <- row$label
    print (rowdata)
    #return(rowdata) HERE???
  }
  
  if(dataType=="GENERAL")
  {
    #print(c("msg:",msg))
    rowMsg<-row$mapValue #for general values
    #print(c("rowMsg",rowMsg))
    mapValues<-list()
    label<- row$label
    #print(rowMsg[[1]][["value"]])
    for(i in 1:length(rowMsg))
    {
      mapValue<-list(rowMsg[[i]][["value"]])
      names(mapValue[[1]])<-mapColumn
      mapValues<-c(mapValues,mapValue)
    }
    print(mapValues) ## REMOVE IN FINAL CODE
    
    #make for each type of mapValue (e.g. trialname, rawIntensity,zscore) a separate vector, such that these can easily be converted to separate columns in the dataframe created in the function parseProtoBuf
    rowdata<-lapply(mapColumn, function(f) vapply(mapValues, function(a) a[[f]],''))
    names(rowdata)<-paste(row$label,mapColumn,sep=".")
  }  

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
