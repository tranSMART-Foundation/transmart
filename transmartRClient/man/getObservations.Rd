\name{getObservations}
\alias{getObservations}
\title{
  Obtain a list of observations for a certain study
}
\description{
  This function will retrieve a list of all observations for a certain
  study, and optionally for a specific subject within that study
}
\usage{
getObservations(studyName, subjectID = NULL, as.data.frame = FALSE)
}
\arguments{
  \item{study}{The name of the study}
  \item{subjectID}{The ID of the subject}
  \item{as.data.frame}{Set to TRUE if you wish the function to attempt conversion of
  the result to a data frame}
}
\details{}
\value{
  A list is returned containing all the observations for the study 
  (and for the specific subject if requested)
}
\references{}
\author{Tim Dorscheidt, tim@thehyve.nl}
\note{}
\seealso{}
\examples{
  # The following will retrieve a list with observations for the study "foo"
  getObservations("foo")
}

\keyword{ database }
