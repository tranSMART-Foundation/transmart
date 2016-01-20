if (! suppressMessages(require(reshape2))) {
    stop("SmartR requires the R package 'reshape2'")
}

### PREPARE SETTINGS ###

significanceMeassure <- SmartR.settings$significanceMeassure
discardNullGenes <- strtoi(SmartR.settings$discardNullGenes)
maxRows <- ifelse(is.null(SmartR.settings$maxRows), 100, as.integer(SmartR.settings$maxRows))

### COMPUTE RESULTS ###

makeMatrix <- function(raw.data) {
    matrix <- data.frame(
            PATIENTID=raw.data$PATIENTID,
            PROBE=raw.data$PROBE,
            GENESYMBOL=raw.data$GENESYMBOL,
            VALUE=raw.data$VALUE, stringsAsFactors=FALSE)
    matrix <- melt(matrix, id=c('PATIENTID', 'PROBE', 'GENESYMBOL'), na.rm=TRUE)
    matrix <- data.frame(dcast(matrix, PROBE + GENESYMBOL ~ PATIENTID), stringsAsFactors=FALSE)

    if (discardNullGenes) {
        matrix <- matrix[matrix$GENESYMBOL != ''
                | ! is.na(matrix$GENESYMBOL)
                | ! is.null(matrix$GENESYMBOL), ]
    } else {
        matrix[matrix$GENESYMBOL == ''
                | is.na(matrix$GENESYMBOL)
                | is.null(matrix$GENESYMBOL), "GENESYMBOL"] <- "NA"
    }

    matrix <- na.omit(matrix)

    dupl.where <- duplicated(matrix$PROBE)
    dupl.rows <- matrix[dupl.where, ]
    matrix <- matrix[! dupl.where, ]

    uids <- paste(matrix$PROBE, matrix$GENESYMBOL, sep="--")
    uids[matrix$PROBE == dupl.rows$PROBE] <- paste(uids[matrix$PROBE == dupl.rows$PROBE], dupl.rows$GENESYMBOL[matrix$PROBE == dupl.rows$PROBE], sep="--")
    matrix <- cbind(UID=uids, matrix[, -c(1,2)])
    matrix
}

