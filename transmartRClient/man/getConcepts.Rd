\name{getConcepts}
\alias{getConcepts}

\title{
Obtain a list of all concepts for a certain study
}
\description{
This function will a retrieve a list of all the concepts for a certain study. This list contains for each concept the name and full name of the concept and the link to the location of this concept on the server.

Concepts are used in tranSMART to organise the stored study data. They are the folders and nodes on the left-hand side in tranSMART's data explorer.
There can be clinical concepts such as age, diagnosis, blood pressure, and high dimensional concepts such as mRNA expression values or copy number variation.

}
\usage{
getConcepts(study.name, as.data.frame = TRUE, cull.columns = TRUE)
}

\arguments{
  \item{study.name}{A character string giving the name of the study}
  \item{as.data.frame}{logical: should the list containing the concept information be converted to a dataframe?}
  \item{cull.columns}{logical. If TRUE several non-essential columns are excluded.}
}
\details{
%%  ~~ If necessary, more details than the description above ~~
}
\value{
If  high dimensional data is available an extra column with ... is available... hoe kan je dat herkennen?
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
