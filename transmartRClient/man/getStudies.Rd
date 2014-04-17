\name{getStudies}
\alias{getStudies}
\title{
  Obtain a list of studies present in the tranSMART database
}
\description{
  This function will retrieve a list of all studies contained within
  the currently accessed tranSMART database. A study-name filter is
  optional.
}
\usage{
getStudies(name.match = "", as.data.frame = TRUE, cull.columns = TRUE)
}
\arguments{
  \item{name.match}{A string or regular expression by which the results are filtered}
  \item{as.data.frame}{Set to TRUE if you wish the function to attempt conversion of
  the result to a data frame}
  \item{cull.columns}{Several non-essential columns are excluded by default.}
}
\details{
  Depending on your connection characteristics, this make take some time.
}
\value{
  A data.frame is returned, whereby each row corresponds to a study in the tranSMART database.
}
\references{}
\author{Tim Dorscheidt, tim@thehyve.nl}
\note{}
\seealso{}
\examples{
  # Simply calling the function without an argument will retrieve
  # a data.frame with information on all studies
  getStudies()

  # Supplying a string will act as a filter on the study names,
  # only returning results which contain this string or match the 
  # regular expression. This example will only show studies that
  # start with "foo" in their name.
  getStudies("^foo")
}

\keyword{ database }
