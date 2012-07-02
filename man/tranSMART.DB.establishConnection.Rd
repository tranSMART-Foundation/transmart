\name{tranSMART.DB.establishConnection}
\alias{tranSMART.DB.establishConnection}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
Function to establish the connection to the tranSMART DB.
}
\description{
Using tranSMART.DB.username,tranSMART.DB.password,tranSMART.DB.dbname a connection to the tranSMART data warehouse will be established and returned.
}
\usage{
tranSMART.DB.establishConnection()
}
%- maybe also 'usage' for other objects documented here.
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

tranSMART.DB.username <- 'someuser'
tranSMART.DB.password <- 'somepassword'
tranSMART.DB.dbname <- 'somedb'

tranSMART.DB.connection <- tranSMART.DB.establishConnection()

}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
