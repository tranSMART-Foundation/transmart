### PREPARE SETTINGS ###

acfPatientID <- SmartR.settings$acfPatientID
xAxisSortOrder <- as.vector(SmartR.settings$xAxisSortOrder)
similarityMeasure <- SmartR.settings$similarityMeasure
linkageMeasure <- SmartR.settings$linkageMeasure
interpolateNAs <- strtoi(SmartR.settings$interpolateNAs)

### COMPUTE RESULTS ###

points <- SmartR.data.cohort1$datapoints

if (length(points$patientID) == 0) {
	stop('Your selection does not match any patient in the defined cohort!')
}

if (! is.null(acfPatientID)) {
	points <- points[points$patientID == acfPatientID, ]
}
timepoints <- sapply(strsplit(points$concept, '\\\\'), function(s) tail(s, n=1))
cutpoints <- sapply(gregexpr('\\\\', points$concept), function(l) l[length(l) - 1])
concepts <- substr(points$concept, 1, cutpoints)

points <- cbind(points, timepoints, stringsAsFactors=FALSE)
names(points)[4] <- 'timepoint'
points$concept <- concepts

sorting <- strtoi(sapply(strsplit(timepoints, ' '), function(s) head(s, n=1)))
points <- points[order(sorting), ]

uniq.patientIDs <- as.vector(unique(points$patientID))
uniq.concepts <- as.vector(unique(points$concept))
uniq.timepoints <- as.vector(unique(points$timepoint))

acfEstimates <- list()
if (! is.null(acfPatientID)) {
	if (! suppressMessages(require(zoo))) {
	    stop("SmartR's Timeline Analysis requires the R package 'zoo'")
	}
	for (concept in uniq.concepts) {
		patientConceptPoints <- points[points$concept == concept, ]
		notOccurringTimepoints <- xAxisSortOrder[! xAxisSortOrder %in% patientConceptPoints$timepoint]
		for (timepoint in notOccurringTimepoints) {
			patientConceptPoints <- rbind(patientConceptPoints, c(acfPatientID, concept, NA, timepoint))
		}
		sorting <- match(patientConceptPoints$timepoint, xAxisSortOrder)
		patientConceptPoints <- patientConceptPoints[order(sorting), ]
		if (interpolateNAs) {
			patientConceptPoints <- as.numeric(patientConceptPoints$value)
			patientConceptPoints <- as.vector(na.approx(zoo(patientConceptPoints), na.rm=FALSE))
			patientConceptPoints <- na.fill(patientConceptPoints, "extend")
			timepoints <- xAxisSortOrder
			lag <- length(timepoints)
		} else {
			patientConceptPoints <- patientConceptPoints[! is.na(patientConceptPoints$value), ]
			timepoints <- xAxisSortOrder[xAxisSortOrder %in% patientConceptPoints$timepoint]
			patientConceptPoints <- as.numeric(patientConceptPoints$value)
			lag <- length(timepoints)
		}
		acfEstimate <- acf(patientConceptPoints, lag.max=lag, plot=FALSE)
		acfEstimate <- c(acfEstimate$acf)
		acfEstimates[[concept]]$'estimate' <- acfEstimate
		acfEstimates[[concept]]$'sortOrder' <- timepoints
	}
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

clusterings <- list()
if (! is.null(similarityMeasure) && ! is.null(linkageMeasure)) {
	if (! suppressMessages(require(zoo))) {
	    stop("SmartR's Timeline Analysis requires the R package 'zoo'")
	}
	if (! suppressMessages(require(TSclust))) {
	    stop("SmartR's Timeline Analysis requires the R package 'TSclust'")
	}
	for (concept in uniq.concepts) {
		A <- matrix(nrow=0, ncol=length(xAxisSortOrder))
		for (patientID in uniq.patientIDs) {
			currentPoints <- points[points$patientID == patientID & points$concept == concept, ]
			notOccurringTimepoints <- xAxisSortOrder[! xAxisSortOrder %in% currentPoints$timepoint]
			for (timepoint in notOccurringTimepoints) {
				currentPoints <- rbind(currentPoints, c(patientID, concept, NA, timepoint))
			}
			sorting <- match(currentPoints$timepoint, xAxisSortOrder)
			currentPoints <- currentPoints[order(sorting), ]
			currentPoints <- as.numeric(currentPoints$value)
			if (interpolateNAs) {
				currentPoints <- as.vector(na.approx(zoo(currentPoints), na.rm=FALSE))
				currentPoints <- na.fill(currentPoints, "extend")
			}
			A <- rbind(A, currentPoints)
		}
		rownames(A) <- uniq.patientIDs
		B <- A[ , colSums(is.na(A)) == 0] # has no effect if interpolating
		if (interpolateNAs) {
			stopifnot(nrow(A) == nrow(B))
			stopifnot(ncol(A) == ncol(B))
		}
		if (ncol(B) <= 2) {
			stop('Distance matrix has not enough values. Is interpolation disabled?')
		}
		distMatrix <- diss(B, METHOD=similarityMeasure)
		clustering <- hclust(distMatrix, method=linkageMeasure)
		dendrogram <- as.dendrogram(clustering)
		clusterings[[concept]]$dendrogram <- dendrogramToJSON(dendrogram)
		clusterings[[concept]]$patientIDs <- uniq.patientIDs[order.dendrogram(dendrogram)]
	}
}

### WRITE OUTPUT ###

SmartR.output$data <- points
SmartR.output$concepts <- uniq.concepts
SmartR.output$patientIDs <- uniq.patientIDs
SmartR.output$timepoints <- uniq.timepoints
SmartR.output$acfEstimates <- acfEstimates
SmartR.output$clusterings <- clusterings
