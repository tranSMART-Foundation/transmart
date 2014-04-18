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

    serverResult <- .transmartServerGetRequest(paste(concept.link, "/highdim", sep=""), accept.type = "hal")
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
        cat(paste(listOfHighdimDataTypes$supportedProjections, "\n"))
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

.parseHighdimData <- function(rawVector, .fast.data.frame=TRUE) {
    dataChopper <- .messageChopper(rawVector)

    message <- dataChopper$getNextMessage()
    header <- read(highdim.HighDimHeader, message)
    columnSpec <- header$columnSpec
    assays <- header$assay
    DOUBLE <- highdim.ColumnSpec$ColumnType$DOUBLE
    STRING <- highdim.ColumnSpec$ColumnType$STRING

    columns <- .expandingList(1000)

    noAssays <- length(assays)

    assayLabels <- .DollarNames(assays[[1]])[1:length(assays[[1]])]

    for (label in assayLabels) {
        if (label == "assayId") {
            columns$add(label, sapply(assays,
                    function(a) sub("assayId: ", "", grep(label, strsplit(as.character(a), split = "\n")[[1]], value = TRUE))))
        } else {
            columns$add(label, sapply(assays, function(a) a[[label]]))
        }
    }
    
    cat(paste("Received data for", noAssays, "assays. Unpacking data.", as.character(Sys.time()), "\n"))

    pb <- txtProgressBar(min = 0, max = length(rawVector), style = 3)
    labelToBioMarker <- hash() #biomarker info is optional, but should not be omitted, as it is also part of the data

    while (!is.null(message <- dataChopper$getNextMessage())) {
        row <- read(highdim.Row, message)
        rowlabel <- row$label
        
        labelToBioMarker[[rowlabel]] <- (if(is.null(row$bioMarker)) NA_character_ else row$bioMarker)
        rowValues <- row$value
        
        setTxtProgressBar(pb, dataChopper$getRawVectorIndex())
        
        if(length(rowValues) == 1) {
            # if only one value, don't paste the columnSpec name.
            columns$add(rowlabel, rowValues[[1]]$doubleValue)
            next
        }

        for(i in 1:length(rowValues)) {
            entryName <- paste(rowlabel, columnSpec[[i]]$name, sep=".")
            if(columnSpec[[i]]$type == STRING) {
                columns$add(entryName, rowValues[[i]]$stringValue) #add the values of one column value. The name of this vector is the rowlabel concatenated to the column specification
            } else if(columnSpec[[i]]$type == DOUBLE) {
                columns$add(entryName, rowValues[[i]]$doubleValue) #add the values of one column value. The name of this vector is the rowlabel concatenated to the column specification
            }
        }
        
    }
    close(pb) 

    cat("Data unpacked. Converting to data.frame.", as.character(Sys.time()),"\n")
    
    if(.fast.data.frame) {
        data <- .as.data.frame.fast(columns$as.list(), length(assayLabels))
    } else {
        data <- as.data.frame(columns$as.list(), stringsAsFactors=FALSE)
    }

    if(all(is.na(values(labelToBioMarker)))) {
        cat("No biomarker information available.")
        labelToBioMarker <- "No biomarker information is available for this dataset"
        return(list(data = data))
    } else {
        cat("Additional biomarker information is available.\nThis function will return a list containing a dataframe containing the high dimensional data and a hash describing which (column) labels refer to which bioMarker\n")
        return(list(data = data, labelToBioMarkerMap = labelToBioMarker))
    }
}

.messageChopper <- function(rawVector, endOfLastMessage = 0) {
    msbSetToOne <- as.raw(128)
    progressTotal <- length(rawVector)
    pb <- c()

    getNextMessage <- function() {
        if (endOfLastMessage >= length(rawVector)) { return(NULL) }
        varint32Size <- min(which((msbSetToOne & rawVector[(endOfLastMessage+1):(endOfLastMessage+5)]) == as.raw(0)))
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

    getRawVectorIndex <- function() { return(endOfLastMessage) }

    return(list(getNextMessage = getNextMessage, getRawVectorIndex = getRawVectorIndex))
}


# We need to repeatedly add an element to a list. With normal list concatenation
# or element setting this would lead to a quadratic number of memory copies and 
# thus a quadratic runtime. To prevent that, this function implements a bare
# bones expanding array, in which list additions are (amortized) constant time.
.expandingList <- function(capacity = 10) {
    buffer <- vector('list', capacity)
    names <- character(capacity)
    count <- 0

    methods <- list()

    methods$double.size <- function() {
        buffer <<- c(buffer, vector('list', capacity))
        names <<- c(names, character(capacity))
        capacity <<- capacity * 2
    }

    methods$add <- function(name, val) {
        if(count == capacity) {
            methods$double.size()
        }

        count <<- count + 1
        buffer[[count]] <<- val
        names[count] <<- name
    }

    methods$as.list <- function() {
        b <- buffer[0:count]
        names(b) <- names[0:count]
        return(b)
    }

    methods
}


.as.data.frame.fast <- function(data, assayLabel.length) {
    # Add Xes to column names that start with numbers or other strange characters
    colnames <- names(data)
    rowDataIndexes <- -grep('^([a-zA-Z]|\\.[^0-9])', colnames)
    colnames[rowDataIndexes] <- paste('X', colnames[rowDataIndexes], sep='')
    
    names(data) <- colnames
    attr(data, 'row.names') <- 1:length(data[[1]])
    class(data) <- 'data.frame'
    
    data
}
