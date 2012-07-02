\name{transmart.listSearchTerms}
\alias{transmart.listSearchTerms}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
	Unfinished : This function mimics the search interface within tranSMART.
}
\description{
%%  ~~ A concise (1-5 lines) description of what the function does. ~~
}
\usage{
transmart.listSearchTerms(term, category = NA, terms.count)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{term}{
%%     ~~Describe \code{term} here~~
}
  \item{category}{
%%     ~~Describe \code{category} here~~
}
  \item{terms.count}{
%%     ~~Describe \code{terms.count} here~~
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
##---- Should be DIRECTLY executable !! ----
##-- ==>  Define data, use random,
##--	or do  help(data=index)  for the standard data sets.

## The function is currently defined as
function (term, category = NA, terms.count) 
{
    searchQuery <- gsub("\n", "", "SELECT DISTINCT t.searchKeyword, \n                                                t.keywordTerm, \n                                                t.rank\n                                FROM search.SearchKeywordTerm t \n                                WHERE t.keywordTerm LIKE ? || '\%' \n\t\t\t\t   ")
    if (!is.na(category)) {
        searchQuery <- paste("AND t.searchKeyword.dataCategory IN ('", 
            category, "')")
    }
    searchQuery <- gsub("\\?", term, searchQuery)
    rs <- dbSendQuery(tranSMART.DB.connection, searchQuery)
    dataToReturn <- fetch(rs)
    dataToReturn
  }
}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
