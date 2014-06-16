# Copyright 2014 Janssen Research & Development, LLC.
#
# This file is part of tranSMART R Client: R package allowing access to
# tranSMART's data via its RESTful API.
#
# This program is free software: you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation, either version 3 of the License, or (at your
# option) any later version, along with the following terms:
#
#   1. You may convey a work based on this program in accordance with
#      section 5, provided that you retain the above notices.
#   2. You may convey verbatim copies of this program code as you receive
#      it, in any medium, provided that you retain the above notices.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
# Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program. If not, see <http://www.gnu.org/licenses/>.


# Performance notes
# 
# Downloading and parsing large data sets of high dimensional data can take a 
# significant amount of time (minutes for several 100 mb). We have attempted to 
# optimize the process a reasonable amount.
# 
# The current RCurl wrapper doesn't expose functionality to download a binary 
# url and process the chunks asynchronously as they come in (that is only 
# supported for text urls). Doing the downloading and parsing at the same time 
# should give a significant improvement, but that would require changes in RCurl
# or a different way of downloading the data.
# 
# The parser has also been optimized up to the level that the R code itself only
# takes a minority of the runtime. The most time consuming operations are the 
# foreign function calls to retrieve the fields from messages and to construct 
# objects to parse the varint32 preceding each message. Significant further 
# optimization of the parser would probably require dropping to C or another 
# lower level language.


getHighdimData <- function(study.name, concept.match = NULL, concept.link = NULL, projection = NULL,
                           progress.download = .make.progresscallback.download(),
                           progress.parse = .make.progresscallback.parse()
                           ) {
    
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
        stop("No valid projection selected.\nSet the projection argument to one of the following options:\n",
             paste(listOfHighdimDataTypes$supportedProjections, "\n"))
        return(NULL)
    }
    message("Retrieving data from server. This can take some time, depending on your network connection speed. ", as.character(Sys.time()))
    serverResult <- .transmartServerGetRequest(projectionLink, accept.type = "binary", progress = progress.download)
    if (length(serverResult$content) == 0) {
        warning("Error in retrieving high dim data.")
        return(NULL)
    }

    return(.parseHighdimData(serverResult$content, progress = progress.parse))
}

.parseHighdimData <- function(rawVector, .to.data.frame.converter=.as.data.frame.fast, progress=.make.progresscallback.parse()) {
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
        
        # RProtoBuf does not support int64 on all platforms that we need to 
        # support. Specifically, binaries from CRAN don't support it, which 
        # means Windows platforms and the default OSX R distribution. Linux and 
        # OSX with Homebrew download source packages from CRAN and compile 
        # locally, so they don't have problems with int64.
        #
        # The problem actually is in Rcpp. CRAN does not allow use of 'long 
        # long' types as it is considered non portable. Rcpp works around this 
        # by conditionally including 64 bit int support if it is compiled 
        # somewhere other than CRAN.
        #
        # The serialization format uses an int64 field for 
        # highdim.Assay.assayId. Reading that field on platforms that do not 
        # have int64 support results in an error. As a workaround we parse the 
        # assayId field from the string representation of the assay. (This works
        # because the as.character() conversion calls a DebugString method in 
        # the C++ protobuf library, so the RProtoBuf C++ code that CRAN sees 
        # never touches the 64 bit integers directly.)
        
        if (label == "assayId") {
            columns$add(label, sapply(assays,
                    function(a) sub("assayId: ", "", grep(label, strsplit(as.character(a), split = "\n")[[1]], value = TRUE))))
        } else {
            columns$add(label, sapply(assays, function(a) a[[label]]))
        }
    }
    
    message("Received data for ", noAssays, " assays. Unpacking data. ", as.character(Sys.time()))

    totalsize <- length(rawVector)
    progress$start(totalsize)
    callback <- progress$update
    
    labelToBioMarker <- hash() #biomarker info is optional, but should not be omitted, as it is also part of the data

    while (!is.null(message <- dataChopper$getNextMessage())) {
        row <- read(highdim.Row, message)
        rowlabel <- row$label
        
        labelToBioMarker[[rowlabel]] <- (if(is.null(row$bioMarker)) NA_character_ else row$bioMarker)
        rowValues <- row$value
        
        callback(dataChopper$getRawVectorIndex(), totalsize)
        
        if(length(rowValues) == 1) {
            # if only one value, don't add the columnSpec name to the rowlabel.
            columns$add(rowlabel, rowValues[[1]]$doubleValue)
            next
        }

        # Multiple columns, add the columnSpec name to the labels to differentiate them.
        for(i in 1:length(rowValues)) {
            entryName <- paste(rowlabel, columnSpec[[i]]$name, sep=".")
            type <- columnSpec[[i]]$type
            if(type == STRING) {
                columns$add(entryName, rowValues[[i]]$stringValue)
            } else if(type == DOUBLE) {
                columns$add(entryName, rowValues[[i]]$doubleValue)
            } else {
                warning("Unknown row type: ", type)
            }
        }
        
    }
    progress$end()
    
    message("Data unpacked. Converting to data.frame. ", as.character(Sys.time()))
    
    data <- .to.data.frame.converter(columns$as.list(), stringsAsFactors=FALSE)

    if(all(is.na(values(labelToBioMarker)))) {
        message("No biomarker information available.")
        labelToBioMarker <- "No biomarker information is available for this dataset"
        return(list(data = data))
    } else {
        message("Additional biomarker information is available.\nThis function will return a list containing a dataframe ",
                "containing the high dimensional data and a hash describing which (column) labels refer to which bioMarker")
        return(list(data = data, labelToBioMarkerMap = labelToBioMarker))
    }
}


