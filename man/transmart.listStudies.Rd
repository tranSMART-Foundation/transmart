\name{transmart.listStudies}
\alias{transmart.listStudies}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
This function will list studies that have a character match with the supplied strings.
}
\description{
This function uses the passed in search string (With \% as wildcards) to generate a list of studies and their concept codes. The STUDYCODE column can be used to identify studies within the other R tranSMART functions. The second parameter is used to trim the concept path to collapse records.
}
\usage{
transmart.listStudies(studyLike,concept.size = 4, gexFlag = FALSE)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
	\item{studyLike}{
	Text string to search for. % is used as a wildcard character.
}  
	\item{concept.size}{
	The number of "chunks" to keep in the concept path, the number actually reflects the number of "\\" delimeters to keep. Default is 4.
}
	\item{gexFlag}{
	If used this flag will return a count from the microarray table of distinct probe ids for the given study. This will indicate if GEX data is available in the study.
}
}
\details{
%%  ~~ If necessary, more details than the description above ~~
}
\value{
A data.frame is returned with a STUDYCODE and CONCEPT_PATH column. Both of these come from the table that is used to generate the tree within the Dataset Explorer. The concepts are grouped by the Study Code and the Concept Path. You can pass the STUDYCODE column to the other tranSMART R functions as a study filter.
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

	transmart.listStudies('GSE1079\%',concept.trim = 5)

}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
