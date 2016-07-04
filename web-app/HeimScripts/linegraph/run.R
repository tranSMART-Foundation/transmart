library(jsonlite)

main <- function() {
    save(loaded_variables, file="/Users/sascha/loaded_variables.Rda")
    save(fetch_params, file="/Users/sascha/fetch_params.Rda")

    df <- buildCrossfilterCompatibleDf(loaded_variables, fetch_params)
    # TODO: this is disabled as long as I don't have real time data to test
    # checkTimeNameSanity(df)

    output <- list()
    output$data_matrix <- df

    json <- toJSON(output, pretty=TRUE, digits=I(17))
    write(json, file="linegraph.json")
    list(messages="Finished successfully")
}

# returns character vector (e.g. c("Age", "Alive" or "Week1", "Week2" if handling time series data))
getNames <- function(loaded_variables, fetch_params) {
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
    names <- getNames(loaded_variables, fetch_params)
    fullNames <- getFullNames(loaded_variables, fetch_params)
    types <- getTypes(loaded_variables)


    df <- data.frame(patientID=integer(),
                     value=c(), # can be string or integer
                     time=integer(),
                     name=character(),
                     fullName=character(),
                     type=character(),
                     subset=integer(),
                     stringsAsFactors=FALSE)

    # build big df step by step via binding row-wise every loaded variable
    for (i in 1:length(names(loaded_variables))) {
        variable <- loaded_variables[[i]]
        # we are not interested in rows with value NA or ""
        variable <- variable[variable[,2] != "" & !is.na(variable[,2]), ]
        # if all values are NA or "" we don't include them in the df
        if (nrow(variable) == 0) next

        variable.df <- data.frame(patientID=as.integer(variable[,1]),
                                  value=variable[,2],
                                  time=sample(1:10, nrow(variable), replace=TRUE), # TODO: use real time values
                                  name=rep(names[i], nrow(variable)),
                                  fullName=rep(fullNames[i], nrow(variable)),
                                  type=rep(types[i], nrow(variable)),
                                  subset=rep(subsets[i], nrow(variable)),
                                  stringsAsFactors=FALSE)

        df <- rbind(df, variable.df)
    }

    df
}

# time (e.g. 15) and name (e.g. Day 15) must have a 1:1 relationship
# It is not possible to have multiple times for one name or multiple names for one time
checkTimeNameSanity <- function(df) {
    df.without.duplicates <- unique(df[, c("time", "name")])
    timeSane = nrow(df.without.duplicates) == length(unique(df.without.duplicates$time))
    nameSane = nrow(df.without.duplicates) == length(unique(df.without.duplicates$name))
    if (! (timeSane && nameSane)) {
        stop("Node names and assigned time values must have a 1:1 relationship.
             E.g. two nodes Age/Week1, Bloodpressure/Week1, must have both the same assigned time (e.g. 1)")
    }
}
