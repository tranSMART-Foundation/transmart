\name{tranSMART.DB.password}
\alias{tranSMART.DB.password}
\docType{data}
\title{
Password for the tranSMART database login.
}
\description{
When the tranSMART packages tries to connect to the database, this will be used in the connection string.
}
\usage{data(tranSMART.DB.password)}
\format{
  The format is:
 chr "somedb_password"
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
tranSMART.DB.username <- 'someuser'
tranSMART.DB.password <- 'somepassword'
tranSMART.DB.dbname <- 'somedb'

tranSMART.DB.connection <- tranSMART.DB.establishConnection()
}