fixString <- function(str) {
    str <- gsub("(?!-)[[:punct:]]", "", str, perl=TRUE)
    str <- gsub(" ", "", str)
    str
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

if (length(SmartR.data.cohort1$mRNAData$PATIENTID) == 0) {
    stop('Your selection does not match any patient in the defined cohort!')
}

valueMatrix.cohort1 <- makeMatrix(SmartR.data.cohort1$mRNAData)
colnames(valueMatrix.cohort1) <- sub("^X", "", colnames(valueMatrix.cohort1))
patientIDs.cohort1 <- colnames(valueMatrix.cohort1)

if (length(SmartR.data.cohort2$mRNAData$PATIENTID) > 0) {
    valueMatrix.cohort2 <- makeMatrix(SmartR.data.cohort2$mRNAData)
    colnames(valueMatrix.cohort2) <- sub("^X", "", colnames(valueMatrix.cohort2))
    valueMatrix <- merge(valueMatrix.cohort1, valueMatrix.cohort2, by="UID", all=FALSE)
} else {
    valueMatrix <- valueMatrix.cohort1
}

patientIDs <- colnames(valueMatrix[, -1])

log2Matrix <- cbind(UID=valueMatrix$UID, log2(valueMatrix[, -1]))
zScoreMatrix <- as.data.frame(t(apply(log2Matrix[, -1], 1, scale)))
colnames(zScoreMatrix) <- patientIDs
zScoreMatrix <- cbind(UID=valueMatrix$UID, zScoreMatrix)

if (significanceMeassure == 'variance') {
    significanceValues <- apply(log2Matrix[, -1], 1, var)
} else if (significanceMeassure == 'zScoreRange') {
    significanceValues <- apply(zScoreMatrix[, -1], 1, function(zScores) {
            bxp <- boxplot.stats(zScores)
            zScores.withoutOutliers <- zScores[zScores >= bxp$stats[2] & zScores <= bxp$stats[4]]
            max(zScores.withoutOutliers) - min(zScores.withoutOutliers)})
} else {
    if (! suppressMessages(require(limma))) {
        stop("SmartR requires the R package 'limma'")
    }
    classVectorS1 <- c(rep(1, ncol(valueMatrix.cohort1[, -1])), rep(2, ncol(valueMatrix[, -1]) - ncol(valueMatrix.cohort1[, -1])))
    classVectorS2 <- rev(classVectorS1)
    design <- cbind(S1=classVectorS1, S2=classVectorS2)
    contrast.matrix = makeContrasts(S1-S2, levels=design)
    fit <- lmFit(log2Matrix[, -1], design)
    fit <- contrasts.fit(fit, contrast.matrix)
    fit <- eBayes(fit)
    contr = 1
    top.fit = data.frame(
            logFC=fit$coefficients[, contr],
            t=fit$t[, contr],
            P.Value=fit$p.value[, contr],
            adj.P.val=p.adjust(p=fit$p.value[, contr], method='fdr'),
            B=fit$lods[, contr]
    )
    significanceValues <- top.fit[[significanceMeassure]]
}

valueMatrix$SIGNIFICANCE <- significanceValues

if (significanceMeassure == "P.Value" | significanceMeassure == "adj.P.val") {
    valueMatrix <- valueMatrix[order(significanceValues, decreasing=FALSE), ]
} else if (significanceMeassure == "logFC" | significanceMeassure == "t") {
    valueMatrix <- valueMatrix[order(abs(significanceValues), decreasing=TRUE), ]
} else {
    valueMatrix <- valueMatrix[order(significanceValues, decreasing=TRUE), ]
}

valueMatrix <- valueMatrix[1:maxRows, ]
log2Matrix <- log2Matrix[match(valueMatrix$UID, log2Matrix$UID), ]
zScoreMatrix <- zScoreMatrix[match(valueMatrix$UID, zScoreMatrix$UID), ]
significanceValues <- valueMatrix$SIGNIFICANCE
uids <- fixString(valueMatrix$UID)

fields.value <- melt(valueMatrix, id=c('UID', 'SIGNIFICANCE'))
fields.log2 <- melt(log2Matrix, id='UID')
fields.zScore <- melt(zScoreMatrix, id='UID')
fields <- cbind(fields.value, fields.log2$value, fields.zScore$value)
names(fields) <- c('UID', 'SIGNIFICANCE', 'PATIENTID', 'VALUE', 'LOG2', 'ZSCORE')
fields$UID <- fixString(fields$UID)

colDendrogramEuclideanComplete <- computeDendrogram(t(zScoreMatrix[, -1]), 'euclidean', 'complete')
colDendrogramEuclideanSingle <- computeDendrogram(t(zScoreMatrix[, -1]), 'euclidean', 'single')
colDendrogramEuclideanAverage <- computeDendrogram(t(zScoreMatrix[, -1]), 'euclidean', 'average')
rowDendrogramEuclideanComplete <- computeDendrogram(zScoreMatrix[, -1], 'euclidean', 'complete')
rowDendrogramEuclideanSingle <- computeDendrogram(zScoreMatrix[, -1], 'euclidean', 'single')
rowDendrogramEuclideanAverage <- computeDendrogram(zScoreMatrix[, -1], 'euclidean', 'average')

colDendrogramManhattanComplete <- computeDendrogram(t(zScoreMatrix[, -1]), 'manhattan', 'complete')
colDendrogramManhattanSingle <- computeDendrogram(t(zScoreMatrix[, -1]), 'manhattan', 'single')
colDendrogramManhattanAverage <- computeDendrogram(t(zScoreMatrix[, -1]), 'manhattan', 'average')
rowDendrogramManhattanComplete <- computeDendrogram(zScoreMatrix[, -1], 'manhattan', 'complete')
rowDendrogramManhattanSingle <- computeDendrogram(zScoreMatrix[, -1], 'manhattan', 'single')
rowDendrogramManhattanAverage <- computeDendrogram(zScoreMatrix[, -1], 'manhattan', 'average')

getFeatureName <- function(concept) {
    featureName <- paste(tail(strsplit(concept, '\\\\')[[1]], n=2), collapse='--')
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
            PATIENTID=as.factor(local.patientIDs),
            TYPE=rep(type, length(local.patientIDs)),
            VALUE=values,
            ZSCORE=if(zScores) scale(values) else rep(NA, length(local.patientIDs)))
    lowDimFields <- lowDimFields[lowDimFields$PATIENTID %in% global.patientIDs, ]
    lowDimFields <- lowDimFields[order(lowDimFields$PATIENTID, decreasing=FALSE), ]
    lowDimFields
}

