library(reshape2)


main <- function(method = 'pearson') {
    num_data <- parseNumericalData()
    num_data <- na.omit(num_data)
    colnames(num_data) <- c('patientID', 'x', 'y')

    cat_data <- parseCategoricalData()

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
        xArrLabel = 'abc',
        yArrLabel = 'efg',
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

parseNumericalData() {
    num_data1 <- loaded_variables[["sr-conceptBox-data_n0_s1"]]
    num_data2 <- loaded_variables[["sr-conceptBox-data_n1_s1"]]
    num_data <- merge(num_data1, num_data2, by="Row.Label")
    num_data
}

parseCategoricalData() {
    numTags <- length(loaded_variables) - 2
    cat_data <- ifelse(numTags, loaded_variables[["sr-conceptBox-annotations_n0_s1"]], NULL)
    for (i in 1:(numTags-1)) {
        cat_data <- merge(cat_data, loaded_variables[["sr-conceptBox-annotations_n" + i + "_s1"]], by="Row.Label")
    }
    cat_data
}