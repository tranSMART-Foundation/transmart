if (! suppressMessages(require(reshape2))) {
    stop("SmartR requires the R package 'reshape2'")
}

### PREPARE SETTINGS ###

significanceMeassure <- settings$significanceMeassure
discardNullGenes <- strtoi(settings$discardNullGenes)
maxRows <- ifelse(is.null(settings$maxRows), 100, as.integer(settings$maxRows))

### COMPUTE RESULTS ###

getHDDMatrix <- function(raw.data) {
    HDD.matrix <- data.frame(
        PATIENTID=raw.data$PATIENTID,
        PROBE=raw.data$PROBE,
        GENESYMBOL=raw.data$GENESYMBOL,
        VALUE=raw.data$VALUE)
    HDD.matrix <- melt(HDD.matrix, id=c('PATIENTID', 'PROBE', 'GENESYMBOL'), na.rm=TRUE)
    HDD.matrix <- data.frame(dcast(HDD.matrix, PROBE + GENESYMBOL ~ PATIENTID), stringsAsFactors=FALSE)
    if (discardNullGenes) {
        HDD.matrix <- HDD.matrix[HDD.matrix$GENESYMBOL != '', ]
    }
    HDD.matrix <- na.omit(HDD.matrix)
    HDD.matrix <- HDD.matrix[order(HDD.matrix$PROBE), ]
    HDD.matrix
}

extractMatrixValues <- function(HDD.matrix) {
    valueMatrix <- HDD.matrix[, -(1:2)]
    valueMatrix
}

getZScoreMatrix <- function(valueMatrix) {
    colNames <- colnames(valueMatrix)
    zScoreMatrix <- t(apply(valueMatrix, 1, scale))
    colnames(zScoreMatrix) <- colNames
    zScoreMatrix
}

getSignificanceValues <- function(valueMatrix, zScoreMatrix, colNum) {
    if (significanceMeassure == 'variance') {
        significanceValues <- apply(valueMatrix, 1, var)
    } else if (significanceMeassure == 'zScoreRange') {
        significanceValues <- apply(zScoreMatrix, 1, function(zScores) { 
                bxp <- boxplot.stats(zScores)
                zScores.withoutOutliers <- zScores[zScores >= bxp$stats[2] & zScores <= bxp$stats[4]]
                max(zScores.withoutOutliers) - min(zScores.withoutOutliers)
        })
    } else {
        if (! suppressMessages(require(limma))) {
            stop("SmartR requires the R package 'limma'")
        }
        classVectorS1 <- c(rep(1, colNum), rep(2, ncol(valueMatrix) - colNum))
        classVectorS2 <- rev(classVectorS1)
        design <- cbind(S1=classVectorS1, S2=classVectorS2)
        contrast.matrix = makeContrasts(S1-S2, levels=design)
        fit <- lmFit(valueMatrix, design)
        fit <- contrasts.fit(fit, contrast.matrix)
        fit <- eBayes(fit)
        contr = 1
        top.fit = data.frame(
                ID=rownames(fit$coefficients),
                logFC=fit$coefficients[, contr],
                t=fit$t[, contr],
                P.Value=fit$p.value[, contr],
                adj.P.val=p.adjust(p=fit$p.value[, contr], method='BH'),
                B=fit$lods[, contr]
        )
        significanceValues <- top.fit[[significanceMeassure]]
    }
    significanceValues
}

sortAndCutHDDMatrix <- function(HDD.matrix, significanceValues) {
    HDD.matrix$SIGNIFICANCE <- significanceValues
    HDD.matrix <- HDD.matrix[order(significanceValues, decreasing=TRUE), ]
    HDD.matrix <- HDD.matrix[1:maxRows, ]
    HDD.matrix
}

fixString <- function(str) {
    str <- gsub("[[:punct:]]", "_", str)
    str <- gsub(" ", "_", str)
    str
}

