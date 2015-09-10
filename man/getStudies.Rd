\name{getStudies}
\alias{getStudies}
\title{
  Obtain a list of studies present in the tranSMART database
}
\description{
  This function will retrieve a list of all studies contained within the currently accessed tranSMART database. A study-name filter is optional.
}
\usage{
getStudies(name.match = "", as.data.frame = TRUE, cull.columns = TRUE)
}
\arguments{
  \item{name.match}{a character string or regular expression by which the results are filtered. The function will select all studies which name contains the string or matches the regular expression that is supplied.}
  \item{as.data.frame}{logical. If TRUE (default setting) the subject information is coerced to a data.frame. If set to FALSE the function returns the information as a list}
  \item{cull.columns}{logical. If TRUE (default setting) several non-essential columns are excluded.}
}
\details{
  Depending on your connection characteristics, this may take some time.  
}
\value{
  A data.frame is returned, whereby each row corresponds to a study in the tranSMART database.
}
\references{}
\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen. 
Contact: development@thehyve.nl}
\note{To be able to access a transmart database, you need to be connected to the server the database is on. If you haven't connected to the server yet, establish a connection using the \code{\link{connectToTransmart}} function.}
\seealso{\code{\link{getConcepts}, \link{getSubjects}, \link{getObservations}, \link{getHighdimData}}.}
\examples{
  \dontrun{
  # Simply calling the function without an argument will retrieve
  # a data.frame with information on all studies
  getStudies()

  # Supplying a string will act as a filter on the study names,
  # only returning results which contain this string or match the 
  # regular expression. This example will only show studies that
  # start with "foo" in their name.
  getStudies("^foo")
  }
}

\keyword{ database }
\keyword{ transmart }
