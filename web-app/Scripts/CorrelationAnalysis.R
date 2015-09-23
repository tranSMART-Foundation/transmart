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
concepts <- unique(points$concept)
if (! length(points)) {
	stop('Your selection does not match any patient in the defined cohort!')
}
xArr <- points[points$concept == concepts[1], ]
yArr <- points[points$concept == concepts[2], ]

xArr <- xArr[xArr$patientID %in% yArr$patientID, ]
yArr <- yArr[yArr$patientID %in% xArr$patientID, ]

xArr <- xArr[order(xArr$patientID), ]
yArr <- yArr[order(yArr$patientID), ]

patientIDs <- xArr$patientID

xArr <- xArr$value
yArr <- yArr$value

if (length(xArr) != length(yArr)) {
	stop(paste('Both variables must have the same number of patients!', length(xArr), length(yArr)))
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
