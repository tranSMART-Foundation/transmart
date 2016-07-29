library(jsonlite)
library(plyr)
library(reshape2)

main <- function() {
    save(loaded_variables, file="/Users/sascha/loaded_variables.Rda")
    save(fetch_params, file="/Users/sascha/fetch_params.Rda")

    df <- buildCrossfilterCompatibleDf(loaded_variables, fetch_params)
    checkTimeNameSanity(df)

    save(df, file="/Users/sascha/df.Rda")

    numeric.stats.df <- getStatsForNumericType(df)
    df <- merge(df, numeric.stats.df, by=c("bioMarker", "timeInteger", "subset"), all=TRUE)

    output <- list()
    output$data_matrix <- df

    json <- toJSON(output, pretty=TRUE, digits=I(17))
    write(json, file="linegraph.json")
    list(messages="Finished successfully")
}

# returns character vector (e.g. c("Age", "Alive" or "Week1", "Week2" if handling time series data))
getNodeNames <- function(loaded_variables, fetch_params) {
    names.without.subset <- sub("_s[1-2]{1}$", "", names(loaded_variables))
    labels <- sapply(names.without.subset, function(el) fetch_params$ontologyTerms[[el]]$name)
    as.character(as.vector(labels))
}

# returns character vector (e.g. c("\\Demo Study\\Vital Status\\Alive\\Week1", "\\Demo Study\\Vital Status\\Alive\\Week2", ...))
getFullNames <- function(loaded_variables, fetch_params) {
    names.without.subset <- sub("_s[1-2]{1}$", "", names(loaded_variables))
    fullNames <- sapply(names.without.subset, function(el) fetch_params$ontologyTerms[[el]]$fullName)
    as.character(as.vector(fullNames))
}

# returns integer vector (e.g. c(1,2,2,2,1,1,2))
getSubsets <- function(loaded_variables) {
    subsets <- sub("^.*_s", "", names(loaded_variables))
    as.integer(subsets)
}

# returns character vector (e.g. c("numeric", "numeric", "categoric", ...))
getTypes <- function(loaded_variables) {
    types <- sub("_.*$", "", names(loaded_variables))
    types[types == "highData"] <- "highDimensional"
    types[types == "numData"] <- "numeric"
    types[types == "catData"] <- "categoric"
    as.character(types)
}

# returns integer of extracted time if possible, NULL otherwise
extractTime <- function(string) {
    match <- regmatches(string, regexpr("\\d+", string)) 
    if (length(match) > 0) {
        return(as.numeric(match[1]))
    }
    NULL
}

# returns df that is compatible with crossfilter.js
buildCrossfilterCompatibleDf <- function(loaded_variables, fetch_params) {
    # gather information
    subsets <- getSubsets(loaded_variables)
    nodeNames <- getNodeNames(loaded_variables, fetch_params)
    fullNames <- getFullNames(loaded_variables, fetch_params)
    types <- getTypes(loaded_variables)

    # initialize empty df
    df <- data.frame(patientID=integer(),
                     value=c(), # can be string or integer
                     timeInteger=integer(),
                     timeString=character(),
                     bioMarker=character(),
                     type=character(),
                     subset=integer(),
                     stringsAsFactors=FALSE)


    # maps name to numeric time (e.g. "Week 12" to 12)
    times <- list()
    # build big df step by step via binding row-wise every loaded variable
    for (i in 1:length(names(loaded_variables))) {
        variable <- loaded_variables[[i]]
        variable.df <- data.frame()

        if (types[i] == "highDimensional") {
            colnames(variable)[-(1:2)] <- sub("^X", "", colnames(variable[-(1:2)]))
            variable.df <- melt(variable, id.vars=c("Row.Label", "Bio.marker"), variable.name="patientID")
        } else {
            variable.df <- data.frame(patientID=as.integer(variable[,1]),
                                      value=variable[,2])
        }

        split <- strsplit(fullNames[i], "\\\\")[[1]]
        bioMarker <- split[length(split) - 1]

        timeString <- nodeNames[i]
        timeInteger <- times[timeString][[1]]
        # if timeString never occured before, assign it a new timeInteger
        if (is.null(timeInteger)) {
            extractedTime <- extractTime(timeString)
            if (is.null(extractedTime)) {
                timeInteger <- length(names(times))
            } else {
                timeInteger <- extractedTime
            }
            times[timeString] <- timeInteger
        }

        # attach additional information
        variable.df <- cbind(variable.df,
                             timeInteger=rep(timeInteger, nrow(variable.df)),
                             timeString=rep(timeString, nrow(variable.df)),
                             bioMarker=rep(bioMarker, nrow(variable.df)),
                             type=rep(types[i], nrow(variable.df)),
                             subset=rep(subsets[i], nrow(variable.df)),
                             stringsAsFactors=FALSE)

        # no value -> no interest
        variable.df <- variable.df[variable.df$value != "" & !is.na(variable.df$value), ]
        if (nrow(variable.df) == 0) next
        # rbind.fill sets missing columns entries to NA
        df <- rbind.fill(df, variable.df)
    }

    # before we are done we assign a unique id to every row to make it easier for the front-end
    df <- cbind(id=1:nrow(df), df)

    df
}

# compute several statistics for the given data frame and return a statistic df
getStatsForNumericType <- function(df) {
    numeric.df <- df[df$type == 'numeric', ]
    timeIntegers <- unique(numeric.df$timeInteger)
    bioMarkers <- unique(numeric.df$bioMarker)
    subsets <- unique(numeric.df$subset)

    stats.df <- data.frame()
    for (subset in subsets) {
        for (bioMarker in bioMarkers) {
            for (timeInteger in timeIntegers) {
                current.df <- numeric.df[numeric.df$timeInteger == timeInteger &
                                         numeric.df$bioMarker == bioMarker &
                                         numeric.df$subset == subset, ]
                if (nrow(current.df) == 0) next
                values <- as.numeric(current.df$value)
                mean <- mean(values)
                median <- median(values)
                sd <- sd(values)
                sem <- sd / sqrt(length(values))
                stats.df <- rbind(stats.df, data.frame(bioMarker=bioMarker,
                                                       timeInteger=timeInteger,
                                                       subset=subset,
                                                       sd=sd,
                                                       sem=sem,
                                                       mean=mean,
                                                       median=median))
            }
        }
    }
    stats.df
}

# time (e.g. 15) and name (e.g. Day 15) must have a 1:1 relationship
# It is not possible to have multiple times for one name or multiple names for one time
checkTimeNameSanity <- function(df) {
    df.without.duplicates <- unique(df[, c("timeInteger", "timeString")])
    timeSane = nrow(df.without.duplicates) == length(unique(df.without.duplicates$timeInteger))
    nameSane = nrow(df.without.duplicates) == length(unique(df.without.duplicates$timeString))
    if (! (timeSane && nameSane)) {
        stop("Node names and assigned time values must have a 1:1 relationship.
             E.g. two nodes Age/Week1, Bloodpressure/Week1, must have both the same assigned time (e.g. 1)")
    }
}
