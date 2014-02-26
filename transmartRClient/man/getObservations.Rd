\name{getObservations}
\alias{getObservations}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
%%  ~~function to do ... ~~
}
\description{
  This function will retrieve a list of all observations for a certain
  study, and optionally for a specific subject within that study
}
\usage{
getObservations(studyName, subjectID = NULL, as.data.frame = FALSE)
}
%- maybe also 'usage' for other objects documented here.
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
}
\references{}
\author{Tim Dorscheidt, tim@thehyve.nl}
\note{}
\seealso{}
\examples{
##---- Should be DIRECTLY executable !! ----
##-- ==>  Define data, use random,
##--	or do  help(data=index)  for the standard data sets.

## The function is currently defined as
function (studyName, subjectID = NULL, as.data.frame = FALSE) 
  # The following will retrieve a list with observations for the study "foo"
  getObservations("foo")
}

\keyword{ database }
