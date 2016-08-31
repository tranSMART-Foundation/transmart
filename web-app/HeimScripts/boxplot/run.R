library(jsonlite)
library(plyr)
library(reshape2)

main <- function(excludedPatientIDs = integer(), useLog=FALSE) {

    save(loaded_variables, file="~/loaded_variables.Rda")
    save(fetch_params, file="~/fetch_params.Rda")

    output <- list()
    output$useLog <- useLog

    df <- buildCrossfilterCompatibleDf(loaded_variables, fetch_params)
    if (useLog) {
        df$value <- log2(df$value)
    }
    df <- df[!is.infinite(df$value), ]

    output$dataMatrix <- df

    toJSON(output)
}

# returns df that is compatible with crossfilter.js
buildCrossfilterCompatibleDf <- function(loaded_variables, fetch_params) {
    # gather information
    subsets <- getSubsets(loaded_variables)
    fullNames <- getFullNames(loaded_variables, fetch_params)
    types <- getTypes(loaded_variables)

    # initialize empty df
    df <- data.frame(patientID=integer(),
                     value=integer(),
                     name=character(),
                     type=character(),
                     subset=integer(),
                     stringsAsFactors=FALSE)


    # build big df step by step via binding row-wise every loaded variable
    for (i in 1:length(names(loaded_variables))) {
        variable <- loaded_variables[[i]]
        variable.df <- data.frame()

        if (types[i] == "highDimensional") {
            colnames(variable)[-(1:2)] <- sub("^X", "", colnames(variable[-(1:2)]))
            variable.df <- melt(variable, id.vars=c("Row.Label", "Bio.marker"), variable.name="patientID")[, -c(1,2)]
        } else {
            variable.df <- data.frame(patientID=as.integer(variable[,1]),
                                      value=variable[,2])
        }

        # attach additional information
        variable.df <- cbind(variable.df,
                             name=rep(fullNames[i], nrow(variable.df)),
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
