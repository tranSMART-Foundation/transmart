#global_register <-list(rowNames = c("a","b"),
#                       columnNames = c("1","2"),
#                       origin = c(1,2),
#                       data = matrix(c(35,700),nrow=2,ncol=2)
#                       )

library(gplots)
# This script expects global_register to be populated before execution
# see lines above for an example
data.row.names <- global_register["rowNames"][[1]]
data.col.names <- global_register["columnNames"][[1]]
data.origin <- global_register["origin"][[1]]
data.matrix <- global_register["data"][[1]]

rownames(data.matrix) <- unlist(data.row.names)
colnames(data.matrix) <- unlist(data.col.names)

#For now we assume only 2 groups. 1 and 2
color.groups <- function(x){
  if(x == 1){
    return("coral3") # R color
  }else{
    return("chartreuse3")
  }
}

groups <- lapply(data.origin, color.groups)
groups <- unlist(groups)

png(filename="heatmap.png",width = 800,height=800)
heatmap.2(data.matrix,
          scale = "none",
          dendrogram = "none",
          Rowv = NA,
          Colv = NA,
          density.info = "none", # histogram", # density.info=c("histogram","density","none")
          trace = "none",
          col=redgreen(75),
          margins=c(12,12),
          ColSideColors= as.character(groups)
          #adjCol=c("left","top")
)
dev.off()
