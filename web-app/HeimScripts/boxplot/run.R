
main <- function(excludedPatientIDs) {
	data <- list()
	data$cohort1 <- list()
	data$cohort2 <- list()
	data$cohort1$concept <- names(loaded_variables)[1]  # tell the subsets apart by _sX identifier
	subset.points <- loaded_variables[0]
	bxp <- boxplot(subset.points$value, plot=FALSE)
	subset <- "no subset"
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
	jsn <- toJson(data, pretty=TRUE)
	print(jsn)
}
