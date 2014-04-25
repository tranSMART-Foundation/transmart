\name{getHighdimData}
\alias{getHighdimData}

\title{
Obtain a high dimensional dataset for a certain study
}
\description{
This function will retrieve a highdimensional dataset for a specific concept of a certain study.   
It retrieves the data from the server and parses it, converting the high dimensional data to a data.frame.
}

\usage{
getHighdimData(study.name, concept.match = NULL, concept.link = NULL, projection = NULL)
}
\arguments{
  \item{study.name}{A character string containing the name of the study}
  \item{concept.match}{ A character string containing the concept name that should be matched. The \code{getHighdimData} function will search within the requested study for the first concept which name matches the given string value. It uses the name column of the result from \code{\link{getConcepts}(study.name)} to perform the matching.}
  \item{concept.link}{A character string containing the link pointing to the location of the data on the server. Candidate values for this argument can be found in \code{the api.link} column of the \code{\link{getConcepts}(study.name)} results. It is the most exact way to refer to a concept, and it overwrites the \code{concept.match} argument.}
  \item{projection}{
  A character string specifying what part of the dataset should be returned: should all data be returned or only a certain type of data, for example only the Log Values for an mRNA data set? (note: the types of data present may differ between studies) 
}
}
\details{
  Retrieving and parsing the data may take some time, depending on the size of the data requested and on your connection characteristics.
  
  A dataframe containing the high dimensional data will only be returned if a projection is specified.
  
  To discover what projections are possible call \code{getHighDimData}, while only providing a study name and concept, i.e. \code{getHighdimData(study.name, concept.match)} or \code{getHighDimData(study.name, concept.link)}. 
  
    
}
\value{
 If a projection is specified, this function returns a list containing the highdimensional data, with the following components: 
 \item{data}{a dataframe containing the high dimensional data.}
 \item{labelToBioMarkerMap}{a hash describing which (column) labels refer to which bioMarker. This component will only be present if biomarker data is available for this particular dataset. See \code{\link{hash}} for more details about how to use hashes.}
 
If no projection is specified this function returns a list of the projections available for the requested study. No highdimensional data is returned. It will also print the instruction to set the projection argument and a list of the projections that are available.
 
}
\references{}
\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen. 
Contact: development@thehyve.nl}
\note{To be able to access a transmart database, you need to be connected to the server the database is on. If you haven't connected to the server yet, establish a connection using the \code{\link{connectToTransmart}} function.}
\seealso{ \code{\link{hash}}}.}

\examples{
\dontrun{

  # if a concept contains high dimensional data, use the following command to       obtain this data:
  getHighdimData(study.name = "GSE8581", concept.match = "Lung")

  # you will be told that one of the listed projections needs to be selected: 
  "No valid projection selected.
  Set the projection argument to one of the following options:
    default_real_projection 
    zscore 
    log_intensity 
    all_data"
 
  # the following will return the actual data:
  data <- getHighdimData(study.name = "GSE8581", concept.match = "Lung", projection = "zscore")
  }
}


\keyword{database}
