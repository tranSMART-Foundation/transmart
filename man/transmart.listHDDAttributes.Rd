\name{transmart.listHDDAttributes}
\alias{transmart.listHDDAttributes}
\title{
	List High Dimensional Data Attributes within tranSMART.
}
\description{
	For a given list of studies this function will query the table that maps patients to samples. Returned is a list of 3 attribute types and their distinct values. These values can be used to filter in other functions, like retrieving gene expression data by a sample type.
}
\usage{
transmart.listHDDAttributes(studyList)
}
\arguments{
  \item{studyList}{
	A list of studies to retrieve the mapping data for.
}
}
\details{
%%  ~~ If necessary, more details than the description above ~~
}
\value{
A list of 3 data frames, each frame is a distinct list of a sample attribute type.
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
	#---
	#Pull gene expression based on Sample Types.
	
	#This gets a list of all public studies.
	publicStudyList <- transmart.listStudies('GSE\%')
	
	#This gets the attributes for all those studies.
	attr=transmart.listHDDAttributes(studyList = publicStudyList$STUDYCODE)
	
	#Pull the Sample type into its own frame.
	sampleTypes <- data.frame(attr[1])
	
	#Pull all the BRCA2 GEX data for the Fifth SAMPLE_TYPE.
	gexData <- transmart.getGEXData(study.list = publicStudyList$STUDYCODE,
					sample.type.list = sampleTypes$SAMPLE_TYPE[5],
					gene.list=c('BrCA2'))
	#---

}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