extraFields <- c()
features <- c()

if (length(SmartR.data.cohort2$mRNAData$PATIENTID) > 0) {
    featureName <- 'Cohort'
    values <- sapply(patientIDs, FUN=function(id) ifelse(id %in% patientIDs.cohort1, 1, 2))
    extraFields <- buildLowDimFields(featureName, patientIDs, patientIDs, 'binary', values, FALSE)
    features <- featureName
}

numerical.lowDimData.cohort1 <- SmartR.data.cohort1$additionalFeatures_numerical
numerical.lowDimData.cohort2 <- SmartR.data.cohort2$additionalFeatures_numerical
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

alphabetical.lowDimData.cohort1 <- SmartR.data.cohort1$additionalFeatures_alphabetical
alphabetical.lowDimData.cohort2 <- SmartR.data.cohort2$additionalFeatures_alphabetical
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

SmartR.output$extraFields <- extraFields
SmartR.output$features <- features
SmartR.output$fields <- fields
SmartR.output$significanceValues <- significanceValues
SmartR.output$patientIDs <- patientIDs
SmartR.output$uids <- uids
SmartR.output$significanceMeassure <- significanceMeassure

SmartR.output$hclustEuclideanComplete <- list(
    order.dendrogram(colDendrogramEuclideanComplete) - 1,
    order.dendrogram(rowDendrogramEuclideanComplete) - 1,
    dendrogramToJSON(colDendrogramEuclideanComplete),
    dendrogramToJSON(rowDendrogramEuclideanComplete))

SmartR.output$hclustEuclideanSingle <- list(
    order.dendrogram(colDendrogramEuclideanSingle) - 1,
    order.dendrogram(rowDendrogramEuclideanSingle) - 1,
    dendrogramToJSON(colDendrogramEuclideanSingle),
    dendrogramToJSON(rowDendrogramEuclideanSingle))

SmartR.output$hclustEuclideanAverage <- list(
    order.dendrogram(colDendrogramEuclideanAverage) - 1,
    order.dendrogram(rowDendrogramEuclideanAverage) - 1,
    dendrogramToJSON(colDendrogramEuclideanAverage),
    dendrogramToJSON(rowDendrogramEuclideanAverage))

SmartR.output$hclustManhattanComplete <- list(
    order.dendrogram(colDendrogramManhattanComplete) - 1,
    order.dendrogram(rowDendrogramManhattanComplete) - 1,
    dendrogramToJSON(colDendrogramManhattanComplete),
    dendrogramToJSON(rowDendrogramManhattanComplete))

SmartR.output$hclustManhattanSingle <- list(
    order.dendrogram(colDendrogramManhattanSingle) - 1,
    order.dendrogram(rowDendrogramManhattanSingle) - 1,
    dendrogramToJSON(colDendrogramManhattanSingle),
    dendrogramToJSON(rowDendrogramManhattanSingle))

SmartR.output$hclustManhattanAverage <- list(
    order.dendrogram(colDendrogramManhattanAverage) - 1,
    order.dendrogram(rowDendrogramManhattanAverage) - 1,
    dendrogramToJSON(colDendrogramManhattanAverage),
    dendrogramToJSON(rowDendrogramManhattanAverage))
