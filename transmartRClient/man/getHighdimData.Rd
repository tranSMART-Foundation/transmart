\name{getHighdimData}
\alias{getHighdimData}

\title{
Obtain high dimensional data for a certain study
}
\description{
This function will retrieve a highdimensional dataset for a specific concept of a certain study.   

%%  ~~ A concise (1-5 lines) description of what the function does. ~~
}

\usage{
getHighdimData(study.name, concept.match = NULL, concept.link = NULL, projection = NULL)
}
\arguments{
  \item{study.name}{A character string containing the name of the study}
  \item{concept.match}{This argument will search for the first concept that matches the value within the requested study. It uses the name column of the result from \code{getConcepts(study.name)} to perform the matching.}
  \item{concept.link}{A string containing the link pointing to the location of the data on the server. Candidate values for this argument can be found in the api.link column of the \code{getConcepts(study.name)} results. It is the most exact way to refer to a concept, and it overwrites the concept.match argument.}
  \item{projection}{
  A character string specifying what part of the dataset should be returned; should all data be returned or only a certain type of data, for example only the Log Values for an mRNA data set (the types of data present may differ between studies)? 
}
}
\details{
  Retrieving and parsing the data may take some time, depending on the size of the data requested and on your connection characteristics.
  
  A dataframe containing the high dimensional data will only be returned if a projection is specified.
  
  To discover what projections are possible call getHighDimData(study.name, concept.match) or getHighDimData(study.name, concept.link). 
  
    
}
\value{
 If a projection is specified, this function returns a list containing the highdimensional data. The first element of the list, called "data", is a dataframe containing the high dimensional data. If biomarker data is available for this particular dataset, the list also contains a hash (called "labelToBioMarkerMap") which describes which (column) labels refer to which bioMarker.
 
 If no projection is specified this function returns a list of the projections available for this study. No highdimensional data is returned.
 
}
\references{}
\author{Tim Dorscheidt, tim@thehyve.nl}
\note{}
\seealso{ \code{\link{hash}}.}

\examples{%% STILL NEED FILL THIS IN!!
##---- Should be DIRECTLY executable !! ----
##-- ==>  Define data, use random,
##--	or do  help(data=index)  for the standard data sets.


\keyword{database}
