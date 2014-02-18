getStudies <- function(nameMatch = "") {
  .checkRClientEnvironment()
  listOfStudies <- RClientEnv$serverGetRequest("/studies")

  studyNames <- sapply(listOfStudies, FUN = function(x) { x[["name"]] })
  names(listOfStudies) <- studyNames
  listOfStudies[ grep(nameMatch, studyNames) ]
}