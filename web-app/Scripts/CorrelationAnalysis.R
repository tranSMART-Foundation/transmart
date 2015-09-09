### PREPARE SETTINGS ###

method <- settings$method
if (! is.null(settings$xLow)) {
	xLow <- as.integer(settings$xLow)
	xHigh <- as.integer(settings$xHigh)
	yLow <- as.integer(settings$yLow)
	yHigh <- as.integer(settings$yHigh)
} else {
	xLow <- -Inf
	xHigh <- Inf
	yLow <- -Inf
	yHigh <- Inf
}

### COMPUTE RESULTS ###

points <- data.cohort1$datapoints
patientIDs <- unique(points$patientID)
concepts <- unique(points$concept)
if (length(concepts) != 2) {
	stop('Please specify exactly two variables to compare with each other!')
}
xArr <- points[points$concept == concepts[1], ]$value
yArr <- points[points$concept == concepts[2], ]$value
if (length(xArr) != length(yArr)) {
	stop('Both variables must have the same number of patients!')
}
selection <- (xArr >= xLow
			& xArr <= xHigh
			& yArr >= yLow
			& yArr <= yHigh)
xArr <- xArr[selection]
yArr <- yArr[selection]
patientIDs <- patientIDs[selection]

annotations <- data.cohort1$annotations
tags <- list()
if (length(annotations) > 0) {
	annotations <- annotations[annotations$patientID %in% patientIDs, ]
	tags <- annotations$value
	if (length(tags) == 0) {
		stop("The chosen annotations don't map to any patients")
	}
	sorting <- match(annotations$patientID, patientIDs)
	tags <- tags[order(sorting)]
}

corTest <- cor.test(xArr, yArr, method=method)
regLineSlope <- corTest$estimate * (sd(yArr) / sd(xArr))
regLineYIntercept <- mean(yArr) - regLineSlope * mean(xArr)

### WRITE OUTPUT ###

output$correlation <- corTest$estimate
output$pvalue <- corTest$p.value
output$regLineSlope <- regLineSlope
output$regLineYIntercept <- regLineYIntercept
output$method <- settings$method
output$xArrLabel <- concepts[1]
output$yArrLabel <- concepts[2]
output$xArr <- xArr
output$yArr <- yArr
output$patientIDs <- patientIDs
output$tags <- tags
