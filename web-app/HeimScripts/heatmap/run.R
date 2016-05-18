library(reshape2)
library(limma)
library(jsonlite)


# # SE: Just to get things working for dev purposes
# rm(list = ls())
# load("/Users/serge/Documents/Projects/SmartR/Development_env_Input_workspace/R_workspace_objects/Heatmap/data.Rda")
# load("/Users/serge/Documents/Projects/SmartR/Development_env_Input_workspace/R_workspace_objects/Heatmap/fetchParams.Rda")
# setwd("/Users/serge/GitHub/SmartR")
# #######


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


SUBSET1REGEX <- "_s1$"  # Regex identifying columns of subset 1.
markerTableJson <- "markerSelectionTable.json" # Name of the json file with limma outputs

main <- function(max_rows = 100, sorting = "nodes", ranking = "coef", geneCardsAllowed = FALSE) {
    max_rows <- as.numeric(max_rows)
    verifyInput(max_rows, sorting)
    df <- parseInput()
    write.table(
        df,
        "heatmap_orig_values.tsv",
        sep = "\t",
        na = "",
        row.names = FALSE,
        col.names = TRUE
    )
    df          <- addStats(df, sorting, ranking, max_rows)
    df          <- mergeDuplicates(df)
    df          <- df[1:min(max_rows, nrow(df)), ]  #  apply max_rows
    fields      <- buildFields(df)
    extraFields <- buildExtraFields(fields)
    uids        <- df[, 1]
    patientIDs  <- unique(fields["PATIENTID"])[,1]

    significanceValues <- df["SIGNIFICANCE"][,1]
    features <- unique(extraFields["FEATURE"])[,1]
    jsn <- list(
        "fields"             = fields,
        "patientIDs"         = patientIDs,
        "uids"               = uids,
        "significanceValues" = significanceValues,
        "logfoldValues"      = df["LOGFOLD"][,1],
        "ttestValues"        = df["TTEST"][,1],
        "pvalValues"         = df["PVAL"][,1],
        "adjpvalValues"      = df["ADJPVAL"][,1],
        "bvalValues"         = df["BVAL"][,1],
        "ranking"            = ranking,
        "features"           = features,
        "extraFields"        = extraFields,
        "maxRows"            = max_rows,
        "warnings"           = c() # initiate empty vector
    )
    writeRunParams(max_rows, sorting, ranking)
    measurements <- cleanUp(df)  # temporary stats like SD and MEAN need
    # to be removed for clustering to work

    # discard UID column
    if (ncol(df) > 2){
        measurements <- measurements[, 2:ncol(measurements)]
    } else {
        measurements <- measurements[2]
    }

    measurements <- toZscores(measurements)
    
    
    if (is.na(significanceValues)) {
        jsn$warnings <- append(jsn$warnings, c("Significance sorting could not be done due to insufficient data"))
    }
    jsn <- addClusteringOutput(jsn, measurements) #
    jsn <- toJSON(jsn, pretty = TRUE, digits = I(17))
    writeDataForZip(df, measurements, patientIDs)  # for later zip generation
    write(jsn, file = "heatmap.json")
    # json file be served the same way
    # like any other file would - get name via
    # /status call and then /download

    msgs <- c("Finished successfuly")
    list(messages = msgs)
    
#     ## SE: For dev purposes
     # return(jsn)
}

## Check input args for heatmap 
verifyInput <- function(max_rows, sorting) {
    if (max_rows <= 0) {
        stop("Max rows argument needs to be higher than zero.")
    }
    if (!(sorting == "nodes" || sorting == "subjects")) {
        stop("Unsupported sorting type. Only nodes and subjects allowed")
    }
}


# # SE: For dev purposes we call the function here
# out = main()
# print(out)


