# Copyright (c) 2014 The Hyve B.V.
# This code is licensed under the GNU General Public License,
# version 3, or (at your option) any later version.

getHighdimData <- function(study.name, concept.match = NULL, concept.link = NULL, projection = NULL, binary.file = NULL, binary.save.filename = NULL) {
  require("RProtoBuf")
  
  .checkTransmartConnection()
    
    if (is.null(concept.link) && !is.null(concept.match)) {
        studyConcepts <- getConcepts(study.name)
        conceptFound <- grep(concept.match, studyConcepts$name)[1]
        if (is.na(conceptFound)) {
            warning(paste("No match found for:", concept.match)) 
        } else { concept.link <- studyConcepts$api.link.self.href[conceptFound] }
    }
    
    if (length(concept.link) == 0L) {
        warning("No concepts selected or found to match your arguments.")
        return(NULL)
    }
    
    serverResult <- .transmartServerGetRequest(
            paste(concept.link, "/highdim", sep=""),
            accept.type = "hal")
    if (length(serverResult$dataTypes) == 0) {
        warning("This high dimensional concept contains no data.")
        return(NULL)
    }
    listOfHighdimDataTypes <- serverResult$dataTypes[[1]]
    
    if (!is.null(projection)) {
        matchingProjectionIndex <- which(names(listOfHighdimDataTypes$api.link) == projection)
        if (length(matchingProjectionIndex) > 0) {
            projectionLink <- listOfHighdimDataTypes$api.link[[matchingProjectionIndex]]
        } else { projection <- NULL }
    } else { projection <- NULL }
    
    if (is.null(projection)) {
        cat("No valid projection selected.\nSet the projection argument to one of the following options:\n ")
        cat(paste(listOfHighdimDataTypes$supportedProjections,"\n"))
        return(listOfHighdimDataTypes$supportedProjections)
    }
    cat("Retrieving data from server.", as.character(Sys.time()), "\n")
    serverResult <- .transmartServerGetRequest(projectionLink, accept.type = "binary")
    if (length(serverResult$content) == 0) {
        warning("Error in retrieving high dim data.")
        return(NULL)
    }

    return(.parseHighdimData(serverResult$content))
}

.parseHighdimData <- function(rawVector) {    
    dataChopper <- .messageChopper(rawVector)
    
    protoFileLocation <- system.file("extdata", "highdim.proto", package="transmartRClient")
    readProtoFiles(protoFileLocation)
    
    message <- dataChopper$getNextMessage()
    header <- read(highdim.HighDimHeader, message)
    columnSpec <- header$columnSpec
    assays <- header$assay
    
    assayLabels <- .DollarNames(assays[[1]])[1:length(assays[[1]])]
    assayHeaders <- list()
    for (label in assayLabels) {
        row <- c()
        if (label == "assayId") {
            row[["assayId"]] <- sapply(assays,
                    function(a) sub("assayId: ", "", grep(label, strsplit(as.character(a), split = "\n")[[1]], value = TRUE)))
        } else {
            row[[label]] <- sapply(assays, function(a) a[[label]])
        }
        assayHeaders <- c(assayHeaders, row)
    }
    
    assayHeaders <- as.data.frame(assayHeaders, stringsAsFactors = FALSE)
    noAssays <- dim(assayHeaders)[1]
    cat(paste("Received data for",noAssays,"assays. Unpacking data and converting to data.frame.",as.character(Sys.time()),"\n"))
    
    dataChopper$startProgressBar()
    assayData <- list()
    labelToBioMarker<- c() #biomarker info is optional, but should not be omitted, as it is also part of the data
    while (!is.null(message <- dataChopper$getNextMessage())) {
            row <- read(highdim.Row, message)
            rowlabel<-row$label
              
            labelToBioMarker[[rowlabel]]<-row$bioMarker
            rowValues <- row$value
        
            for(i in 1:length(rowValues)) 
            {
                dataType<-name(highdim.ColumnSpec$ColumnType$value(columnSpec[[i]]$type))
                entryName <- paste(rowlabel, columnSpec[[i]]$name, sep=".")
                if(dataType == "STRING")
                {
                  assayData [[entryName]] <- rowValues[[i]]$stringValue #add the values of one column value to a list. The name of this vector is the rowlabel concatenated to the column specification 
                }
                if(dataType == "DOUBLE")
                {
                  assayData [[entryName]] <- rowValues[[i]]$doubleValue #add the values of one column value to a list. The name of this vector is the rowlabel concatenated to the column specification 
                }
            }
        dataChopper$updateProgressBar()
        
    }
    dataChopper$stopProgressBar()
    
    assayData <- as.data.frame(assayData, stringsAsFactors=FALSE)
    data<-cbind(assayHeaders, assayData)
    
    
    labelToBioMarker<-as.data.frame(labelToBioMarker)
    colnames(labelToBioMarker)<-"bioMarker"
    labelToBioMarker[,"label"]<-rownames(labelToBioMarker)
    labelToBioMarker<-labelToBioMarker[,c("label","bioMarker")]
    rownames(labelToBioMarker)<-NULL
    
    cat("Data unpacked.", as.character(Sys.time()),"\n")
    
    
    if(all(labelToBioMarker[,"bioMarker"]==""))
    {
      labelToBioMarker<-"No biomarker information is available for this dataset"
      return(data)
    }
    cat("Additional biomarker information is available. Returning a list, containing the high dimensional data and a table describing which (column) labels refer to which bioMarker\n")
    return(list(highdimData=data, labelToBioMarkerMap = labelToBioMarker))
}

.messageChopper <- function(rawVector, endOfLastMessage = 0) {
    msbSetToOne <- as.raw(128)
    progressTotal <- length(rawVector)
    
    getNextMessage <- function() {
        if (endOfLastMessage >= length(rawVector)) {return(NULL)}
        varint32Size <- max(which((msbSetToOne & rawVector[(endOfLastMessage+1):(endOfLastMessage+5)]) > 0)) + 1
        varint32Connection <- rawConnection(rawVector[(endOfLastMessage+1):(endOfLastMessage+varint32Size)], open = "r+b")
        class(varint32Connection) <- "connection"
        connection <- ConnectionInputStream(varint32Connection)
        close(varint32Connection)
        messageSize <- tryCatch(ReadVarint32(connection), error= function(e) NULL)
        if (is.null(messageSize)) return(NULL)
        endOfThisMessage <- endOfLastMessage + varint32Size + messageSize
        message <- rawVector[(endOfLastMessage+1+varint32Size):endOfThisMessage]
        endOfLastMessage <<- endOfThisMessage
        return(message)
    }
    
    startProgressBar <- function() { pb <<- txtProgressBar(min = 0, max = progressTotal, style = 3) }
    updateProgressBar <- function() { setTxtProgressBar(pb, endOfLastMessage) }
    stopProgressBar <- function() { close(pb) }
    
    return(list(getNextMessage = getNextMessage, startProgressBar = startProgressBar,
            updateProgressBar = updateProgressBar, stopProgressBar = stopProgressBar))
}

