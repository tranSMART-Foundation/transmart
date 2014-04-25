\name{getObservations}
\alias{getObservations}
\title{
  Obtain a list of observations for a certain study
}
\description{
  This function will retrieve a list of all observations for a certain
  study.
}
\usage{
getObservations(study.name, concept.match = NULL, concept.links = NULL, as.data.frame = TRUE)
}
\arguments{
  \item{study.name}{The name of the study}
  \item{concept.match}{This argument will search for and select the first concept that matches the value within the requested study. It uses the name column of the result from \code{getConcepts(study.name)} to perform the matching.}
  \item{concept.links}{Candidate values for this argument can be found in the api.link column of the \code{getConcepts(study.name)} results. It is the most exact way to refer to a concept, and it overwrites the concept.match argument.}
  \item{as.data.frame}{Set to TRUE if you wish the function to attempt conversion of
  the result to a data frame}
}
\details{


}
\value{
  When as.data.frame is set to true (default setting), a list is returned containing three data.frame objects.
  
  The first data.frame called 'observations' contains the actual clinical observation values. Each row corresponds to one subject, whereby the first column contains the subject.id. Every other column contains values pertaining to one concept.
  The second data.frame called 'subjectInfo' contains additional information on each of the subjects.
  The third data.frame called 'conceptInfo' maps the column names of the 'observations' data.frame to their corresponding concept labels as they are defined within tranSMART.
  
  If as.data.frame is set to false, a list is contained with a structure very similar to how it is received from the server.
}
\references{}
\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen. 
Contact: development@thehyve.nl}
\note{To be able to access a transmart database, you need to be connected to the server the database is on. If you haven't connected to the server yet, establish a connection using the \code{\link{connectToTransmart}} function.}
\seealso{}
\examples{
\dontrun{
  # The following will retrieve a list with observations for the study "foo"
  getObservations("foo")
  }
}

\keyword{ database }
