# Copyright (c) 2014 The Hyve B.V.
# This code is licensed under the GNU General Public License,
# version 3, or (at your option) any later version.

getHighdimData <- function(study.name, concept.match = NULL, concept.link = NULL, projection = NULL) {
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
    return(.parseHighdimData(serverResult))
}

.parseHighdimData <- function(serverResult) {    
    require("RProtoBuf")
    
    protoFileLocation <- system.file("extdata", "highdim.proto", package="transmartRClient")
    readProtoFiles(protoFileLocation)
    
    contentConnection <- rawConnection(serverResult$content, open = "r+b")
    class(contentConnection) <- "connection"
    connection <- ConnectionInputStream(contentConnection)
    
    message <- .getChunk(connection)
    header <- read(highdim.HighDimHeader, message)
    typeName <- name(highdim.HighDimHeader$RowType$value(header$rowsType))
    mapColumn <- header$mapColumn
    assays <- header$assay
    
    if(all(typeName != c("DOUBLE", "GENERAL"))) stop("HighDim data not of type DOUBLE or GENERAL")
    
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
    
    assayHeaders <- as.data.frame(assayHeaders)
    noAssays <- dim(assayHeaders)[1]
    cat(paste("Received data for",noAssays,"assays. Unpacking data and converting to data.frame.",as.character(Sys.time()),"\n"))
    
    assayData <- list()
    while (!is.null(message <- .getChunk(connection))) {
        row <- read(highdim.Row, message)
        if(typeName=="DOUBLE") {
            assayData[[row$label]] <- row$doubleValue #for double values
        }
        if(typeName=="GENERAL") {
            rowValues <- row$mapValue
            rowData <- c()
            for(singleType in rowValues) {
                rowEntry <- singleType$value
                names(rowEntry) <- paste(row$label, mapColumn, sep=".")
                rowData <- rbind(rowData, rowEntry)
            }
            rownames(rowData) <- NULL
            for (colIndex in 1:ncol(rowData)) assayData[[colnames(rowData)[colIndex]]] <- rowData[ , colIndex,drop=FALSE]
        }
    }
    
    assayData <- as.data.frame(assayData)
    rownames(assayData) <- NULL
    cat(as.character(Sys.time()))
    cbind(assayHeaders, assayData)
}

.getChunk <- function(connection) {
    size <- tryCatch(ReadVarint32(connection), error= function(e) NULL)
    if (is.null(size)) return(NULL)
    ReadRaw(connection, size = size)
}
