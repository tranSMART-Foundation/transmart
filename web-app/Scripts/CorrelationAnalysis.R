if (! suppressMessages(require(reshape2))) stop("SmartR requires the R package 'reshape2'")

conceptStrToFolderStr <- function(s) {
    splitString <- strsplit(s, "")[[1]]
    backslashs <- which(splitString == "\\")
    substr(s, 0, tail(backslashs, 2)[1])
}

points <- SmartR.data.cohort1$datapoints
concepts <- unique(points$concept)
if (! length(points)) stop('Your selection does not match any patient in the defined cohort!')
df <- data.frame(dcast(points, patientID ~ concept))
if(length(SmartR.settings$patientIDs)) df <- df[df$patientID %in% SmartR.settings$patientIDs, ]
annotations <- SmartR.data.cohort1$annotations
folders <- as.vector(sapply(annotations$concept, conceptStrToFolderStr))
if (length(unique(folders)) > 1) {
    stop("Sorry, but at this moment only one folder at a time is supported for annotation.")
} else if (length(unique(folders)) == 1) {
    annotations <- annotations[, c('patientID', 'value')]
    df <- merge(df, annotations, by='patientID', all=T)
    colnames(df) <- c('patientID', 'x', 'y', 'tag')
    tags <- df$tag
} else {
    tags <- list()
   colnames(df) <- c('patientID', 'x', 'y')
}

df <- df[!is.na(df[,2]) & !is.na(df[,3]), ]

corTest <- tryCatch({
	cor.test(df[,2], df[,3], method=SmartR.settings$method)
}, error = function(e) {
	ll <- list()
	ll$estimate <- as.numeric(NA)
	ll$p.value <- as.numeric(NA)
	ll
})

regLineSlope <- corTest$estimate * (sd(df[,3]) / sd(df[,2]))
regLineYIntercept <- mean(df[,3]) - regLineSlope * mean(df[,2])

SmartR.output$correlation <- corTest$estimate
SmartR.output$pvalue <- corTest$p.value
SmartR.output$regLineSlope <- regLineSlope
SmartR.output$regLineYIntercept <- regLineYIntercept
SmartR.output$method <- SmartR.settings$method
SmartR.output$xArrLabel <- concepts[1]
SmartR.output$yArrLabel <- concepts[2]
SmartR.output$patientIDs <- df[,0]
SmartR.output$tags <- unique(tags)
SmartR.output$points <- df