\name{getSubjects}
\alias{getSubjects}
\title{
  Obtain a list of all subjects within a certain study.
}
\description{
  This function will retrieve a list of all subjects and their personal information for a certain
  study.
}
\usage{
getSubjects(study, as.data.frame = TRUE)
}

\arguments{
  \item{study}{a character string giving the name of the study}
  \item{as.data.frame}{logical. If TRUE (default setting) the subject information is returned as a data.frame. If set to FALSE the function returns the information as a list}
}
\details{}
\value{
  A data.frame (or list) is returned, whereby each row (or list element) contains the information partaining to
  one subject.
}
\references{}
\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen. 
Contact: development@thehyve.nl}
\note{}
\seealso{}
\examples{
  ##not run##
    # The following will retrieve a data.frame with subject-information for the study "foo"
    getSubjects("foo")
}

\keyword{ database }
