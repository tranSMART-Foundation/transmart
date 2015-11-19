###########
## R-script to produce summary statistics (mean, median, etc.,) and static boxplot image for the data loading and data preprocessing tab
#
# Expected input: 
# * variable "loaded_variables" - a list of data.frames, containing one or more data.frames. Present in the environment.
#   Data for multiple high dimensional nodes can be provided. 
#   Per high dimensional node 1 or 2 dataframes are to be passed on, depending on whether 1 or 2 patient subsets are created. 
#   Descriptive names (labels) are given to the data.frames so that it can be recognized which data.frame was derived from which data node
#   PROPOSED FORMAT: some unique identifier for the node (numerical identifier appended behind the letter "n") followed by _s1 or _s2 depending on subset, e.g. n0_s1, n0_s2, n1_s1, n1_s2. 
#         (RIGHT NOW THE UNDERSCORE IS USED FOR SPLITTING THE TWO, SO IF UNDERSCORES ARE USED IN THE NODE IDENTIFIER ,THIS SHOULD BE CHANGED)
#   The data.frames (coming from high dimensional nodes) have columns: Row.Label, Bio.marker (optional), ASSAY_0001, ASSAY_0002 ...  
#     ** right now this is only implemented for high dimensional data nodes, later the functionality might be extended for clinical data. In that case it is possible to recognize if it is high or low dim data based on the column names of the data.frame (assuming low dim data will also be passed on in the form of data.frames)
#
# Output: 
# * 1 boxplot image per data node, png format. Name: Box_plot_Node_<Node Identifier>.png. 
# * 1 textfile per node with the summary statistics per subset, in json format. Name: Summary_stats-Node_<Node Identifier>.json. \
# Note: If the data node is not high dimensional or the dataset is empty, no boxplot will be returned - only an image with the text "No data points to plot", 
#   also no mean, median etc will be returned in the summary statistics: only variableLabel, node name and subset name are returned 
#   and totalNumberOfValues = 1 and numberOfMissingValues = 1.
###########

library(jsonlite)
library(gplots)

main <- function()
{
  data_measurements <- extract_measurements(loaded_variables)
  produce_summary_stats(data_measurements)
  produce_boxplot(data_measurements)
}


# Extract the measurement values from the data.frames.
#  The data.frames (coming from high dimensional nodes) have columns: Row.Label, Bio.marker (optional), ASSAY_0001, ASSAY_0002 ...  
#  All columns except Row.Label and Bio.marker contain the measurement values.
extract_measurements <- function(datasets)
{  
  for(i in 1:length(datasets))
  {
    dataset <- datasets[[i]]
    colNames <- colnames(dataset)
    
    # test if the data.frame contains data from a high dimensional data node
    #   the first column of the data from high dimensional nodes should be "Row.Label", a second column called "Bio.marker" is optional.
    #   for low dimensional data these columns will contain different names 
    is_highDim <- (colNames[1] == "Row.Label")
    
    # if it is not high dim, then it is clinical data. Right now this script is only written to handle high dimensional data.
    if(!is_highDim)
    {
      datasets[[i]] <- NA
    }
    
    # only keep the measurement values, by removing the row label and biomarker columns
    if(is_highDim)
    {
      non_measurement_columns <- which(colNames %in% c("Row.Label","Bio.marker"))
      datasets[[i]] <- dataset[ ,-non_measurement_columns]
    }
  }
  
  return(datasets) 
}


