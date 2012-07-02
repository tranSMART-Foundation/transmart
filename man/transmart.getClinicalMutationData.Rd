\name{transmart.getClinicalMutationData}
\alias{transmart.getClinicalMutationData}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
This functions performs a very specific search for a Mutation Type concept within the ontology tree.
}
\description{
In order to pull a list of the mutations that a patient has a search is done on the ontology to find the concept that represents the mutations on the genes specified. A data frame is constructed with has a column per supplied gene and a row per patient in the studies supplied. The intersection lists the concept (if one exists).
}
\usage{
transmart.getClinicalMutationData(study.list,gene.list,trimLength = 4)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{study.list}{
A list of studies to limit the text search to. This lookup is case insensitive.
}
  \item{gene.list}{
A list of genes to look for mutations for.
}
  \item{trimLength}{
This is the length of the resulting concept code at a patient/gene intersection.
}
}
\details{
%%  ~~ If necessary, more details than the description above ~~
}
\value{
A data frame is returned with PATIENT_ID (Internal Identifier), SUBJECT_ID (Identifier from source data), STUDY ID (Internal ID) and a column for each gene specified. In each column is the result of searching to see if that patient was associated with a mutation in that gene. If NA is present that means the concept was not associated with this patient (ie. they do not have the mutation).
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

	mutData <- transmart.getClinicalMutationData(
								study.list = c('VELCADE024','VELCADE025'),
								gene.list=c('NRAS','BRAF','MET'),
								trimLength = 4)

}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
