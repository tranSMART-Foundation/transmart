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
authenticateWithTransmart(oauthDomain, prefetched.request.token = NULL)
}
\arguments{
  \item{oauthDomain}{a character string naming the domain of the OAuth server}
  \item{prefetched.request.token}{a character string containing a prefetched token. If a request token has already been obtained, it can be entered here}
}
\details{
  If authentication was succesful, a security token is added to the \emph{transmartClientEnv}
  environment (if this environment does not exist, it will be created), which will authorize this R client to
  access the tranSMART database through the available get methods (eg. \code{\link{getStudies}}).
  
  If a previous authentication exists, the user will be asked whether it should be overwritten.
}
\value{}
\references{}
\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen. 
Contact: development@thehyve.nl}
\note{}
\seealso{\code{\link{connectToTransmart}}}
\examples{
\dontrun{  

  # Set the function's argument to the location of your OAuth 
  # and tranSMART servers, and follow the instructions
  localOAuthDomain <- "http://some.transmart.server/transmart"
  AuthenticateWithTransmart(localOAuthDomain) 
  }
}
\keyword{ environment }
\keyword{ connection }
\keyword{ database }
\keyword{ transmart }
