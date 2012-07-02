\name{transmart.getProbeGeneMapping}
\alias{transmart.getProbeGeneMapping}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
This function will get a lookup table of probes/genes from the tranSMART database.
}
\description{
tranSMART has annotation information stored that can be queried by either the probe or gene id to get the appropriate mapping information between the two.
}
\usage{
transmart.getProbeGeneMapping(probeIds)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{probeIds}{
	A list of probe identifiers to find the gene symbols for.
}
  \item{geneIds}{
	A list of genes to look up the probe ids for.
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
transmart.getProbeGeneMapping(probeIds = c('220665_at','220675_s_at','220730_at'))
transmart.getProbeGeneMapping(geneIds = c('LUZP4'))
}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
