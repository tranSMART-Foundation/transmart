\name{transmart.getGEXData}
\alias{transmart.getGEXData}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
This function gets Gene Expression data from the tranSMART database.
}
\description{
This function will retrieve Gene Expression data based on filters. Options are available to pivot and aggregate the data.
}
\usage{
transmart.getGEXData(study.list = NA, 
					gene.list = NA, 
					pathway = NA, 
					signature = NA, 
					patient.list = NA, 
					sample.type.list = NA, 
					tissue.type.list = NA, 
					timepoint.list = NA, 
					platform.list = NULL, 
					probe.list = NULL, 
					platform.removeOnOverlap = NULL, 
					show.genes = FALSE, 
					print.statement = FALSE, 
					data.pivot = TRUE, 
					data.pivot.aggregate = NA)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{study.list}{
	A list of studies to limit the text search to. This lookup is case insensitive.
}
  \item{gene.list}{
	A list of genes to filter the GEX by.
}
  \item{pathway}{
	A pathway (Exact text match, case insensitive.)
}
  \item{signature}{
	The name of a gene signature that was previously loaded into the tranSMART database.
}
  \item{patient.list}{
	A list of patient IDs to filter the data by. This is the patient identifier generated within tranSMART.
}
  \item{sample.type.list}{
	A list of sample types to filter the data by. The available sample types can be retrieved using the transmart.listHDDAttributes function.
}
  \item{tissue.type.list}{
	A list of tissue types to filter the data by. The available tissue types can be retrieved using the transmart.listHDDAttributes function.
}
  \item{timepoint.list}{
	A list of timepoints to filter the data by. The available timepoints can be retrieved using the transmart.listHDDAttributes function.
}
  \item{platform.list}{
	This is a list of platforms to filter on.
}
  \item{probe.list}{
	A list of probe ids to filter the results by.
}
  \item{platform.removeOnOverlap}{
	This is a list of platforms that you want to be *overlapped* if there are probes in multiple platforms. The script will check for probes in multiple platforms and remove the records that have a platform in the provided list.
}
  \item{show.genes}{
	If this is set to true a gene column will be added to data output.
}
  \item{print.statement}{
	If this is set to true the function will only print the SQL statement to retrieve the GEX data instead of running it.
}
  \item{data.pivot}{
	Flag indicating whether the GEX data should be pivoted or not.
}
  \item{data.pivot.aggregate}{
	Function to use when aggregating data during a pivot. Use this only if you know what you are aggregating.
}
  \item{data.pivot.patient_id}{
	This will use the tranSMART internal identifier which is guarenteed to be unique per study (barring any data issues).
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
	#---
	#Getting Gene Expression data by Study and a list of genes.
	gexData <- transmart.getGEXData(study.list = c('Gse10021'),
										gene.list=c('BrCA2'))
	#---
	
	#---
	#Getting Gene Expression data by study and a signature loaded into the 
	#tranSMART data warehouse.
	gexData <- transmart.getGEXData(study.list = c('GSE10021'),
								signature=c('Breast Cancer Lung Metastasis'))
	#---
	
	#---
	#Getting Gene Expression data by study and gene and using an aggregation function when 
	#pivoting. This is useful when there are multiple values per probe as in the case of 
	#some studies. If this function is not provided for those studies, a message warning 
	#the user about "length" being used will display and the returned data frame will 
	#almost certain contain funny looking data.
	gexData <- transmart.getGEXData(study.list = c('Gse10021'),
								gene.list=c('BrCA2'),
								data.pivot.aggregate = median)
	#---
	
	#---
	#Using get distinct concepts to generate a study list of public studies 
	#to get Gene Expression data for.
	breastCancerStudies <- transmart.getDistinctConcepts(
pathMatchList = c('\\\\Public Studies\\\\\%\\\\Cancer\\\\Breast Cancer\\\\\%Affymetrix'))
	
	#Pass in the list of studies, a list of genes, 
	#a function to aggregate the data with multiple records, 
	#a parameter to narrow down which platform to pull data for, 
	#and a boolean that will force the pivot to use 
	#our own unique internal ID as the patient ID.
	breastCancerData <- transmart.getGEXData(
		study.list=breastCancerStudies$STUDYCODE,
		gene.list=c('BRCA1','BRCA2'),
		platform.list = c('GPL96'),
		data.pivot.aggregate = median,
		data.pivot.patient_id = TRUE)	
	#---
	
	#---
	#Pull gene expression data for a study, for a given signature, 
	#with a platform filter but don't pivot it.
	gep.tmp.oneplat=transmart.getGEXData(study.list="GSE10021",
								signature="u133p2.gene.names",
								platform.list = c('GPL96'), 
								data.pivot = FALSE)
	#---
	
	#---
	#Pull gene expression data but remove any probes 
	#from GPL97 if they overlap with GPL96.
	gep.tmp.overlapremoved=transmart.getGEXData(study.list="GSE10021",
									platform.removeOnOverlap = c('GPL97'))
	#---
	
	#---
	#Pull gene expression by a probe id.
	gep.tmp.probelist = transmart.getGEXData(study.list="GSE10021",
							probe.list=c('201172_x_at','202068_s_at'))
	#---
	
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