.make.progresscallback.parse <- function() {
    pb <- NULL
    lst <- list()
    lst$start <- function(total) {
        pb <<- txtProgressBar(min = 0, max = total, style = 3)
    }
    lst$update <- function(current, .total) {
        setTxtProgressBar(pb, current)
    }
    lst$end <- function() {
        close(pb)
    }

    lst
}

.messageChopper <- function(rawVector, endOfLastMessage = 0) {
    msbSetToOne <- as.raw(128)
    progressTotal <- length(rawVector)
    pb <- c()

    getNextMessage <- function() {
        # The protobuf messages are written using writeDelimited in the Java
        # protobuf library. Unfortunately the C++ and R versions don't support
        # that function natively. We manually read a varint32 from the
        # connection that indicates the size of the next message.

        if (endOfLastMessage >= length(rawVector)) { return(NULL) }
        # The last byte of the varint32 has its most significant bit set to one.
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
# or element setting this would lead to a large number of memory copies and a
# quadratic runtime. To prevent that, this function implements a bare bones
# expanding array, in which list appends are (amortized) constant time.
.expandingList <- function(capacity = 10) {
    buffer <- vector('list', capacity)
    names <- character(capacity)
    length <- 0

    methods <- list()

    methods$double.size <- function() {
        buffer <<- c(buffer, vector('list', capacity))
        names <<- c(names, character(capacity))
        capacity <<- capacity * 2
    }

    methods$add <- function(name, val) {
        if(length == capacity) {
            methods$double.size()
        }

        length <<- length + 1
        buffer[[length]] <<- val
        names[length] <<- name
    }

    methods$as.list <- function() {
        b <- buffer[0:length]
        names(b) <- names[0:length]
        return(b)
    }

    methods
}


.as.data.frame.fast <- function(data, ...) {
    # Add X'es to column names that start with numbers or other strange characters
    colnames <- names(data)
    rowDataIndexes <- -grep('^([a-zA-Z]|\\.[^0-9])', colnames)
    colnames[rowDataIndexes] <- paste('X', colnames[rowDataIndexes], sep='')
    
    names(data) <- colnames
    attr(data, 'row.names') <- 1:length(data[[1]])
    class(data) <- 'data.frame'
    
    data
}
