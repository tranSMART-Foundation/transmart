\name{transmart.getProbeGeneSNPMapping}
\alias{transmart.getProbeGeneSNPMapping}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
This function will get a lookup table of SNP Names/genes from the tranSMART database.
}
\description{
tranSMART has annotation information stored that can be queried by either the SNP name or gene id to get the appropriate mapping information between the two.
}
\usage{
transmart.getProbeGeneSNPMapping(probeIds = NA,geneIds = NA)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{probeIds}{
	A list of SNP identifiers to find the gene symbols for.
}
  \item{geneIds}{
	A list of genes to look up the SNP names for.
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
	byProbe <- transmart.getProbeGeneSNPMapping(probeIds = c('SNP_A-1855402'))
	byGene <- transmart.getProbeGeneSNPMapping(geneIds = c('MAPT'))
}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
