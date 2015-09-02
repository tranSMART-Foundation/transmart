if (! suppressMessages(require(reshape2))) {
    stop("SmartR requires the R package 'jsonlite'")
}

### PREPARE SETTINGS ###

significanceMeassure <- settings$significanceMeassure
discardNullGenes <- strtoi(settings$discardNullGenes)
maxRows <- ifelse(is.null(settings$maxRows), 100, as.integer(settings$maxRows))

### COMPUTE RESULTS ###

highDimData <- highDimData_cohort1
highDimData <- melt(highDimData, id=c('PATIENTID', 'PROBE', 'GENEID', 'GENESYMBOL'))
highDimData <- data.frame(dcast(highDimData, PROBE + GENEID + GENESYMBOL ~ PATIENTID), stringsAsFactors=FALSE)

highDimData <- na.omit(highDimData)

if (discardNullGenes) {
    highDimData <- highDimData[highDimData$GENESYMBOL != '', ]
}

valueMatrix <- highDimData[, -(1:3)]
zScoreMatrix <- t(apply(valueMatrix, 1, scale))

if (significanceMeassure == 'variance') {
    significanceValues <- apply(valueMatrix, 1, var)
} else if (significanceMeassure == 'zScoreRange') {
    significanceValues <- apply(zScoreMatrix, 1, function(zScores) { 
            bxp <- boxplot.stats(zScores)
            zScores.withoutOutliers <- zScores[zScores >= bxp$stats[2] & zScores <= bxp$stats[4]]
            max(zScores.withoutOutliers) - min(zScores.withoutOutliers)
        })
} else {
    stop('Unknown significance measure!')
} 

highDimData$SIGNIFICANCE <- significanceValues
highDimData.value <- highDimData
highDimData.zScore <- cbind(highDimData[, 1:3], zScoreMatrix)

highDimData.value <- highDimData.value[order(significanceValues, decreasing=TRUE), ]
highDimData.zScore <- highDimData.zScore[order(significanceValues, decreasing=TRUE), ]

highDimData.value <- highDimData.value[1:maxRows, ]
highDimData.zScore <- highDimData.zScore[1:maxRows, ]

significanceValues <- highDimData.value$SIGNIFICANCE

probes <- highDimData.value$PROBE
geneIDs <- highDimData.value$GENEID
geneSymbols <- highDimData.value$GENESYMBOL


fields.value <- melt(highDimData.value, id=c('PROBE', 'GENEID', 'GENESYMBOL', 'SIGNIFICANCE'))
fields.zScore <- melt(highDimData.zScore, id=c('PROBE', 'GENEID', 'GENESYMBOL'))

fields <- fields.value
fields <- cbind(fields, fields.zScore[, 5])

names(fields) <- c('PROBE', 'GENEID', 'GENESYMBOL', 'SIGNIFICANCE', 'PATIENTID', 'VALUE', 'ZSCORE')
fields$PATIENTID <- as.numeric(sub("^X", "", levels(fields$PATIENTID)))[fields$PATIENTID]
fields <- fields[order(fields$PROBE, fields$PATIENTID, decreasing=FALSE), ]
fields <- fields[order(fields$SIGNIFICANCE, decreasing=TRUE), ]

patientIDs <- unique(fields$PATIENTID)

sorted.zScoreMatrix <- highDimData.zScore[, -(1:3)]
colDendrogramEuclideanComplete <- as.dendrogram(hclust(dist(t(sorted.zScoreMatrix), method='euclidean'), method='complete'))
colDendrogramEuclideanSingle <- as.dendrogram(hclust(dist(t(sorted.zScoreMatrix), method='euclidean'), method='single'))
colDendrogramEuclideanAverage <- as.dendrogram(hclust(dist(t(sorted.zScoreMatrix), method='euclidean'), method='average'))
rowDendrogramEuclideanComplete <- as.dendrogram(hclust(dist(sorted.zScoreMatrix, method='euclidean'), method='complete'))
rowDendrogramEuclideanSingle <- as.dendrogram(hclust(dist(sorted.zScoreMatrix, method='euclidean'), method='single'))
rowDendrogramEuclideanAverage <- as.dendrogram(hclust(dist(sorted.zScoreMatrix, method='euclidean'), method='average'))

colDendrogramManhattanComplete <- as.dendrogram(hclust(dist(t(sorted.zScoreMatrix), method='manhattan'), method='complete'))
colDendrogramManhattanSingle <- as.dendrogram(hclust(dist(t(sorted.zScoreMatrix), method='manhattan'), method='single'))
colDendrogramManhattanAverage <- as.dendrogram(hclust(dist(t(sorted.zScoreMatrix), method='manhattan'), method='average'))
rowDendrogramManhattanComplete <- as.dendrogram(hclust(dist(sorted.zScoreMatrix, method='manhattan'), method='complete'))
rowDendrogramManhattanSingle <- as.dendrogram(hclust(dist(sorted.zScoreMatrix, method='manhattan'), method='single'))
rowDendrogramManhattanAverage <- as.dendrogram(hclust(dist(sorted.zScoreMatrix, method='manhattan'), method='average'))

