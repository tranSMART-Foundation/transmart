library(reshape2)

main <- function(method = "pearson", patientIDs = integer()) {

    num_data <- parseNumericalData()
    # cat_data <- parseCategoricalData()

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

parseNumericalData <- function() {
    num_data1 <- loaded_variables[["datapoints_n0_s1"]]
    num_data2 <- loaded_variables[["datapoints_n1_s1"]]
    num_data <- merge(num_data1, num_data2, by="Row.Label")
    num_data <- na.omit(num_data)
    colnames(num_data) <- c("patientID", "x", "y")
    num_data
}

parseCategoricalData <- function() {
    nTags <- length(loaded_variables) - 2
    cat_data <- ifelse(nTags, loaded_variables[["annotations_n0_s1"]], NULL)
    for (i in 1:(nTags-1)) {
        cat_data <- merge(cat_data, loaded_variables[[paste("annotations_n", i, "_s1", sep="")]], by="Row.Label")
    }

    cat_data[, apply(cat_data, 2, function(col) !all(col == ""))]

    cat_data
}