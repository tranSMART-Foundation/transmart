\name{transmart.getDistinctConcepts}
\alias{transmart.getDistinctConcepts}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
This function finds clinical concepts in the tranSMART database that match the supplied strings.
}
\description{
Before clinical data can be retrieved from the tranSMART warehouse the user must find the concept codes associated with the data to be retrieved. This function will perform a search of the concept paths within tranSMART, restricted to a list of studies.
}
\usage{
transmart.getDistinctConcepts(studyList = NULL, pathMatchList)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{studyList}{
A list of studies to limit the text search to. This lookup is case insensitive. If this is not supplied the results will be for all studies.
}
  \item{pathMatchList}{
A list of strings to search on. The wildcard character will be placed before and after each term when looking for a matching concept path. This search term is case insensitive.
}
}
\details{
%%  ~~ If necessary, more details than the description above ~~
}
\value{
A data frame is returned with a column for the concept code (Which will be used in the getClinicalData function), the concept path and a count of the number of patients who have that concept. A column with the study ID is also supplied.
}
\references{
%% ~put references to the literature/web site here ~
}
\author{
%%  ~~who you are~~
}
\note{
%%  ~~further notes~~
}

%% ~Make other sections like Warning with \section{Warning }{....} ~

\seealso{
%% ~~objects to See Also as \code{\link{help}}, ~~~
}
\examples{

#Assumes a connection to the tranSMART DB has been made.

conceptList <- transmart.getDistinctConcepts(studyList = c('GSE20685'),
											pathMatchList = c('duration'))
clinicalData <- transmart.getClinicalData(concepts.codelist = conceptList$CONCEPT_D)

}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
