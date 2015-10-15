library(gplots)



main <- function(){
  dataset <- loaded_variables[[1]] #dataframe with columns: Row.Label, Bio.marker, ASSAY_0001 ASSAY_0002 ...
  measurements <- extractMeasurements(dataset)
  measurements <- assignNames(measurements,dataset)
  measurements <- transform(measurements)
  makeHeatmap(measurements)
}

extractMeasurements <- function(measurements){
  measurements  <- subset(dataset,select=-c(Row.Label,Bio.marker)) # this will select all columns other than Row.Label,Bio.marker columns
  measurements  <- data.matrix(measurements)
}

assignNames <- function(measurements,dataset){
  rownames(measurements) <- dataset$Row.Label
  return(measurements)
}

transform <- function(measurements){
  measurements <- log(measurements,2)
}

makeHeatmap <- function(measurements){
  png(filename="heatmap.png",width = 800,height=800)
  heatmap.2(measurements,
            scale = "none",
            dendrogram = "none",
            Rowv = NA,
            Colv = NA,
            density.info = "none", # histogram", # density.info=c("histogram","density","none")
            trace = "none",
            col=redgreen(75),
            margins=c(12,12)
            #ColSideColors= as.character(groups)
            #adjCol=c("left","top")
  )
  dev.off()
}

