library(reshape2)
library(limma)
library(jsonlite)


# SE: Just to get things working for dev purposes
#  rm(list = ls())
#  load("/Users/serge/Documents/Projects/SmartR/Development_env_Input_workspace/R_workspace_objects/Heatmap/data.Rda")
#  load("/Users/serge/Documents/Projects/SmartR/Development_env_Input_workspace/R_workspace_objects/Heatmap/fetchParams.Rda")
#  load("/Users/serge/Documents/Projects/SmartR/Development_env_Input_workspace/R_workspace_objects/Heatmap/loaded_variables_withLDD.Rda")
#  load("/Users/serge/Documents/Projects/SmartR/Development_env_Input_workspace/R_workspace_objects/Heatmap/fetch_params_withLDD.Rda")
#  setwd("/Users/serge/GitHub/SmartR")
#######


if (!exists("remoteScriptDir")) {  #  Needed for unit-tests
    remoteScriptDir <- "web-app/HeimScripts"
}


## Loading functions ##
utils <- paste(remoteScriptDir, "/_shared_functions/Generic/utils.R", sep="")
limmaUtils <- paste(remoteScriptDir, "/_shared_functions/GEX/limmaUtils.R", sep="")
dataFrameUtils <- paste(remoteScriptDir, "/_shared_functions/GEX/DataFrameAndGEXmatrixUtils.R", sep="")
heatmapUtils <- paste(remoteScriptDir, "/_shared_functions/Clustering/heatmapUtils.R", sep="")

source(utils)
source(limmaUtils)
source(dataFrameUtils)
source(heatmapUtils)
#######################


SUBSET1REGEX <- "_s1$"  # Regex identifying columns of subset 1.
markerTableJson <- "markerSelectionTable.json" # Name of the json file with limma outputs

main <- function(max_rows = 100, sorting = "nodes", ranking = "coef", geneCardsAllowed = FALSE) {
    max_rows <- as.numeric(max_rows)
    verifyInputHeatmap(max_rows, sorting)
    
    ## Returns a list containing two variables named HD and LD
    data.list <- parseInput()
    
    hd.df = data.list$HD
    ld.df = data.list$LD    
    
    ## Low dimensional annotation data frame  
    extraFieldsExtended.df = buildExtraFieldsExtended(ld.df)
    
    
    ## SE: for debug
    ## hd.df = hd.df[, c(1, 2, 4, 109, 110)]
    ## Two subsets, 2 samples in one subset and one in the other
    ## hd.df = hd.df[, c(1, 2, 4, 109, 110)]
    
    ## Two subsets, one sample in one subset and one in the other
    ## hd.df = hd.df[, c(1, 2, 4, 110)]

    ## two subsets, multiple samples 
    ## hd.df = hd.df[, c(1, 2, 4, 109)]
        
    ## two subsets, multiple samples 
   # hd.df = hd.df[, c(1, 2, 4, 5, 6, 7, 8, 107, 108, 109)]
    
        
    write.table(
        hd.df,
        "heatmap_orig_values.tsv",
        sep = "\t",
        na = "",
        row.names = FALSE,
        col.names = TRUE
    )
    
    ## Creating the extended data frame containing besides the input data,
    ## a set of statistics. 
    hd.df          <- addStats(hd.df, sorting, ranking, max_rows)
    

    hd.df          <- mergeDuplicates(hd.df)
    hd.df          <- hd.df[1:min(max_rows, nrow(hd.df)), ]  #  apply max_rows
    
    fields      <- buildFields(hd.df)
    extraFields <- buildExtraFields(fields)
    uids        <- hd.df[, 1]
    patientIDs  <- unique(fields["PATIENTID"])[,1]
    

    significanceValues <- hd.df["SIGNIFICANCE"][,1]
    
        
    features <- unique(extraFields["FEATURE"])[,1]

    
    ## A df containing the computed value rankings for
    ## all possible statistical methods
    ranking_hd.df = getAllStatRanksForExtDataFrame(hd.df)
    

    ## The returned jsn object that will be dumped to file
    jsn <- list(
        "fields"              = fields,
        "patientIDs"          = patientIDs,
        "uids"                = uids,
        "logfoldValues"       = hd.df["LOGFOLD"][,1],
        "ttestValues"         = hd.df["TTEST"][,1],
        "pvalValues"          = hd.df["PVAL"][,1],
        "adjpvalValues"       = hd.df["ADJPVAL"][,1],
        "bvalValues"          = hd.df["BVAL"][,1],
        "ranking"             = ranking,
        "features"            = features,
        "extraFields"         = extraFields,
        "extraFieldsExtended" = extraFieldsExtended.df,
        "maxRows"             = max_rows,
        "allStatRanking"      = ranking_hd.df,
        "warnings"            = c() # initiate empty vector
    )
    
    ## To keep track of the parameters selected for the execution of the code
    writeRunParams(max_rows, sorting, ranking)
    
    # temporary stats like SD and MEAN need to be removed for clustering to work
    measurements <- cleanUp(hd.df)  

    
    # discard UID column
    if (ncol(hd.df) > 2){
        measurements <- measurements[, 2:ncol(measurements)]
    } else {
        measurements <- measurements[2]
    }

    measurements <- toZscores(measurements)
    
    
    ## If no significanceValues are available throw a warning:
    if (all(is.na(significanceValues)))
        jsn$warnings <- append(jsn$warnings, c("Significance sorting could not be done due to insufficient data"))
    
    
    jsn <- addClusteringOutput(jsn, measurements) #
    jsn <- toJSON(jsn, pretty = TRUE, digits = I(17))
    writeDataForZip(hd.df, measurements, patientIDs)  # for later zip generation
    write(jsn, file = "heatmap.json")
    # json file be served the same way
    # like any other file would - get name via
    # /status call and then /download

    msgs <- c("Finished successfuly")
    list(messages = msgs)
    
#     ## SE: For debug purposes
#       return(jsn)
}




# SE: For debug purposes
# out = main(ranking = "median")
#print(out[1:20,])


