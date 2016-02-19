
if (!exists("remoteScriptDir")) {  #  Needed for unit-tests
    remoteScriptDir <- "web-app/HeimScripts"
}

inputUtils <- paste(remoteScriptDir, "/core/input.R", sep="")
source(inputUtils)

main <- function( excludedPatientIDs = NULL ) {
    datapoints <- parse.input(sourceLabel="datapoints", loaded_variables=loaded_variables, type="numeric")
    datapoints <- datapoints[datapoints[,2] != "NA", ]
    colnames(datapoints)[2] <- 'value'

    subsets <- parse.input(sourceLabel="subsets", loaded_variables=loaded_variables, type="categoric")
    df <- merge(datapoints, subsets, by="patientID")

    df$category[df$category == ""] <- "no subset"
    df$jitter <- runif(nrow(df), -0.5, 0.5)

    if (! is.null(excludedPatientIDs)) {
        df <- df[, ! df$patientID %in% excludedPatientIDs]
    }
    patientIDs <- df$patientID

    output <- list()
    output$concept <- fetch_params$ontologyTerms$datapoints_n0$fullName
    output$globalMin <- min(df[,2])
    output$globalMax <- max(df[,2])
    output$categories <- unique(df$category)

    for (cat in unique(df$category)) {
        subset <- df[df$category == cat,]
        bxp <- boxplot(subset[,2], plot=FALSE)
        output[[cat]] <- list()
        output[[cat]]$lowerWhisker <- bxp$stats[1]
        output[[cat]]$lowerHinge <- bxp$stats[2]
        output[[cat]]$median <- bxp$stats[3]
        output[[cat]]$upperHinge <- bxp$stats[4]
        output[[cat]]$upperWhisker <- bxp$stats[5]
        outlier <- subset[,2] > output[[cat]]$upperWhisker | subset[,2] < output[[cat]]$lowerWhisker
        subset$outlier <- outlier
        output[[cat]]$points <- subset
    }

    toJSON(output)
}
