\name{getSubjects}
\alias{getSubjects}
\title{
  Obtain a list of all subjects within a certain study
}
\description{
  This function will retrieve a list of all subjects for a certain
  study, including the personal information available per subject.
}
\usage{
getSubjects(study, as.data.frame = TRUE)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{study}{The name of the study}
  \item{as.data.frame}{Set to FALSE if you wish the function to return the result as a list}
}
\details{}
\value{
  A data.frame is returned, whereby each row contains the information partaining to
  one subject.
}
\references{}
\author{Tim Dorscheidt, tim@thehyve.nl}
\note{}
\seealso{}
\examples{
  # The following will retrieve a data.frame with subject-information for the study "foo"
  getSubjects("foo")
}

\keyword{ database }
