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
connectToTransmart(transmartDomain, use.authentication = TRUE, ...)
}

\arguments{
  \item{transmartDomain}{a character string naming the domain of the tranSMART server to connect to.}
  \item{use.authentication}{logical indicating whether or not to use OAuth authentication. Must be TRUE if the tranSMART server requires OAuth authentication}
  \item{\dots}{additional arguments to be passed on to \code{\link{authenticateWithTransmart}}}
}
\details{
  This function initialises the connection settings to a tranSMART server. 
  It will set up the connection settings inside the \emph{transmartClientEnv}
  environment (if non-existent, this environment is created), which will enable this R client to
  access the tranSMART database through the available get methods (eg. \code{\link{getStudies}}).
  
  If \code{use.authentication} is not disabled, it will also guide the user through the authentication process.
  
}
\value{}
\references{}
\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen. 
Contact: development@thehyve.nl}
\note{}
\seealso{\code{\link{getStudies}}.

For more information about the transmart RClient package and what it can be used for: \link{transmartRClient-package}}
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
