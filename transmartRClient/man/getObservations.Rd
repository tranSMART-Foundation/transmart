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
getObservations(study.name, concept.match = NULL, concept.links = NULL, as.data.frame = TRUE)
}
\arguments{
  \item{study.name}{The name of the study}
  \item{concept.match}{This argument will search for the first concept that matches the value within the requested study. It uses the name column of the result from \code{getConcepts(study.name)} to perform the matching.}
  \item{concept.links}{Candidate values for this argument can be found in the api.link column of the \code{getConcepts(study.name)} results. It is the most exact way to refer to a concept, and it overwrites the concept.match argument.}
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
