\name{tranSMART.DB.username}
\alias{tranSMART.DB.username}
\docType{data}
\title{
Username for the tranSMART database login.
}
\description{
%%  ~~ A concise (1-5 lines) description of the dataset. ~~
}
\usage{data(tranSMART.DB.username)}
\format{
  The format is:
 chr "somedb_user"
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
