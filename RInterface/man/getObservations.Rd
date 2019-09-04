\name{getObservations}
\alias{getObservations}
\title{
  Obtain the observation values for the clinical data of a certain study
}
\description{
  This function will retrieve the observation values for the clinical data of a certain
  study. A subset of observations can be selected by filtering by concept.
}
\usage{
getObservations(study.name, concept.match = NULL, concept.links = NULL, as.data.frame = TRUE)
}
\arguments{
  \item{study.name}{a character string giving the name of a study.}
  \item{concept.match}{a character string or character vector containing the concept name(s) that should be matched. For each vector element, the \code{getObservations} function will search within the requested study for the first concept which name contains the given character string. It uses the name column of the result from \code{\link{getConcepts}} to perform the matching.}
  \item{concept.links}{ a character string or a character vector containing the link(s) pointing to the locations of the chosen concepts on the server. Candidate values for this argument can be found in the \code{api.link.self.href} column of the \code{\link{getConcepts}} results. It is the most exact way to refer to a concept, and it overwrites the \code{concept.match} argument.}
  \item{as.data.frame}{logical (default setting is TRUE): should the list containing the observation values be converted to a dataframe?}
}
\details{


}
\value{
  When as.data.frame is set to TRUE (default setting), a list is returned containing three data.frame objects: 
  \item{observations}{a dataframe containing the actual clinical observation values. Each row corresponds to one subject, whereby the first column contains the subject.id. Each other column contains values pertaining to a single concept.}
  \item{subjectInfo}{a dataframe containing additional information on each of the subjects.}
  \item{conceptInfo}{a dataframe that maps the column names of the '\code{observations}' data.frame to their corresponding concept labels as they are defined within tranSMART.}
  
  If as.data.frame is set to FALSE, a list is contained with a structure very similar to how it is received from the server.
}
\references{}
\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen. Contact: development@thehyve.nl}
\note{To be able to access a transmart database, you need to be connected to the server the database is on. If you haven't connected to the server yet, establish a connection using the \code{\link{connectToTransmart}} function.}
\seealso{\code{\link{getStudies}, \link{getConcepts}}}
\examples{
\dontrun{
  # The following will retrieve a list with observations for the study "foo"
  getObservations("foo")
  }
}

\keyword{ database }
\keyword{ transmart }
