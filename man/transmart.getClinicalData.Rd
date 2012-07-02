\name{transmart.getClinicalData}
\alias{transmart.getClinicalData}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
This function returns Clinical Data from the tranSMART database.
}
\description{
Clinical Data (aka low dimensional data) represents clinical parameters that have been collected about patients/samples. These things could include medical history, demographics, or low complexity assays. In order to retrieve this data the user must supply a list of codes which can be retrieved using the getDistinctConcepts function.
}
\usage{
transmart.getClinicalData(concepts.codelist, 
							data.pivot = TRUE, 
							concepts.prePivotTrim = TRUE, 
							concepts.trimLengths = 4, 
							sql.print = FALSE)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{concepts.codelist}{
	A list of concept cds. This would ideally be passed in from the return of the getDistinctConcepts function.
}
  \item{data.pivot}{
	A boolean indicating whether the data should be pivoted after retrieving.
}
  \item{concepts.prePivotTrim}{
	This boolean indicates if the concept paths should be trimmed before the pivot occurs. A concept path is a "\\" seperated string indicating a concepts relation to other concepts. The path is laid out visually in the dataset explorer tree within tranSMART. By trimming items off before pivoting columns can be collapsed. An example is when multiple studies worth of data are retrieved but the path is trimmed so that the survival time node from all 4 studies has the same concept code. When the data is pivoted all the surival times will be put into one column.
}
  \item{concepts.trimLengths}{
	This is the number of "\\" seperated elements to remove from the concept path when trimming. A negative trim length will leave items from the end of the path while positive lengths remove from the begining.
}
  \item{sql.print}{
	Not used as this time.
}
}
\details{
%%  ~~ If necessary, more details than the description above ~~
}
\value{
A data frame is returned with a patient num (internal identifier), Subject ID (ID that is taken from the raw input files when data is loaded into tranSMART), Trial Name (Name of the study) and the remaining columns are the clinical data items. If the data is not pivoted, it will be represented in a long format instead of a wide one.
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

conceptList <- transmart.getDistinctConcepts(studyList = c('GSE5287'),
												pathMatchList = c('\%Overall\%'))
clinicalData <- transmart.getClinicalData(concepts.codelist = conceptList$CONCEPT_CD,
											concepts.trimLengths = -1)

}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
