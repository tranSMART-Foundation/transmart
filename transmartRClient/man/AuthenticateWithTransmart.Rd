\name{authenticateWithTransmart}
\alias{authenticateWithTransmart}
\title{
  Authenticate the R client to gain access to tranSMART database
}
\description{
  Function that will redirect the user to an OAuth authentication server to
  acquire a token that grants the client (semi-)permanent access to tranSMART.
}
\usage{
authenticateWithTransmart(oauthDomain = "http://localhost:8080/transmart", prefetched.request.token = NULL)
}
\arguments{
  \item{oauthDomain}{The domain of the OAuth server}
  \item{prefetched.request.token}{If a request token has already been obtained, it can be entered here}
}
\details{
  If previous authentication exists, the user will be asked whether it should be overwritten.
}
\value{
  If authentication was succesful, a security token is added to the \emph{transmartClientEnv}
  environment (if non-existent, this environment is created), which will authorize this client to
  access the tranSMART database through the available get methods (eg. \code{getStudies()}).
}
\references{}
\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen. 
Contact: development@thehyve.nl}
\note{}
\seealso{}
\examples{
  # Set the function's argument to the location of your OAuth 
  # and tranSMART servers, and follow the instructions
  localOAuthDomain <- "http://localhost:8080/transmart"
  AuthenticateWithTransmart(localOAuthDomain) 
}
\keyword{ environment }
\keyword{ connection }
\keyword{ database }
