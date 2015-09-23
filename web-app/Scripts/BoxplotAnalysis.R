excludedPatientIDs <- settings$excludedPatientIDs

calc_boxplot <- function(datapoints, subsets) {
	data <- list()
	points <- datapoints[c('patientID', 'value')]
	subsets <- subsets[c('patientID', 'value')]
	patientIDs <- points$patientID
	concept <- datapoints$concept[1]
	data$concept <- concept
	data$globalMin <- min(points$value)
	data$globalMax <- max(points$value)
 	assignedPatientIDs <- c()
	nonEmptySubsets <- c()
	for (subset in c(unique(subsets$value), 'no subset')) {
		if (subset != 'no subset') {
			subset.points <- subsets[subsets$value == subset & subsets$patientID %in% patientIDs, ]
			subset.values <- points[points$patientID %in% subset.points$patientID, ]
		} else {
			unassignedPatientIDs <- patientIDs[! patientIDs %in% assignedPatientIDs]
			subset.points <- data.frame(patientID=unassignedPatientIDs, subset=rep(NA, length(unassignedPatientIDs)))			
			subset.values <- points[points$patientID %in% unassignedPatientIDs, ]
		}
		subset.points <- subset.points[! subset.points$patientID %in% excludedPatientIDs, ]
		subset.values <- subset.values[! subset.values$patientID %in% excludedPatientIDs, ]
		if (nrow(subset.points) > 0) {
			nonEmptySubsets <- c(nonEmptySubsets, subset)
			sorting <- match(subset.points$patientID, subset.values$patientID)
			subset.points <- subset.points[order(sorting), ]
			subset.points <- cbind(subset.points, subset.values$value)
			jitter <- runif(nrow(subset.points), -0.5, 0.5)
			subset.points <- cbind(subset.points, jitter)
			names(subset.points) <- c('patientID', 'subset', 'value', 'jitter')
			assignedPatientIDs <- c(assignedPatientIDs, subset.points$patientID)
			bxp <- boxplot(subset.points$value, plot=FALSE)
			data[[subset]] <- list()
			data[[subset]]$lowerWhisker <- bxp$stats[1]
			data[[subset]]$lowerHinge <- bxp$stats[2]
			data[[subset]]$median <- bxp$stats[3]
			data[[subset]]$upperHinge <- bxp$stats[4]
			data[[subset]]$upperWhisker <- bxp$stats[5]
			outlier <- subset.points$value > data[[subset]]$upperWhisker | subset.points$value < data[[subset]]$lowerWhisker
			subset.points <- cbind(subset.points, outlier)
			data[[subset]]$points <- subset.points
		}
	}
	data$subsets <- nonEmptySubsets
	data
}

if (! length(data.cohort1$concept1)) {
	stop('Your selection does not match any patient in the defined cohort!')
}
output$cohort1 <- calc_boxplot(data.cohort1$concept1, data.cohort1$subsets1)

if (length(data.cohort2$concept2) > 0) {
	output$cohort2 <- calc_boxplot(data.cohort2$concept2, data.cohort2$subsets2)
}
