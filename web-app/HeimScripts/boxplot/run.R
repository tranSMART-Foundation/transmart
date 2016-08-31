
main <- function(excludedPatientIDs = integer(), useLog=FALSE) {

    save(loaded_variables, file="~/loaded_variables.Rda")
    save(fetch_params, file="~/fetch_params.Rda")

    output <- list()
    output$useLog <- useLog
    output$concept <- fetch_params$ontologyTerms$datapoints_n0$fullName
    output$excludedPatientIDs <- excludedPatientIDs

    df1 <- loaded_variables$datapoints_n0_s1
    df1 <- prepareData(df1, excludedPatientIDs, useLog, 1)
    output$dataMatrix <- df1

    if(!is.null(loaded_variables$datapoints_n0_s2)) {
        df2 <- loaded_variables$datapoints_n0_s2
        df2 <- prepareData(df2, excludedPatientIDs, useLog, 2)
        output$dataMatrix <- rbind(output$dataMatrix, df2)
    }

    toJSON(output)
}

prepareData <- function(df, excludedPatientIDs, useLog, subset) {
    df <- na.omit(df)
    if (nrow(df) == 0) {
        stop(paste("Too few data points for subset ", subset), sep="")
    }
    colnames(df) <- c("patientID", "value")
    df$subset <- subset
    if (useLog) {
        df$value <- log2(df$value)
    }
    df <- df[!is.infinite(df$value), ]
    if (nrow(df) == 0) {
        stop(paste("Too few data points for subset ", subset), sep="")
    }
    df
}