dendrogramToJSON <- function(d) {
    totalMembers <- attributes(d)$members
    add_json <- function(x, start, left) {
        members <- attributes(x)$members
        height <- attributes(x)$height
        index <- (start - 1):(start + members - 2)
        index <- paste(index, collapse=' ')
        jsonString <<- paste(jsonString, sprintf('{"height":"%s", "index":"%s", "children":[', height, index))
        if (is.leaf(x)){
            jsonString <<- paste(jsonString, ']}')
        } else {
            add_json(x[[1]], start, TRUE)
            jsonString <<- paste(jsonString, ",")
            leftMembers <- attributes(x[[1]])$members
            add_json(x[[2]], start + leftMembers, FALSE)
            jsonString <<- paste(jsonString, "]}")
        }
    }
    jsonString <- ""
    add_json(d, TRUE)
    return(jsonString)
}

# FIXME make sure to check for missing fields

numerical.lowDimData <- lowDimData$additionalFeatures_numerical
concepts <- unique(numerical.lowDimData$concept)
extraFields <- c()
features <- c()
for (concept in concepts) {
    featureName <- tail(strsplit(concept, '\\\\')[[1]], n=1)
    featureName <- gsub(' |\\(|\\)', '_', featureName)
    conceptData <- numerical.lowDimData[numerical.lowDimData$concept == concept, ]
    binary = length(unique(conceptData$value)) == 2
    newFields <- data.frame(
        FEATURE=rep(featureName, nrow(conceptData)),
        PATIENTID=conceptData$patientID,
        TYPE=rep(ifelse(binary, 'binary', 'numerical'), nrow(conceptData)),
        VALUE=conceptData$value,
        ZSCORE=scale(conceptData$value))
    newFields <- newFields[newFields$PATIENTID %in% patientIDs, ]
    newFields <- newFields[order(newFields$PATIENTID, decreasing=FALSE), ]
    extraFields <- rbind(extraFields, newFields)
    features <- c(features, featureName)
}

conceptStrToFolderStr <- function(s) {
    splitString <- strsplit(s, "")[[1]]
    backslashs <- which(splitString == "\\")
    substr(s, 0, tail(backslashs, 2)[1])
}

alphabetical.lowDimData <- lowDimData$additionalFeatures_alphabetical
folders <- as.vector(sapply(alphabetical.lowDimData$concept, conceptStrToFolderStr))
unique.folders <- unique(folders)
for (folder in unique.folders) {
    folderData <- alphabetical.lowDimData[folder == folders, ]
    featureName <- tail(strsplit(folder, '\\\\')[[1]], n=1)
    featureName <- gsub(' |\\(|\\)', '_', featureName)
    binary = length(unique(conceptData$value)) == 2
    newFields <- data.frame(
        FEATURE=rep(featureName, nrow(folderData)),
        PATIENTID=folderData$patientID,
        TYPE=rep('alphabetical', nrow(folderData)),
        VALUE=folderData$value,
        ZSCORE=rep(NA, nrow(folderData)))
    newFields <- newFields[newFields$PATIENTID %in% patientIDs, ]
    newFields <- newFields[order(newFields$PATIENTID, decreasing=FALSE), ]
    extraFields <- rbind(extraFields, newFields)
    features <- c(features, featureName)
}

fields$PROBE <- gsub("[[:space:]]", "_", fields$PROBE)
probes <- gsub("[[:space:]]", "_", probes)

### WRITE OUTPUT ###
output$extraFields <- extraFields
output$features <- features
output$fields <- fields
output$significanceValues <- significanceValues
output$patientIDs <- patientIDs
output$probes <- probes
output$geneIDs <- geneIDs
output$geneSymbols <- geneSymbols

output$hclustEuclideanComplete <- list(
    order.dendrogram(colDendrogramEuclideanComplete) - 1,
    order.dendrogram(rowDendrogramEuclideanComplete) - 1,
    dendrogramToJSON(colDendrogramEuclideanComplete),
    dendrogramToJSON(rowDendrogramEuclideanComplete))

output$hclustEuclideanSingle <- list(
    order.dendrogram(colDendrogramEuclideanSingle) - 1,
    order.dendrogram(rowDendrogramEuclideanSingle) - 1,
    dendrogramToJSON(colDendrogramEuclideanSingle),
    dendrogramToJSON(rowDendrogramEuclideanSingle))

output$hclustEuclideanAverage <- list(
    order.dendrogram(colDendrogramEuclideanAverage) - 1,
    order.dendrogram(rowDendrogramEuclideanAverage) - 1,
    dendrogramToJSON(colDendrogramEuclideanAverage),
    dendrogramToJSON(rowDendrogramEuclideanAverage))

output$hclustManhattanComplete <- list(
    order.dendrogram(colDendrogramManhattanComplete) - 1,
    order.dendrogram(rowDendrogramManhattanComplete) - 1,
    dendrogramToJSON(colDendrogramManhattanComplete),
    dendrogramToJSON(rowDendrogramManhattanComplete))

output$hclustManhattanSingle <- list(
    order.dendrogram(colDendrogramManhattanSingle) - 1,
    order.dendrogram(rowDendrogramManhattanSingle) - 1,
    dendrogramToJSON(colDendrogramManhattanSingle),
    dendrogramToJSON(rowDendrogramManhattanSingle))

output$hclustManhattanAverage <- list(
    order.dendrogram(colDendrogramManhattanAverage) - 1,
    order.dendrogram(rowDendrogramManhattanAverage) - 1,
    dendrogramToJSON(colDendrogramManhattanAverage),
    dendrogramToJSON(rowDendrogramManhattanAverage))