# Function to produce one JSON file per data node containing the summary stats per subset for that node. Summary stats include: the number of values, number of missing values quartiles, min, max, mean, std deviation, median
# NOTE: this function converts the data.frame to a vector containing all datapoints from the dataframe and calculates the statistics over this vector
#       The statistics are thus not calculated per data column or row, but over the whole data.frame
# NOTE 2: quartiles, mean and other statistics can only be calculated on numeric data. If this method is extended for clinical data: 
#         * Are the desired summary statistics the same for clinical data (ie. mean, sd, median, etc.) or should different stats be calculated?
#         * extend this method to recognize low dim data and split up the data.frame or apply this method to each data column separately to calculate the statistics per clinical variable separately (ie. supply the clinical data as a list containing a separate data.frame/vector for each clinical variable)
#         * build in  a test to determine if a variable is numeric or categorical and only calculate the statistics if numeric. 
#         * If a variable is categorical: are missing values in that case NA or  "" (empty string? )
produce_summary_stats <- function(measurement_tables)
{
  # construct data.frame to store the results from the summary statistics in
  result_table <- as.data.frame(matrix(NA, length(measurement_tables),12, 
                                       dimnames = list(names(measurement_tables), 
                                                       c("variableLabel","node","subset","totalNumberOfValuesIncludingMissing", "numberOfMissingValues", "min","max","mean", "standardDeviation", "q1","median","q3"))))
  # add information about node and subset identifiers
  result_table$subset <- gsub(".*_","", rownames(result_table))
  result_table$node <- gsub("_.*","", rownames(result_table))
  
  # calculate summary stats per data.frame
  for(i in 1:length(measurement_tables))
  {
    # get the name of the data.frame, identifying the node and subset 
    identifier <- names(measurement_tables)[i]
    result_table[identifier, "variableLabel"] <- identifier
    
    # convert data.frame to a vector containing all values of that data.frame, a vector remains a vector
    measurements <- unlist(measurement_tables[[i]])
    
    # determine total number of values and number of missing values
    is.missing <- is.na(measurements)
    result_table[identifier, "totalNumberOfValuesIncludingMissing"] <- length(measurements)
    result_table[identifier, "numberOfMissingValues"] <- length(which(is.missing))
    
    # calculate descriptive statistics, only for numerical data. 
    # the 50% quantile is the median. 0 and 100% quartiles are min and max respectively
    result_table[identifier, "mean"] <- mean(measurements, na.rm=T)
    result_table[identifier, "standardDeviation"] <- sd(measurements, na.rm=T)
    
    quartiles <- quantile(measurements, na.rm=T) 
    result_table[identifier, "min"] <- quartiles["0%"]
    result_table[identifier, "max"] <- quartiles["100%"]
    result_table[identifier, "q1"] <- quartiles["25%"]
    result_table[identifier, "median"] <- quartiles["50%"]
    result_table[identifier, "q3"] <- quartiles["75%"]    
  }
  
  # remove rownames so that rownames are not included in the JSON output.
  rownames(result_table) <- 1:nrow(result_table)
  
  # write the summary statistics for each node to a separate file, in JSON format
  unique_nodes <- unique(result_table$node)
  for(node in unique_nodes)
  {
    partial_table <- result_table[which(result_table$node == node), ,drop = F]
    summary_stats_JSON <- toJSON(partial_table, dataframe = "rows", pretty = T)
    fileName <- paste("summary_stats_node_", node, ".json", sep = "")
    write(summary_stats_JSON, fileName)
  }
}

# Function that outputs one box plot image per data node
produce_boxplot <- function(measurement_tables)
{
  #get node and subset identifiers
  nodes <- gsub("_.*","",names(measurement_tables))
  subsets <- gsub(".*_","", names(measurement_tables))
  
  
  # convert the tables to vectors for use with the boxplot function
  # this converts a data.frame to a vector containing all values from the data.frame, a vector remains a vector 
  measurement_vectors <- measurement_tables
  for(i in 1:length(measurement_vectors))
  {
    measurement_vectors[[i]] <- unlist(measurement_vectors[[i]]) 
  }
  
  #make a separate boxplot for each node and write to a PNG file.
  for(node in nodes)
  {
    # grab the data.frames corresponding to the selected node
    identifiers_single_node <- grep(paste("^",node, sep = ""), names(measurement_vectors), value = T )
    single_node_data <- measurement_vectors[identifiers_single_node]
    
    #remove node prefix from the names (labels) of the data.frame 
    names(single_node_data) <-  gsub(".*_","",names(single_node_data))
    
    #make sure the subsets are always ordered s1, s2, ...
    single_node_data <- single_node_data[order(names(single_node_data))] 
    
    ## create box plot, output to PNG file
    fileName <- paste("box_plot_node_", node, ".png", sep = "")    
    png(filename = fileName)
    
    # in case there is data present: create box plot
    if(!all(is.na(single_node_data)))
    {
      boxplot(single_node_data, col = "grey", ylab = "Value") # REMOVE THIS AFTER DISCUSSION: with col and border you can also define color of the box plots... should it be white, grey, or for example two different colors in case of two subsets?
    }

    # if there are no data values: create image with text "No data points to plot"
    if(all(is.na(single_node_data)))
    {
      sinkplot("start")
      write("No data points\n\   to plot","")
      sinkplot("plot")
      box("outer", lwd= 2)
    }
    
    dev.off()
  }
}
