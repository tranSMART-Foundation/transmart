\name{getTransmartToken}
\alias{getTransmartToken}
\title{
  Retrieve the authentication token that can be used to connect to Transmart
}
\description{
  This function returns a token that can be used to authenticate to Transmart. In OAuth parlance, it is the refresh token. It is possible to store this token in an R script so that script can connect to Transmart without requiring interactive authentication. See \code{\link{connectToTransmart}} for details. Note that Transmart only issues refresh tokens since version 1.2.5.
}
\usage{
getTransmartToken()
}
\value{
  A single character element is returned that is a secret authentication token, or \code{NULL} if there is no active connection or no refresh token.
}
\references{}
\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen. 
Contact: development@thehyve.nl}
\seealso{\code{\link{connectToTransmart}}}

\keyword{ database }
\keyword{ transmart }