buildFields <- function(HDD.value.matrix, HDD.zscore.matrix) {
    fields.value <- melt(HDD.value.matrix, id=c('PROBE', 'GENESYMBOL', 'SIGNIFICANCE'))
    fields.zScore <- melt(HDD.zscore.matrix, id=c('PROBE', 'GENESYMBOL', 'SIGNIFICANCE'))
    
    fields <- fields.value
    fields <- cbind(fields, fields.zScore$value)
    
    names(fields) <- c('PROBE', 'GENESYMBOL', 'SIGNIFICANCE', 'PATIENTID', 'VALUE', 'ZSCORE')
    fields$PATIENTID <- as.numeric(sub("^X", "", levels(fields$PATIENTID)))[fields$PATIENTID]
    fields <- fields[order(fields$PROBE, fields$PATIENTID, decreasing=FALSE), ]
    fields <- fields[order(fields$SIGNIFICANCE, decreasing=TRUE), ]

    fields$PROBE <- fixString(fields$PROBE)

    fields
}

computeDendrogram <- function(zScoreMatrix, distanceMeassure, linkageMethod) {
    as.dendrogram(hclust(dist(zScoreMatrix, method=distanceMeassure), method=linkageMethod))
}

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

if (length(data.cohort1$mRNAData$PATIENTID) == 0) {
    stop('Your selection does not match any patient in the defined cohort!')
}

HDD.value.matrix.cohort1 <- getHDDMatrix(data.cohort1$mRNAData)
patientIDs.cohort1 <- as.numeric(sub("^X", "", colnames(HDD.value.matrix.cohort1))[-(1:2)])
colNum <- ncol(HDD.value.matrix.cohort1) - 2
if (length(data.cohort2$mRNAData) > 0) {
    HDD.value.matrix.cohort2 <- getHDDMatrix(data.cohort2$mRNAData)
    patientIDs.cohort2 <- as.numeric(sub("^X", "", colnames(HDD.value.matrix.cohort2))[-(1:2)])

    HDD.value.matrix.cohort1 <- HDD.value.matrix.cohort1[HDD.value.matrix.cohort1$PROBE %in% HDD.value.matrix.cohort2$PROBE, ]
    HDD.value.matrix.cohort2 <- HDD.value.matrix.cohort2[HDD.value.matrix.cohort2$PROBE %in% HDD.value.matrix.cohort1$PROBE, ]

    valueMatrix.cohort1 <- extractMatrixValues(HDD.value.matrix.cohort1)
    valueMatrix.cohort2 <- extractMatrixValues(HDD.value.matrix.cohort2)

    valueMatrix <- cbind(valueMatrix.cohort1, valueMatrix.cohort2)
    HDD.value.matrix <- cbind(HDD.value.matrix.cohort1[, 1:2], valueMatrix)
} else {
    valueMatrix <- extractMatrixValues(HDD.value.matrix.cohort1)
    HDD.value.matrix <- HDD.value.matrix.cohort1
}

patientIDs <- as.numeric(sub("^X", "", colnames(valueMatrix)))
zScoreMatrix <- getZScoreMatrix(valueMatrix)
HDD.zScore.matrix <- cbind(HDD.value.matrix[, 1:2], zScoreMatrix)

significanceValues <- getSignificanceValues(valueMatrix, zScoreMatrix, colNum)
FINAL.HDD.value.matrix <- sortAndCutHDDMatrix(HDD.value.matrix, significanceValues)
FINAL.HDD.zScore.matrix <- sortAndCutHDDMatrix(HDD.zScore.matrix, significanceValues)

significanceValues.sorted <- FINAL.HDD.value.matrix$SIGNIFICANCE
zScoreMatrix.sorted <- FINAL.HDD.zScore.matrix[, -c(1:2, ncol(FINAL.HDD.zScore.matrix))]

fields <- buildFields(FINAL.HDD.value.matrix, FINAL.HDD.zScore.matrix)
probes <- fixString(FINAL.HDD.value.matrix$PROBE)
geneSymbols <- FINAL.HDD.value.matrix$GENESYMBOL

colDendrogramEuclideanComplete <- computeDendrogram(t(zScoreMatrix.sorted), 'euclidean', 'complete')
colDendrogramEuclideanSingle <- computeDendrogram(t(zScoreMatrix.sorted), 'euclidean', 'single')
colDendrogramEuclideanAverage <- computeDendrogram(t(zScoreMatrix.sorted), 'euclidean', 'average')
rowDendrogramEuclideanComplete <- computeDendrogram(zScoreMatrix.sorted, 'euclidean', 'complete')
rowDendrogramEuclideanSingle <- computeDendrogram(zScoreMatrix.sorted, 'euclidean', 'single')
rowDendrogramEuclideanAverage <- computeDendrogram(zScoreMatrix.sorted, 'euclidean', 'average')

