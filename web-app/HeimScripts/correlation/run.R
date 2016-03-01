library(reshape2)

main <- function(method = "pearson", patientIDs = integer()) {

    num_data <- parse.input(sourceLabel="datapoints", loaded_variables=loaded_variables, type="numeric")
    car_data <- parse.input(sourceLabel="annotations", loaded_variables=loaded_variables, type="categoric")

    num_data <- na.omit(num_data)
    colnames(num_data) <- c("patientID", "x", "y")

    if (length(patientIDs) > 0) {
        num_data <- num_data[num_data$patientID %in% patientIDs, ]
    }

    df <- num_data
    df$tag <- NA
    apply(df, 1, function(row) unname(row))

    unname(unlist(df[1,-1]))

    corTest <- tryCatch({
        cor.test(num_data[,2], num_data[,3], method=method)
    }, error = function(e) {
        ll <- list()
        ll$estimate <- as.numeric(NA)
        ll$p.value <- as.numeric(NA)
        ll
    })

    regLineSlope <- corTest$estimate * (sd(num_data[,3]) / sd(num_data[,2]))
    regLineYIntercept <- mean(num_data[,3]) - regLineSlope * mean(num_data[,2])

    output <- list(
        correlation = corTest$estimate,
        pvalue = corTest$p.value,
        regLineSlope = regLineSlope,
        regLineYIntercept = regLineYIntercept,
        xArrLabel = fetch_params$ontologyTerms$datapoints_n0$fullName,
        yArrLabel = fetch_params$ontologyTerms$datapoints_n1$fullName,
        method = method,
        patientIDs = num_data[,1],
        tags = c(),
        points = num_data
    )
    toJSON(output)
}

conceptStrToFolderStr <- function(s) {
    splitString <- strsplit(s, "")[[1]]
    backslashs <- which(splitString == "\\")
    substr(s, 0, tail(backslashs, 2)[1])
}