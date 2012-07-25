\name{transmart.getSNPData}
\alias{transmart.getSNPData}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
This function gets SNP data from the tranSMART database.
}
\description{
This function will retrieve SNP data based on filters. Options are available to pivot and aggregate the data.
}
\usage{
transmart.getSNPData(study.list = NA, 
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
					data.CN.pivot.aggregate = NA, 
					data.GT.pivot.aggregate = NA, 
					data.pivot.patient_id = FALSE)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{study.list}{
	A list of studies to limit the text search to. This lookup is case insensitive.
}
  \item{gene.list}{
	A list of genes to filter the SNP by.
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
	A list of sample types to filter the data by. The available sample types can be retrieved using another function.
}
  \item{tissue.type.list}{
	A list of tissue types to filter the data by. The available tissue types can be retrieved using another function.
}
  \item{timepoint.list}{
	A list of timepoints to filter the data by. The available timepoints can be retrieved using another function.
}
  \item{platform.list}{
	This is a list of platforms to filter on.
}
  \item{probe.list}{
	A list of probe ids to filter the results by.
}
  \item{show.genes}{
	If this is set to true a gene column will be added to data output.
}
  \item{print.statement}{
	If this is set to true the function will only print the SQL statement to retrieve the SNP data instead of running it.
}
  \item{data.pivot}{
	Flag indicating whether the SNP data should be pivoted or not.
}
  \item{data.CN.pivot.aggregate}{
	Function to use when aggregating data during a pivot. Use this only if you know what you are aggregating.
}
  \item{data.GT.pivot.aggregate}{
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

If unpivoted, a single data frame is returned. If pivoted, a list of data frames is returned, one for Copy Number and one for Genotype.

\item{DF1 }{Copy Number Data}
\item{DF2 }{Genotype Data}
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
	#Getting SNP Data for two studies, only probes in one gene, not pivoting the data.
	unpivotSNP <- transmart.getSNPData(study.list = c('GSE1234','GSE12345'),
											gene.list=c('BRCA2'),data.pivot = FALSE)
	
	#Getting SNP Data by a list of genes for one study.
	byGeneSNP <- transmart.getSNPData(study.list = c('GSE1234'),
											gene.list=c('BRCA2','BRCA1'))
	
	#Getting SNP data by a signature for one study.
	bySigSNP <- transmart.getSNPData(study.list = c('GSE1234'),
											signature='some.gene.names')
	
	#Getting SNP Data by a signature for multiples studies, forcing the pivot 
	#to use our internal ID.
	bySigSNP <- transmart.getSNPData(study.list = c('GSE1234','GSE12345'),
						signature='some.gene.names',
						data.pivot.patient_id = TRUE)
	
	#Getting SNP data by a pathway.
	byPath <- transmart.getSNPData(study.list = c('GSE12345'),pathway='metabolism')
	
	#Getting SNP data by a list of probes.
	byProbe <- transmart.getSNPData(study.list = c('GSE1234'),
						probe.list=c('SNP_A-1827941','SNP_A-4203012','SNP_A-1869849'))
	
	#Getting SNP data by probes, using our internal ID.
	byProbePatient <- transmart.getSNPData(study.list = c('GSE1234'),
						probe.list=c('SNP_A-1827941','SNP_A-4203012','SNP_A-1869849'),
						data.pivot.patient_id = TRUE)
	
	#Getting SNP data with a gene column.
	bySigSNPFullParameters <- transmart.getSNPData(study.list = c('GSE1234'),
						signature='u133p2.gene.names',show.genes = TRUE)
	
	#Example of pulling two data frames from list.
	CNV <- data.frame(byProbePatient[1])
	GENO <- data.frame(byProbePatient[2])

}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
