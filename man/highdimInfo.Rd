\name{highdimInfo}
\alias{highdimInfo}

\title{
Show information on a high dimensional dataset
}
\description{
This function will retrieve and return a nested structure containing the high dimensional data description for a given concept.
}

\usage{
highdimInfo(study.name =  NULL, concept.match = NULL, concept.link = NULL)
}
\arguments{
You must specify either \code{concept.link} or \code{study.name} and \code{concept.match}.
  \item{study.name}{a character string giving the name of a study.}
  \item{concept.match}{a character string containing the concept name that should be matched. The function will search within the requested study for the first concept of which the name matches the given string value. It uses the name column of the result from \code{\link{getConcepts}} to perform the matching.}
  \item{concept.link}{a character string containing the link pointing to the location of the data on the server. Candidate values for this argument can be found in \code{api.link.self.href} column of the \code{\link{getConcepts}} results. It is the most exact way to refer to a concept. If this is specified, \code{study.name} and \code{concept.match} are ignored.}
}
\details{
  This function returns information on a high dimensional data concept. The description retrieved from the server is returned to the caller as-is, so the exact structure depends on the server you are connecting to.
}
\value{
  A named list. The first level contains an item for every high dimensional data type that this concept supports. Usually that will be just one.

  For every such data type, there is again a list with several names:

  \item{supportedProjections}{A character vector of supported projections for this concept.}
  \item{supportedDataConstraints}{A character vector of supported data constraints for this concept.}
  \item{supportedAssayConstraints}{A character vector of supported assay constraints for this concept.}
  \item{name}{The name of this data type. This is the same as the name of the top level list.}
  \item{assayCount}{The number of assays in this data type.}
  \item{api.link}{This contains a nested structure with links to related resources. For example there is \code{self$href} that points to the current resource, and \code{<projection_name>$href} that is the url for downloading data for that projection.}

}
\references{}
\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen.
Contact: development@thehyve.nl}
\note{To be able to access a transmart database, you need to be connected to the server the database is on. If you haven't connected to the server yet, establish a connection using the \code{\link{connectToTransmart}} function.}
\seealso{\code{\link{hash}, \link{getHighdimData}, \link{getStudies}, \link{getConcepts}}.}

\examples{
\dontrun{

  # if a concept contains high dimensional data, use the following command to see a description of that data:
  info <- highdimInfo(study.name = "GSE8581", concept.match = "Lung")
  }
}


\keyword{ database }
\keyword{ transmart }
