\name{tranSMART.DB.connection}
\alias{tranSMART.DB.connection}
\docType{data}
\title{
This variable holds the connection object to the oracle database.
}
\description{
This object is initialized before every command, creating the connection to the database.
}
\usage{tranSMART.DB.connection <- tranSMART.DB.establishConnection()}
\format{
%%  ~~ If necessary, more details than the __description__ above ~~
}
\details{
%%  ~~ If necessary, more details than the __description__ above ~~
}
\source{
%%  ~~ reference to a publication or URL from which the data were obtained ~~
}
\references{
%%  ~~ possibly secondary sources and usages ~~
}
\examples{
##Establish a connection to a database 
##(Assuming the server,username and password variables have been created.)
tranSMART.DB.connection <- tranSMART.DB.establishConnection()
}
\keyword{datasets}
