

main <- function( excludedPatientIDs = c("") ) {
  input      <<- parseInput(loaded_variables)

  subsets    <- parseSubsets(input$groups1)
  datapoints <- parseDataPoints(input$box1)

  concept <- datapoints$concept[1]

  data <- list()
  points <- datapoints[,1:2]

  patientIDs <- points$patientID
  points$concept <- concept
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
  dfOut <- list(cohort1 = data)
  toJSON(dfOut)
}

dropEmpty <- function(df) {
  df[df$value != "",]
}

parseSubsets <- function(subsetDfs) {
  if (length(subsetDfs) == 0) {
    return(list())
  }
  df <- subsetDfs[[1]]
  colnames(df) <- c('patientID', 'value')
  df <- dropEmpty(df)
  if (length(subsetDfs) == 1) {
    return(df)
  }
  for (i in 2:length(subsetDfs)) {
    subset <- subsetDfs[[i]]
    colnames(subset) <- c('patientID', 'value')
    subset <- dropEmpty(subset)
    df <- rbind(df, subset)
  }
  df
}

parseDataPoints <- function(datapointsDfs) {
  if (length(datapointsDfs) == 0) {
    stop("No datapoints in box1 - cannot generate boxplot")
  }
  df <- datapointsDfs[[1]]
  concept <- rep(colnames(df)[2], nrow(df))
  colnames(df) <- c('patientID', 'value')
  df$concept   <- concept
  if (length(datapointsDfs) == 1) {
    return(df)
  }
  for (i in 2:length(datapointsDfs)) {
    pointsDF <- datapointsDfs[[i]]
    concept            <- rep( colnames(pointsDF)[2], nrow(pointsDF) )
    colnames(pointsDF) <- c('patientID', 'value')
    pointsDF$concept   <- concept
    df <- rbind(df, pointsDF)
  }
  df
}

parseInput <- function(input) {
  dataLabels <- names(input)
  splitted <- strsplit(dataLabels, "_")
  sources <- sapply(splitted, function(x) { x[[1]] })
  sources <- unique(sources)
  labelsOrganizedBySource <- list()
  for (dataSource in sources) {
    regexpPattern <- paste(dataSource,"_",sep="")
    indices <- grep(dataLabels, pattern =  regexpPattern)
    labelsOrganizedBySource[[dataSource]] <- input[indices]
  }
  labelsOrganizedBySource
}
