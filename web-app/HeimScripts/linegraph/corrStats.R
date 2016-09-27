main <- function(info) {
    save(loaded_variables, file="~/loaded_variables.Rda")
    save(info, file="~/info.Rda")
    save(runResults, file="~/runResults.Rda")
    df <- runResults$data_matrix
    # we only care about categoric data in this script
    cat.df <- df[df$type == "categoric", ]

    patientIDs <- unique(cat.df$patientID)
    bioMarkers <- unique(cat.df$bioMarker)
    timeIntegers <- unique(cat.df$timeInteger)

    # create binary vector that represents occurence of info$bioMarker at info$timePoint for every patient
    time.df <- cat.df[cat.df$timeInteger == info$timeInteger, ]
    bin.vec_1 = as.numeric(vapply(patientIDs,
                                  function(patientID) any(time.df[time.df$patientID == patientID, ]$bioMarker == info$bioMarker),
                                  logical(length=1)))
    # do the same for every other timepoint and biomarker to compute correlations
    output <- data.frame(bioMarker=character(), timeInteger=integer(), corrCoef=numeric(), pValue=numeric())
    for (bioMarker in bioMarkers[bioMarkers != info$bioMarker]) {
        for (timeInteger in timeIntegers[timeIntegers > info$timeInteger]) {
            time.df <- cat.df[cat.df$timeInteger == timeInteger, ]
            bin.vec_2 = as.numeric(vapply(patientIDs,
                                          function(patientID) any(time.df[time.df$patientID == patientID, ]$bioMarker == bioMarker),
                                          logical(length=1)))
            test <- cor.test(bin.vec_1, bin.vec_2, method="pearson")
            output <- rbind(output, data.frame(bioMarker=bioMarker, timeInteger=timeInteger, corrCoef=as.numeric(test$estimate), pValue=test$p.value))
        }
    }
    json <- toJSON(output, digits=I(17))
    return(json)
}
