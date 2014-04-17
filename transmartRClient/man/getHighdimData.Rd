\name{getHighdimData}
\alias{getHighdimData}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
%%  ~~function to do ... ~~
}
\description{
%%  ~~ A concise (1-5 lines) description of what the function does. ~~
}
\usage{
getHighdimData(study.name, concept.match = NULL, concept.link = NULL, projection = NULL)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{study.name}{
%%     ~~Describe \code{study.name} here~~
}
  \item{concept.match}{
%%     ~~Describe \code{concept.match} here~~
}
  \item{concept.link}{
%%     ~~Describe \code{concept.link} here~~
}
  \item{projection}{
%%     ~~Describe \code{projection} here~~
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
function (study.name, concept.match = NULL, concept.link = NULL, 
    projection = NULL) 
{
    .checkTransmartConnection()
    if (is.null(concept.link) && !is.null(concept.match)) {
        studyConcepts <- getConcepts(study.name)
        conceptFound <- grep(concept.match, studyConcepts$name)[1]
        if (is.na(conceptFound)) {
            warning(paste("No match found for:", concept.match))
        }
        else {
            concept.link <- studyConcepts$api.link.self.href[conceptFound]
        }
    }
    if (length(concept.link) == 0L) {
        warning("No concepts selected or found to match your arguments.")
        return(NULL)
    }
    serverResult <- .transmartServerGetRequest(paste(concept.link, 
        "/highdim", sep = ""), accept.type = "hal")
    if (length(serverResult$dataTypes) == 0) {
        warning("This high dimensional concept contains no data.")
        return(NULL)
    }
    listOfHighdimDataTypes <- serverResult$dataTypes[[1]]
    if (!is.null(projection)) {
        matchingProjectionIndex <- which(names(listOfHighdimDataTypes$api.link) == 
            projection)
        if (length(matchingProjectionIndex) > 0) {
            projectionLink <- listOfHighdimDataTypes$api.link[[matchingProjectionIndex]]
        }
        else {
            projection <- NULL
        }
    }
    else {
        projection <- NULL
    }
    if (is.null(projection)) {
        cat("No valid projection selected.\nSet the projection argument to one of the following options:\n ")
        cat(paste(listOfHighdimDataTypes$supportedProjections, 
            "\n"))
        return(listOfHighdimDataTypes$supportedProjections)
    }
    cat("Retrieving data from server.", as.character(Sys.time()), 
        "\n")
    serverResult <- .transmartServerGetRequest(projectionLink, 
        accept.type = "binary")
    if (length(serverResult$content) == 0) {
        warning("Error in retrieving high dim data.")
        return(NULL)
    }
    return(.parseHighdimData(serverResult$content))
  }
}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
