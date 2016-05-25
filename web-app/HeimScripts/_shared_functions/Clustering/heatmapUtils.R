########################################################################
## This file contains more specific heatmap-related functionalities   ##
########################################################################




computeDendrogram <- function(distances, linkageMethod) {
  as.dendrogram(hclust(distances, method = linkageMethod))
}

dendrogramToJSON <- function(d) {
  totalMembers <- attributes(d)$members
  add_json <- function(x, start) {
    members <- attributes(x)$members
    height <- attributes(x)$height
    index <- (start - 1):(start + members - 2)
    index <- paste(index, collapse = ' ')
    jsonString <<- paste(
      jsonString,
      sprintf('{"height":"%s", "index":"%s", "children":[', height, index)
    )
    if (is.leaf(x)) {
      jsonString <<- paste(jsonString, ']}')
    } else {
      add_json(x[[1]], start)
      jsonString <<- paste(jsonString, ",")
      leftMembers <- attributes(x[[1]])$members
      add_json(x[[2]], start + leftMembers)
      jsonString <<- paste(jsonString, "]}")
    }
  }
  jsonString <- ""
  add_json(d, 1)
  return(jsonString)
}


addClusteringOutput <- function(jsn, measurements_arg) {
  # we need to discard rows and columns without at least two values
  # determine rows without two non-NAs
  thresholdRows <- floor(ncol(measurements_arg) / 2 ) + 1  #  Half of the lentgh +1 has to be filled otherwise
  thresholdCols <- floor(nrow(measurements_arg) / 2 ) + 1  #  there might not be enough overlap for clustering
  logicalSelection <- apply(measurements_arg, 1, function(row) length(which(!is.na(row))) >= thresholdRows)
  measurements_rows <- measurements_arg[logicalSelection, ]
  # and for columns
  logicalSelection <- apply(measurements_arg, 2, function(col) length(which(!is.na(col))) >= thresholdCols)
  measurements_cols <- t(measurements_arg[ , logicalSelection])
  jsn$numberOfClusteredRows <- nrow(measurements_rows)
  jsn$numberOfClusteredColumns <- nrow(measurements_cols)  # still nrow (transposed)
  if (is.null(jsn$numberOfClusteredRows)) jsn$numberOfClusteredRows <- 0
  if (is.null(jsn$numberOfClusteredColumns)) jsn$numberOfClusteredColumns <- 0
  if (jsn$numberOfClusteredRows < 2 | jsn$numberOfClusteredColumns < 2 ) {  # Cannot cluster less than 2x2 matrix
    jsn$warnings <- append(jsn$warnings, c("Clustering could not be done due to insufficient data"))
    return(jsn)
  }
  
  euclideanDistancesRow <- dist(measurements_rows, method = "euclidean")
  manhattanDistancesRow <- dist(measurements_rows, method = "manhattan")
  euclideanDistancesCol <- dist(measurements_cols, method = "euclidean")
  manhattanDistancesCol <- dist(measurements_cols, method = "manhattan")
  
  colDendrogramEuclideanComplete <- computeDendrogram( euclideanDistancesCol, 'complete')
  colDendrogramEuclideanSingle <- computeDendrogram( euclideanDistancesCol, 'single')
  colDendrogramEuclideanAverage <- computeDendrogram( euclideanDistancesCol, 'average')
  rowDendrogramEuclideanComplete <- computeDendrogram( euclideanDistancesRow, 'complete')
  rowDendrogramEuclideanSingle <- computeDendrogram( euclideanDistancesRow, 'single')
  rowDendrogramEuclideanAverage <- computeDendrogram( euclideanDistancesRow, 'average')
  
  colDendrogramManhattanComplete <- computeDendrogram( manhattanDistancesCol, 'complete')
  colDendrogramManhattanSingle <- computeDendrogram( manhattanDistancesCol, 'single')
  colDendrogramManhattanAverage <- computeDendrogram( manhattanDistancesCol, 'average')
  rowDendrogramManhattanComplete <- computeDendrogram( manhattanDistancesRow, 'complete')
  rowDendrogramManhattanSingle <- computeDendrogram( manhattanDistancesRow, 'single')
  rowDendrogramManhattanAverage <- computeDendrogram( manhattanDistancesRow, 'average')
  
  calculateOrderInTermsOfIndexes <- function(dendrogram, originalOrderedLabels) {
    clusterOrderedLabels  <- labels(dendrogram)
    
    allIndexes <- 1 : length(originalOrderedLabels)
    orderOfClustered <- match(clusterOrderedLabels, originalOrderedLabels)
    
    # put the elements that were not clustered at the end
    notIncluded <- allIndexes[!is.element(allIndexes, orderOfClustered)]
    c(orderOfClustered, notIncluded) - 1  # start at 0
  }
  
  columnOrder <- function(dendrogram) {
    calculateOrderInTermsOfIndexes(dendrogram, colnames(measurements_arg))
  }
  rowOrder <- function(dendrogram) {
    calculateOrderInTermsOfIndexes(dendrogram, rownames(measurements_arg))
  }
  
  
  jsn$hclustEuclideanComplete <- list(
    columnOrder(colDendrogramEuclideanComplete),
    rowOrder(rowDendrogramEuclideanComplete),
    dendrogramToJSON(colDendrogramEuclideanComplete),
    dendrogramToJSON(rowDendrogramEuclideanComplete)
  )
  
  jsn$hclustEuclideanSingle <- list(
    columnOrder(colDendrogramEuclideanSingle),
    rowOrder(rowDendrogramEuclideanSingle),
    dendrogramToJSON(colDendrogramEuclideanSingle),
    dendrogramToJSON(rowDendrogramEuclideanSingle)
  )
  
  jsn$hclustEuclideanAverage <- list(
    columnOrder(colDendrogramEuclideanAverage),
    rowOrder(rowDendrogramEuclideanAverage),
    dendrogramToJSON(colDendrogramEuclideanAverage),
    dendrogramToJSON(rowDendrogramEuclideanAverage)
  )
  
  jsn$hclustManhattanComplete <- list(
    columnOrder(colDendrogramManhattanComplete),
    rowOrder(rowDendrogramManhattanComplete),
    dendrogramToJSON(colDendrogramManhattanComplete),
    dendrogramToJSON(rowDendrogramManhattanComplete)
  )
  
  jsn$hclustManhattanSingle <- list(
    columnOrder(colDendrogramManhattanSingle),
    rowOrder(rowDendrogramManhattanSingle),
    dendrogramToJSON(colDendrogramManhattanSingle),
    dendrogramToJSON(rowDendrogramManhattanSingle)
  )
  
  jsn$hclustManhattanAverage <- list(
    columnOrder(colDendrogramManhattanAverage),
    rowOrder(rowDendrogramManhattanAverage),
    dendrogramToJSON(colDendrogramManhattanAverage),
    dendrogramToJSON(rowDendrogramManhattanAverage)
  )
  return(jsn)
}
