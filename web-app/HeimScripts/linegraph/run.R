library(jsonlite)
library(plyr)
library(reshape2)

main <- function() {
    save(loaded_variables, file="/Users/sascha/loaded_variables.Rda")
    save(fetch_params, file="/Users/sascha/fetch_params.Rda")

    df <- buildCrossfilterCompatibleDf(loaded_variables, fetch_params)
    checkTimeNameSanity(df)

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

        # attach additional information
        variable.df <- cbind(variable.df, timeInteger=sample(1:10, nrow(variable.df), replace=TRUE), # TODO: use real time value
                             timeString=rep(nodeNames[i], nrow(variable.df)),
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

    df
}

# time (e.g. 15) and name (e.g. Day 15) must have a 1:1 relationship
# It is not possible to have multiple times for one name or multiple names for one time
checkTimeNameSanity <- function(df) {
    # FIXME: disabled until I have real data
    return()
    df.without.duplicates <- unique(df[, c("timeInteger", "timeString")])
    timeSane = nrow(df.without.duplicates) == length(unique(df.without.duplicates$time))
    nameSane = nrow(df.without.duplicates) == length(unique(df.without.duplicates$name))
    if (! (timeSane && nameSane)) {
        stop("Node names and assigned time values must have a 1:1 relationship.
             E.g. two nodes Age/Week1, Bloodpressure/Week1, must have both the same assigned time (e.g. 1)")
    }
}
