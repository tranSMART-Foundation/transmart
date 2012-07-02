\name{transmart.getGeneGoMembership}
\alias{transmart.getGeneGoMembership}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
Retrieve GeneGo pathway information from the tranSMART database.	
}
\description{
This function will take a pathway name and return all the genes in that pathway. Optionally the user can download all the pathway information by leaving the parameter blank.
}
\usage{
transmart.getGeneGoMembership(genegoName = NA)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{genegoName}{
	The name of the GeneGo pathway to look up genes for.
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

	allResults <- transmart.getGeneGoMembership()
	byPathway <- transmart.getGeneGoMembership(
				genegoName = c('alanine, cysteine, and L-methionine metabolism'))
	
}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
