library(gplots)


dataset1color <- "coral3"
dataset2color <- "chartreuse3"
labelColumns <- c("Row.Label","Bio.marker")
MAX_ROWS <- 50 # this will be made into an argument after the prototype

#Input expected 1 or 2 dataframes with columns: Row.Label, Bio.marker, ASSAY_0001 ASSAY_0002 ...
# In the prorotype we do not use biomarker yet
main <- function(){
  datasets <- parseInput(loaded_variables) #Hardcoded input parsing - only for the prototype
  measurements <- extractMeasurements(datasets) #extract the numeric part - as a numeric matrix is needed for the heatmap.2 function
  humanReadableNames <-extractNames(datasets)
  measurements <- assignNames(measurements,humanReadableNames) #combine label with Biomarker(if present) and assign as a name to be displayed on the heatmap
  measurements <- transform(measurements) #log2 transform the numerical matrix with measurements
  grouping <- extractGrouping(datasets) #Grouping determines coloring of the samples - one color for dataset1 and a different one for dataset2
  makeHeatmap(measurements,grouping) # plot the heatmap and save it in the working directory as a .png file
}

parseInput <- function(variables){
  onlyTwo <- variables[1:2]
  onlyTwo[[1]] <- variables[[1]][1:MAX_ROWS,]
  onlyTwo <- onlyTwo[!sapply(onlyTwo, is.null)]
  if(length(onlyTwo) == 2){
    onlyTwo[[2]] <- variables[[2]][1:MAX_ROWS,]
  }
  return(onlyTwo)
}

extractGrouping <- function(datasets){
  ds1Length <- ncol(datasets[[1]]) - length(labelColumns)
  grouping <- rep(dataset1color,ds1Length)
  if(length(datasets) > 1){
    ds2Length <- ncol(datasets[[2]]) - length(labelColumns)
    secondSetGrouping <- rep(dataset2color,ds2Length)
    grouping <- c(grouping,secondSetGrouping)
  }
  return(grouping)
}

extractMeasurements <- function(datasets){
  measurements <- extractMeasurement(datasets[[1]])
  if(length(datasets) > 1){
    secondDsMeasurements <-extractMeasurement(datasets[[2]])
    measurements <- cbind(measurements,secondDsMeasurements)
  }
  return(measurements)
}

extractMeasurement <- function(dataset){
  measurements  <- subset(dataset,select=-c(Row.Label,Bio.marker)) # this will select all columns other than Row.Label,Bio.marker columns
  measurements  <- data.matrix(measurements)
}

assignNames <- function(measurements, humanReadableRowNames){
  rownames(measurements) <- humanReadableRowNames
  return(measurements)
}

extractNames <- function(datasets){
  humanReadableNames <- datasets[[1]]$Row.Label
  #  if(length(datasets) > 1){
  #    namesSecondDs <- datasets[[2]]$Row.Label
  #    humanReadableNames <- c(humanReadableNames, namesSecondDs)
  #  }
  return(humanReadableNames)
}


transform <- function(measurements){
  measurements <- log(measurements,2)
}

makeHeatmap <- function(measurements,grouping){
  png(filename="heatmap.png", width = 800, height = 800)
  breakpoints <- seq(from= -2,to = 10,length.out=75) # this will clamp all values below 0.1
  breakpoints <- c(-10, breakpoints) # number of breaks must equal number of colors +1
  heatmap.2(measurements,
            scale = "none",
            dendrogram = "none",
            Rowv = NA,
            Colv = NA,
            density.info = "none",
            trace = "none",
            col=redgreen(75),
            margins=c(12,12),
            ColSideColors= as.character(grouping),
            breaks = breakpoints
  )
  dev.off()
}
