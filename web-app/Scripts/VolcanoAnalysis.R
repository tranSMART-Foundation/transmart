if (! suppressMessages(require(reshape2))) {
    stop("SmartR requires the R package 'reshape2'")
}

if (! suppressMessages(require(limma))) {
    stop("SmartR requires the R package 'limma'")
}

if (length(SmartR.data.cohort1$mRNAData$PATIENTID) == 0) {
    stop('Your selection does not match any patient in the defined cohort!')
}

discardNullGenes <- strtoi(SmartR.settings$discardNullGenes)

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

valueMatrix.cohort1 <- makeMatrix(SmartR.data.cohort1$mRNAData)
valueMatrix.cohort2 <- makeMatrix(SmartR.data.cohort2$mRNAData)

colnames(valueMatrix.cohort1) <- sub("^X", "", colnames(valueMatrix.cohort1))
colnames(valueMatrix.cohort2) <- sub("^X", "", colnames(valueMatrix.cohort2))

valueMatrix <- merge(valueMatrix.cohort1, valueMatrix.cohort2, by="UID", all=FALSE)

patientIDs <- colnames(valueMatrix[, -1])

log2Matrix <- cbind(UID=valueMatrix$UID, log2(valueMatrix[, -1]))
zScoreMatrix <- as.data.frame(t(apply(log2Matrix[, -1], 1, scale)))
colnames(zScoreMatrix) <- patientIDs
zScoreMatrix <- cbind(UID=valueMatrix$UID, zScoreMatrix)

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

uids <- fixString(valueMatrix$UID)
pValues <- top.fit$P.Value
negativeLog10PValues <- -log10(top.fit$P.Value)
logFCs <- top.fit$logFC

SmartR.output$uids <- uids
SmartR.output$pValues <- pValues
SmartR.output$negativeLog10PValues <- negativeLog10PValues
SmartR.output$logFCs <- logFCs
SmartR.output$patientIDs <- patientIDs
SmartR.output$zScoreMatrix <- zScoreMatrix
