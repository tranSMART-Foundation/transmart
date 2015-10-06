library(jsonlite)
init <- function(){
    variableName <- c("expression","patients")
    variableType <- c("High-Dimension","Patient-Set")
    df <- data.frame(variableName,variableType)
    toJSON(df)
}
init()
