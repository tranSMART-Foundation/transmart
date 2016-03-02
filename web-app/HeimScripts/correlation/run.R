library(reshape2)

main <- function(method = "pearson", selectedPatientIDs = integer()) {

    num_data <- parse.input(sourceLabel="datapoints", loaded_variables=loaded_variables, type="numeric")
    cat_data <- parse.input(sourceLabel="annotations", loaded_variables=loaded_variables, type="categoric")

    df <- num_data
    df <- na.omit(df)

    if (nrow(cat_data) > 0) {
        df <- merge(df, cat_data, by="patientID")
    } else {
        df$annotation <- ''
    }

    colnames(df) <- c("patientID", "x", "y", "annotation")

    if (length(selectedPatientIDs) > 0) {
        df <- df[df$patientID %in% selectedPatientIDs, ]
    }

    corTest <- tryCatch({
        cor.test(df$x, df$y, method=method)
    }, error = function(e) {
        ll <- list()
        ll$estimate <- as.numeric(NA)
        ll$p.value <- as.numeric(NA)
        ll
    })

    regLineSlope <- corTest$estimate * (sd(df$y) / sd(df$x))
    regLineYIntercept <- mean(df$y) - regLineSlope * mean(df$x)

    output <- list(
        correlation = corTest$estimate,
        pvalue = corTest$p.value,
        regLineSlope = regLineSlope,
        regLineYIntercept = regLineYIntercept,
        xArrLabel = fetch_params$ontologyTerms$datapoints_n0$fullName,
        yArrLabel = fetch_params$ontologyTerms$datapoints_n1$fullName,
        method = method,
        patientIDs = df$patientID,
        annotations = unique(df$annotation),
        points = df
    )
    toJSON(output)
}

conceptStrToFolderStr <- function(s) {
    splitString <- strsplit(s, "")[[1]]
    backslashs <- which(splitString == "\\")
    substr(s, 0, tail(backslashs, 2)[1])
}