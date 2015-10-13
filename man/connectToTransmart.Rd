\name{connectToTransmart}
\alias{connectToTransmart}

\title{
  Initializes a new connection to the tranSMART database via the RESTful API
}
\description{
  This function initializes the necessary connection settings to access the
  tranSMART database.
}
\usage{
connectToTransmart(transmartDomain, use.authentication = TRUE, token = NULL, ...)
}

\arguments{
  \item{transmartDomain}{a character string naming the domain of the tranSMART server to connect to.}
  \item{use.authentication}{logical indicating whether or not to use OAuth authentication. Must be TRUE (default setting) if the tranSMART server requires OAuth authentication}
  \item{token}{A token that can be used to authenticate to Transmart. This is  the "refresh token" in OAuth terminology. To get such a token, use \code{\link{getTransmartToken}} after authenticating interactively for the first time.}
  \item{\dots}{additional arguments to be passed on to \code{\link{authenticateWithTransmart}}}
}
\details{
  This function initialises the connection settings to a tranSMART server. 
  It will set up the connection settings inside the \emph{transmartClientEnv} environment (if non-existent, this environment is created), which will enable this R client to access the tranSMART database through the available get methods (eg. \code{\link{getStudies}}).
  
  If \code{use.authentication} is not disabled, it will also guide the user through the authentication process.
  
  The \code{token} parameter can be used to connect to Transmart in non-interactive contexts. To do so, you must configure Transmart to use non-expiring refresh tokens and to re-use the same refresh token. For the first, set \code{grails.plugin.springsecurity.oauthProvider.tokenService.refreshTokenValiditySeconds = null} (and maybe remove \code{~transmart/.grails/oauth2db.h2.db}). For the second, set \code{grails.plugin.springsecurity.oauthProvider.tokenServices.reuseRefreshToken = true}.
}
\value{}
\references{}
\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen. 
Contact: development@thehyve.nl}
\note{}
\seealso{\code{\link{getStudies}}, \code{\link{connectToTransmart}}, \code{\link{getTransmartToken}}.

  For more information about the transmart RClient package and what it can be used for: \link{transmartRClient-package}
}
\examples{
\dontrun{
  
  # Set the function's \code{transmartDomain} argument to the location of the tranSMART database: 
  localTransmartDomain <- "http://some.transmart.server/transmart"
  connectToTransmart(localTransmartDomain)
  
  # Follow the instructions for authentication.
  }
}
\keyword{ environment }
\keyword{ connection }
\keyword{ database }
\keyword{ transmart }