colDendrogramManhattanComplete <- computeDendrogram(t(zScoreMatrix.sorted), 'manhattan', 'complete')
colDendrogramManhattanSingle <- computeDendrogram(t(zScoreMatrix.sorted), 'manhattan', 'single')
colDendrogramManhattanAverage <- computeDendrogram(t(zScoreMatrix.sorted), 'manhattan', 'average')
rowDendrogramManhattanComplete <- computeDendrogram(zScoreMatrix.sorted, 'manhattan', 'complete')
rowDendrogramManhattanSingle <- computeDendrogram(zScoreMatrix.sorted, 'manhattan', 'single')
rowDendrogramManhattanAverage <- computeDendrogram(zScoreMatrix.sorted, 'manhattan', 'average')

getFeatureName <- function(concept) {
    featureName <- tail(strsplit(concept, '\\\\')[[1]], n=1)
    featureName <- fixString(featureName)
    featureName
}

conceptStrToFolderStr <- function(s) {
    splitString <- strsplit(s, "")[[1]]
    backslashs <- which(splitString == "\\")
    substr(s, 0, tail(backslashs, 2)[1])
}

buildLowDimFields <- function(featureName, local.patientIDs, global.patientIDs, type, values, zScores) {
    lowDimFields <- data.frame(
            FEATURE=rep(featureName, length(local.patientIDs)),
            PATIENTID=local.patientIDs,
            TYPE=rep(type, length(local.patientIDs)),
            VALUE=values,
            ZSCORE=if(zScores) scale(values) else rep(NA, length(local.patientIDs)))
    lowDimFields <- lowDimFields[lowDimFields$PATIENTID %in% global.patientIDs, ]
    lowDimFields <- lowDimFields[order(lowDimFields$PATIENTID, decreasing=FALSE), ]
    lowDimFields
}

extraFields <- c()
features <- c()

if (length(data.cohort2$mRNAData) > 0) {
    featureName <- 'Cohort'
    values <- sapply(patientIDs, FUN=function(id) ifelse(id %in% patientIDs.cohort1, 1, 2))
    extraFields <- buildLowDimFields(featureName, patientIDs, patientIDs, 'binary', values, FALSE)
    features <- featureName
}

numerical.lowDimData.cohort1 <- data.cohort1$additionalFeatures_numerical
numerical.lowDimData.cohort2 <- data.cohort2$additionalFeatures_numerical
numerical.lowDimData <- rbind(numerical.lowDimData.cohort1, numerical.lowDimData.cohort2)
concepts <- unique(numerical.lowDimData$concept)
for (concept in concepts) {
    featureName <- getFeatureName(concept)
    conceptData <- numerical.lowDimData[numerical.lowDimData$concept == concept, ]
    type = ifelse(length(unique(conceptData$value)) == 2, 'binary', 'numerical')
    newFields <- buildLowDimFields(featureName, conceptData$patientID, patientIDs, type, conceptData$value, TRUE)
    extraFields <- rbind(extraFields, newFields)
    features <- c(features, featureName)
}

alphabetical.lowDimData.cohort1 <- data.cohort1$additionalFeatures_alphabetical
alphabetical.lowDimData.cohort2 <- data.cohort2$additionalFeatures_alphabetical
alphabetical.lowDimData <- rbind(alphabetical.lowDimData.cohort1, alphabetical.lowDimData.cohort2)
folders <- as.vector(sapply(alphabetical.lowDimData$concept, conceptStrToFolderStr))
unique.folders <- unique(folders)
for (folder in unique.folders) {
    folderData <- alphabetical.lowDimData[folder == folders, ]
    featureName <- getFeatureName(folder)
    newFields <- buildLowDimFields(featureName, folderData$patientID, patientIDs, 'alphabetical', folderData$value, FALSE)
    extraFields <- rbind(extraFields, newFields)
    features <- c(features, featureName)
}

### WRITE OUTPUT ###

output$extraFields <- extraFields
output$features <- features
output$fields <- fields
output$significanceValues <- significanceValues.sorted
output$patientIDs <- patientIDs
output$probes <- probes
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
