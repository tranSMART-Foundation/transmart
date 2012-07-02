\name{transmart.getPatientMapping}
\alias{transmart.getPatientMapping}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
	This function gets a data frame that contains a mapping between tranSMART unique ID's and the Subject IDs supplied in the curation process.
}
\description{
tranSMART assigns unique IDs to patient records in addition to the identifier that is carried over from the data loading process. This function will get the mapping for a list of studies.
}
\usage{
transmart.getPatientMapping(studyList)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{studyList}{
	A list of studies to get the patient IDs for. Patient IDs are unique across studies where the ID provided in the data loading process may not be.
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

	patientMappingFrame <- transmart.getPatientMapping(studyList = c('Gse10021'))
	
}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
