library(gplots)


main <- function(){
dataset <- loaded_variables[[1]] #dataframe with columns: Row.Label, Bio.marker, ASSAY_0001 ASSAY_0002 ...

measurements  <- subset(dataset,select=-c(Row.Label,Bio.marker)) # this will select all columns other than Row.Label,Bio.marker columns
measurements  <- data.matrix(measurements)

measurements <- log(measurements,2)

rownames(measurements) <- df$Row.Label

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

