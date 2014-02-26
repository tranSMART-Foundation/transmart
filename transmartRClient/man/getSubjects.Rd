\name{getSubjects}
\alias{getSubjects}
\title{
  Obtain a list of all subjects within a certain study
}
\description{
  This function will retrieve a list of all subjects for a certain
  study, including the clinical information available per subject.
}
\usage{
getSubjects(study)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{study}{The name of the study}
}
\details{}
\value{
  A list is returned, whereby each element contains the information partaining to
  one subject.
}
\references{}
\author{Tim Dorscheidt, tim@thehyve.nl}
\note{}
\seealso{}
\examples{
  # The following will retrieve a list with subject-information for the study "foo"
  getSubjects("foo")
}

\keyword{ database }
