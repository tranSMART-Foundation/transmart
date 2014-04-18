\name{getConcepts}
\alias{getConcepts}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
Obtain a list of all concepts for a certain study %%  ~~function to do ... ~~
}
\description{
 
%%  ~~ A concise (1-5 lines) description of what the function does. ~~
}
\usage{
getConcepts(study.name, as.data.frame = TRUE, cull.columns = TRUE)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{study.name}{
%%     ~~Describe \code{study.name} here~~
}
  \item{as.data.frame}{
%%     ~~Describe \code{as.data.frame} here~~
}
  \item{cull.columns}{
%%     ~~Describe \code{cull.columns} here~~
}
}
\details{
%%  ~~ If necessary, more details than the description above ~~
}
\value{
%%  ~Describe the value returned
%%  If it is a LIST, use
%%  \item{comp1 }{Description of 'comp1'}
%%  \item{comp2 }{Description of 'comp2'}
%% ...
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
##---- Should be DIRECTLY executable !! ----
##-- ==>  Define data, use random,
##--	or do  help(data=index)  for the standard data sets.

## The function is currently defined as
function (study.name, as.data.frame = TRUE, cull.columns = TRUE) 
{
    .checkTransmartConnection()
    serverResult <- .transmartServerGetRequest(paste("/studies/", 
        study.name, "/concepts", sep = ""), accept.type = "hal")
    listOfConcepts <- serverResult$ontology_terms
    if (as.data.frame) {
        dataFrameConcepts <- .listToDataFrame(listOfConcepts)
        if (cull.columns) {
            columnsToKeep <- match(c("name", "fullName", "api.link.self.href"), 
                names(dataFrameConcepts))
            if (all(is.na(columnsToKeep))) {
                warning("Nothing remains after culling columns.")
                return(NULL)
            }
            return(dataFrameConcepts[, columnsToKeep])
        }
        return(dataFrameConcepts)
    }
    listOfConcepts
  }
}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
